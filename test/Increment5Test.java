
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class Increment5Test {

    @Test
    void dirtyFixtureProducesExpectedResults()
            throws Exception {

        Pipeline pipeline =
            new PipelineConfigBuilder().build(
                "configs/dirty-validation.yaml"
            );

        pipeline.run();

        RunSummary summary = pipeline.summary();

        assertEquals(9, summary.incoming());
        assertEquals(2, summary.outgoing());
        assertEquals(7, summary.deadLetters());
        assertEquals(0, summary.filtered());

        assertEquals(
            2,
            Files.readAllLines(
                Path.of("dirty-output.log")
            ).size()
        );

        assertEquals(
            7,
            Files.readAllLines(
                Path.of("dirty-dead-letter.jsonl")
            ).size()
        );
    }

    @Test
    void emptyFileCompletesSuccessfully() {

        Pipeline pipeline = new Pipeline(
            new FileLineSource(
                "data/access-empty.log"
            ),
            List.of(new ParserStage(true)),
            (Sink<LogRecord>) item -> {}
        );

        pipeline.run();

        assertEquals(0, pipeline.summary().incoming());
        assertEquals(0, pipeline.summary().outgoing());
    }

    @Test
    void skipPolicyCountsErrors() {

        Source<String> source = out -> {
            out.emit("bad");
            out.emit("good");
        };

        Stage<String, String> stage = (item, out) -> {

            if (item.equals("bad")) {
                throw new StageException("bad item");
            }

            out.emit(item);
        };

        List<String> valid = new ArrayList<>();

        Pipeline pipeline = new Pipeline(
            source,
            List.of(stage),
            (Sink<String>) valid::add,
            null,
            List.of(ErrorPolicy.parse("SKIP")),
            List.of("test")
        );

        pipeline.run();

        assertEquals(List.of("good"), valid);

        assertEquals(
            1L,
            pipeline.summary()
                .skippedByStage()
                .get("test[0]")
        );
    }

    @Test
    void failFastStopsPipeline() {

        Source<String> source =
            out -> out.emit("bad");

        Stage<String, String> stage =
            (item, out) -> {
                throw new StageException("fail");
            };

        Pipeline pipeline = new Pipeline(
            source,
            List.of(stage),
            (Sink<String>) item -> {}
        );

        assertThrows(
            PipelineException.class,
            pipeline::run
        );

        assertEquals(
            1,
            pipeline.summary().incoming()
        );
    }

    @Test
    void retrySucceedsOnThirdAttempt() {

        int[] calls = {0};

        Stage<String, String> stage =
            (item, out) -> {

                calls[0]++;

                if (calls[0] < 3) {
                    throw new StageException(
                        "temporary error"
                    );
                }

                out.emit(item);
            };

        List<String> received = new ArrayList<>();

        Pipeline pipeline = new Pipeline(
            (Source<String>) out -> out.emit("ok"),
            List.of(stage),
            (Sink<String>) received::add,
            null,
            List.of(ErrorPolicy.parse("RETRY(2)")),
            List.of("transient")
        );

        pipeline.run();

        assertEquals(3, calls[0]);
        assertEquals(List.of("ok"), received);
    }

    @Test
    void missingDeadLetterSinkIsRejected()
            throws Exception {

        Path yaml = Files.createTempFile(
            "logflow-invalid",
            ".yaml"
        );

        try {

            Files.writeString(
                yaml,
                "source:\n" +
                "  type: file\n" +
                "  path: data/access-small.log\n" +
                "stages:\n" +
                "  - type: parser\n" +
                "    errorPolicy: DEAD_LETTER\n" +
                "sink:\n" +
                "  type: file\n" +
                "  path: output.log\n"
            );

            assertThrows(
                IllegalArgumentException.class,
                () -> new PipelineConfigBuilder()
                    .build(yaml.toString())
            );

        } finally {
            Files.deleteIfExists(yaml);
        }
    }
}
