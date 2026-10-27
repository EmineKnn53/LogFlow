import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class Pipeline {

    private final Source<?> source;
    private final List<Stage<?, ?>> stages;
    private final Sink<?> sink;

    private final Sink<FailedRecord> deadLetterSink;
    private final List<ErrorPolicy> policies;
    private final List<String> names;

    private RunSummary summary = new RunSummary();

    // Onceki artimlarla uyumluluk
    public Pipeline(
            Source<?> source,
            List<Stage<?, ?>> stages,
            Sink<?> sink) {

        this(source, stages, sink, null, null, null);
    }

    // Artim 5: hata politikalari ve ikinci cikis kanali
    public Pipeline(
            Source<?> source,
            List<Stage<?, ?>> stages,
            Sink<?> sink,
            Sink<FailedRecord> deadLetterSink,
            List<ErrorPolicy> policies,
            List<String> names) {

        this.source = source;
        this.stages = new ArrayList<>(stages);
        this.sink = sink;
        this.deadLetterSink = deadLetterSink;

        this.policies = policies == null
                ? java.util.Collections.nCopies(
                    stages.size(),
                    new ErrorPolicy(
                        ErrorPolicy.Mode.FAIL_FAST, 0
                    )
                )
                : List.copyOf(policies);

        this.names = names == null
                ? stages.stream()
                    .map(s -> s.getClass().getSimpleName())
                    .toList()
                : List.copyOf(names);

        if (this.policies.size() != stages.size()
                || this.names.size() != stages.size()) {

            throw new IllegalArgumentException(
                "Asama/politika sayisi uyusmuyor"
            );
        }
    }

    public RunSummary summary() {
        return summary;
    }

    public void run() {

        summary = new RunSummary();

        long start = System.nanoTime();

        int openedStages = 0;
        boolean sinkOpened = false;
        boolean deadOpened = false;

        RuntimeException failure = null;

        try {

            sink.open();
            sinkOpened = true;

            if (deadLetterSink != null) {
                deadLetterSink.open();
                deadOpened = true;
            }

            for (Stage<?, ?> stage : stages) {
                stage.open();
                openedStages++;
            }

            runSource();

        } catch (RuntimeException e) {

            failure = e instanceof PipelineException
                    ? e
                    : new PipelineException(
                        "Pipeline durdu", e
                    );

        } finally {

            for (int i = openedStages - 1; i >= 0; i--) {
                try {
                    stages.get(i).close();
                } catch (RuntimeException e) {
                    failure = append(failure, e);
                }
            }

            if (deadOpened) {
                try {
                    deadLetterSink.close();
                } catch (RuntimeException e) {
                    failure = append(failure, e);
                }
            }

            if (sinkOpened) {
                try {
                    sink.close();
                } catch (RuntimeException e) {
                    failure = append(failure, e);
                }
            }

            summary.elapsed(
                System.nanoTime() - start
            );

            summary.print();
        }

        if (failure != null) {
            throw failure;
        }
    }

    private RuntimeException append(
            RuntimeException previous,
            RuntimeException next) {

        PipelineException wrapped =
                next instanceof PipelineException p
                    ? p
                    : new PipelineException(
                        "Kaynak kapatma hatasi", next
                    );

        if (previous == null) {
            return wrapped;
        }

        previous.addSuppressed(wrapped);
        return previous;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runSource() {

        Emitter next = item -> {

            ((Sink) sink).consume(item);

            summary.outgoingOne();
        };

        for (int i = stages.size() - 1; i >= 0; i--) {

            Stage stage = stages.get(i);
            Emitter downstream = next;

            ErrorPolicy policy = policies.get(i);

            String name = names.get(i) + "[" + i + "]";

            next = item -> {

                int attempts =
                        policy.mode() == ErrorPolicy.Mode.RETRY
                        ? policy.attempts() + 1
                        : 1;

                for (int attempt = 0;
                     attempt < attempts;
                     attempt++) {

                    final int[] emitted = {0};

                    try {

                        stage.process(item, value -> {

                            emitted[0]++;
                            downstream.emit(value);
                        });

                        if (emitted[0] == 0) {
                            summary.filteredOne();
                        }

                        return;

                    } catch (StageException e) {

                        if (policy.mode()
                                == ErrorPolicy.Mode.RETRY
                                && attempt + 1 < attempts) {

                            continue;
                        }

                        if (policy.mode()
                                == ErrorPolicy.Mode.FAIL_FAST
                                || policy.mode()
                                == ErrorPolicy.Mode.RETRY) {

                            throw new PipelineException(
                                "Asama hatasi: " + name, e
                            );
                        }

                        if (policy.mode()
                                == ErrorPolicy.Mode.SKIP) {

                            summary.skippedOne(name);

                        } else {

                            if (deadLetterSink == null) {

                                throw new PipelineException(
                                    "Dead-letter sink eksik"
                                );
                            }

                            String raw =
                                    item instanceof LogRecord r
                                    ? r.raw()
                                    : String.valueOf(item);

                            deadLetterSink.consume(
                                new FailedRecord(
                                    raw,
                                    name,
                                    e.getClass().getSimpleName()
                                        + ": " + e.getMessage(),
                                    Instant.now()
                                )
                            );

                            summary.deadLetterOne();
                        }

                        return;
                    }
                }
            };
        }

        Emitter first = next;

        ((Source) source).produce(item -> {

            summary.incomingOne();
            first.emit(item);
        });
    }
}