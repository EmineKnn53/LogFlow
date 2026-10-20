
import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class YamlPipelineIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void yamlPipelineDosyayiFiltreleyipYazmali()
            throws Exception {

        // 1. Gecici dosyalari hazirla.
        Path input = tempDir.resolve("input.log");
        Path output = tempDir.resolve("output.log");
        Path yamlFile = tempDir.resolve("pipeline.yaml");

        // 2. Test verilerini olustur.
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

        // 3. YAML yapilandirmasini olustur.
        String yaml =
            "source:\n" +
            "  type: file\n" +
            "  path: '" + input.toString().replace("\\", "/") + "'\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "  - type: filter\n" +
            "    minStatus: 400\n" +
            "  - type: projection\n" +
            "    fields: [clientIp, status, path]\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: '" + output.toString().replace("\\", "/") + "'\n";

        Files.writeString(yamlFile, yaml);

        // 4. YAML'den pipeline olustur.
        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        Pipeline pipeline = builder.build(
            yamlFile.toString()
        );

        // 5. Pipeline'i calistir.
        pipeline.run();

        // 6. Cikti dosyasini kontrol et.
        assertTrue(Files.exists(output));

        List<String> results = Files.readAllLines(output);

        assertEquals(2, results.size());

        assertEquals(
            "{clientIp=10.0.0.5, status=403, path=/admin}",
            results.get(0)
        );

        assertEquals(
            "{clientIp=192.168.1.10, status=500, path=/api/users}",
            results.get(1)
        );
    }
}
