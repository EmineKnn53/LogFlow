
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

public class FilterOrderExperiment {

    public static void main(String[] args) {

        String inputFile = args.length > 0
                ? args[0]
                : "data/access-small.log";

        System.out.println("=== FILTER ORDER EXPERIMENT ===");
        System.out.println("Input: " + inputFile);

        Result first = runExperiment(
                inputFile, true
        );

        Result second = runExperiment(
                inputFile, false
        );

        System.out.println("\n=== RESULTS ===");

        printResult("Status -> Path", first);
        printResult("Path -> Status", second);

        boolean sameResults =
                first.records.equals(second.records);

        System.out.println(
                "\nSame output: " + sameResults
        );

        if (!sameResults) {
            throw new AssertionError(
                    "Filter orders produced different results!"
            );
        }
    }

    private static Result runExperiment(
            String inputFile,
            boolean statusFirst) {

        AtomicLong statusChecks = new AtomicLong();
        AtomicLong pathChecks = new AtomicLong();

        Predicate<LogRecord> statusFilter = record -> {
            statusChecks.incrementAndGet();
            return record.status() >= 400;
        };

        Predicate<LogRecord> pathFilter = record -> {
            pathChecks.incrementAndGet();
            return record.path().startsWith("/api");
        };

        List<String> records = new ArrayList<>();

        Sink<String> sink = records::add;

        PipelineBuilder<LogRecord> builder =
                PipelineBuilder
                        .from(new FileLineSource(inputFile))
                        .via(new ParserStage());

        long start = System.nanoTime();

        if (statusFirst) {
            builder = builder
                    .via(new PredicateFilterStage(statusFilter))
                    .via(new PredicateFilterStage(pathFilter));
        } else {
            builder = builder
                    .via(new PredicateFilterStage(pathFilter))
                    .via(new PredicateFilterStage(statusFilter));
        }

        Pipeline pipeline = builder
                .via(new ProjectionStage(
                        "clientIp", "status", "path"
                ))
                .via((java.util.Map<String, Object> input,
                      Emitter<String> out) ->
                        out.emit(input.toString()))
                .to(sink);

        pipeline.run();

        long elapsed = System.nanoTime() - start;

        return new Result(
                records,
                statusChecks.get(),
                pathChecks.get(),
                elapsed
        );
    }

    private static void printResult(
            String name,
            Result result) {

        System.out.println("\nOrder: " + name);

        System.out.println(
                "Output records: " + result.records.size()
        );

        System.out.println(
                "Status checks: " + result.statusChecks
        );

        System.out.println(
                "Path checks: " + result.pathChecks
        );

        System.out.println(
                "Total checks: "
                        + (result.statusChecks + result.pathChecks)
        );

        System.out.printf(
                "Elapsed time: %.3f ms%n",
                result.elapsedNanos / 1_000_000.0
        );
    }

    private static class Result {

        final List<String> records;
        final long statusChecks;
        final long pathChecks;
        final long elapsedNanos;

        Result(
                List<String> records,
                long statusChecks,
                long pathChecks,
                long elapsedNanos) {

            this.records = List.copyOf(records);
            this.statusChecks = statusChecks;
            this.pathChecks = pathChecks;
            this.elapsedNanos = elapsedNanos;
        }
    }
}
