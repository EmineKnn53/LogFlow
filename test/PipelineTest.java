import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineTest {

    @Test
    void pipelineShouldTransferDataFromSourceToSink() {

        // Test verileri
        List<String> input = List.of("birinci", "ikinci", "ucuncu");

        // Source
        Source<String> source = emitter -> {
            for (String item : input) {
                emitter.emit(item);
            }
        };

        // Stage
        Stage<String, LogRecord> stage = new Stage<>() {

            @Override
            public void open() {
            }

            @Override
            public void process(
                    String item,
                    Emitter<LogRecord> emitter) throws StageException {

                LogRecord record = new LogRecord(
                        null,
                        null,
                        null,
                        item,
                        200,
                        0,
                        null,
                        java.util.Map.of(),
                        item
                );

                emitter.emit(record);
            }

            @Override
            public void close() {
            }
        };

        // Sink'ten gelen kayıtları burada tutacağız
        List<LogRecord> results = new ArrayList<>();

        Sink<LogRecord> sink = results::add;

        // Pipeline
        Pipeline pipeline = new Pipeline(
                source,
                stage,
                sink
        );

        pipeline.run();

        // Kontroller
        assertEquals(3, results.size());

        assertEquals("birinci", results.get(0).path());
        assertEquals("ikinci", results.get(1).path());
        assertEquals("ucuncu", results.get(2).path());
    }
}