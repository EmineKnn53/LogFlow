
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PipelineIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void threeStagePipelineShouldFilterAndWriteFile()
            throws IOException {

        // 1. Gecici giris ve cikis dosyalarini olustur.
        Path input = tempDir.resolve("input.log");
        Path output = tempDir.resolve("output.log");

        // 2. Test icin kucuk bir CLF veri seti hazirla.
        List<String> lines = List.of(
                "127.0.0.1 - - [29/Sep/2026:10:15:01] "
                        + "\"GET /index.html HTTP/1.1\" 200 512",

                "10.0.0.5 - - [29/Sep/2026:10:15:05] "
                        + "\"GET /admin HTTP/1.1\" 403 128",

                "192.168.1.10 - - [29/Sep/2026:10:15:09] "
                        + "\"GET /api/users HTTP/1.1\" 500 256",

                "192.168.1.15 - - [29/Sep/2026:10:15:12] "
                        + "\"GET /home HTTP/1.1\" 200 768"
        );

        Files.write(input, lines);

        // 3. PipelineBuilder ile uc asamali pipeline kur.
        ParserStage parser = new ParserStage();

        Pipeline pipeline = PipelineBuilder
                .from(new FileLineSource(input.toString()))
                .via(parser)
                .via(new PredicateFilterStage(
                        record -> record.status() >= 400
                ))
                .via(new ProjectionStage(
                        "clientIp", "status", "path"
                ))
                .to(new FileSink<>(output.toString()));

        // 4. Pipeline'i calistir.
        pipeline.run();

        // 5. Dosyanin olustugunu kontrol et.
        assertTrue(Files.exists(output));

        // 6. Dosyadaki kayitlari oku.
        List<String> results = Files.readAllLines(output);

        // 7. Yalnizca 403 ve 500 durum kodlari kalmali.
        assertEquals(2, results.size());

        assertEquals(
                "{clientIp=10.0.0.5, status=403, path=/admin}",
                results.get(0)
        );

        assertEquals(
                "{clientIp=192.168.1.10, status=500, path=/api/users}",
                results.get(1)
        );

        // 8. Parser hatasi olmamali.
        assertEquals(0, parser.getErrorCount());
    }
}
