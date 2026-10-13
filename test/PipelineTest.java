
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

public class PipelineTest {

    @Test
    void pipelineShouldTransferDataThroughStage() {

        // Test icin ornek veri kaynagi
        Source<String> source = out -> {
            out.emit("birinci");
            out.emit("ikinci");
            out.emit("ucuncu");
        };

        // Gelen metinleri buyuk harfe donusturen asama
        Stage<String, String> stage = (input, out) -> {
            out.emit(input.toUpperCase(Locale.ROOT));
        };

        // Pipeline sonucunu saklayacak liste
        List<String> results = new ArrayList<>();

        // Sonuclari listeye ekleyen sink
        Sink<String> sink = results::add;

        // PipelineBuilder ile pipeline olusturma
        Pipeline pipeline = PipelineBuilder
                .from(source)
                .via(stage)
                .to(sink);

        // Pipeline'i calistirma
        pipeline.run();

        // Sonuclari dogrulama
        assertEquals(3, results.size());

        assertEquals("BIRINCI", results.get(0));
        assertEquals("IKINCI", results.get(1));
        assertEquals("UCUNCU", results.get(2));
    }
}
