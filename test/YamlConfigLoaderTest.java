
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class YamlConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void gecerliYamlDosyasiOkunmali() throws Exception {
        Path file = tempDir.resolve("valid.yaml");

        Files.writeString(file,
            "source:\n" +
            "  type: file\n" +
            "  path: data/access-small.log\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        YamlConfigLoader loader = new YamlConfigLoader();
        Map<String, Object> config =
            loader.load(file.toString());

        assertNotNull(config);
        assertTrue(config.containsKey("source"));
        assertTrue(config.containsKey("stages"));
        assertTrue(config.containsKey("sink"));
    }

    @Test
    void olmayanDosyaHataVermeli() {
        Path file = tempDir.resolve("olmayan.yaml");

        YamlConfigLoader loader = new YamlConfigLoader();

        assertThrows(
            IllegalArgumentException.class,
            () -> loader.load(file.toString())
        );
    }

    @Test
    void kokYapisiListeOlanYamlReddedilmeli()
            throws Exception {

        Path file = tempDir.resolve("invalid.yaml");

        Files.writeString(file,
            "- parser\n- filter\n"
        );

        YamlConfigLoader loader = new YamlConfigLoader();

        assertThrows(
            IllegalArgumentException.class,
            () -> loader.load(file.toString())
        );
    }

    @Test
    void sayisalAnahtarReddedilmeli()
            throws Exception {

        Path file = tempDir.resolve("numeric-key.yaml");

        Files.writeString(file,
            "123: value\n"
        );

        YamlConfigLoader loader = new YamlConfigLoader();

        assertThrows(
            IllegalArgumentException.class,
            () -> loader.load(file.toString())
        );
    }
}
