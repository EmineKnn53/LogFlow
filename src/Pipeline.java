
import java.util.ArrayList;
import java.util.List;

public class Pipeline {

    private final Source<?> source;
    private final List<Stage<?, ?>> stages;
    private final Sink<?> sink;

    public Pipeline(
            Source<?> source,
            List<Stage<?, ?>> stages,
            Sink<?> sink) {

        this.source = source;
        this.stages = new ArrayList<>(stages);
        this.sink = sink;
    }

    public void run() {

        int openedStages = 0;
        boolean sinkOpened = false;

        try {
            for (Stage<?, ?> stage : stages) {
                stage.open();
                openedStages++;
            }

            sink.open();
            sinkOpened = true;

            runSource();

        } finally {

            for (int i = openedStages - 1; i >= 0; i--) {
                stages.get(i).close();
            }

            if (sinkOpened) {
                sink.close();
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void runSource() {

        Emitter next = item -> ((Sink) sink).consume(item);

        for (int i = stages.size() - 1; i >= 0; i--) {

            Stage stage = stages.get(i);
            Emitter downstream = next;

            next = item -> {
                try {
                    stage.process(item, downstream);
                } catch (StageException e) {
                    throw new RuntimeException(
                            "Pipeline asamasinda hata olustu.", e
                    );
                }
            };
        }

        Emitter first = next;
        ((Source) source).produce(first);
    }
}
