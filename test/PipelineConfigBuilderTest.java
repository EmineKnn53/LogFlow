
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineConfigBuilderTest {

    @TempDir
    Path tempDir;

    // Testler icin gecici YAML dosyasi olusturur.
    private Path createYaml(String name, String content)
            throws Exception {

        Path file = tempDir.resolve(name);
        Files.writeString(file, content);
        return file;
    }

    // TEST 1: Gecerli yapilandirma
    @Test
    void gecerliYapilandirmaPipelineOlusturmali()
            throws Exception {

        Path config = createYaml("valid.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: data/access-small.log\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "  - type: filter\n" +
            "    minStatus: 400\n" +
            "  - type: projection\n" +
            "    fields: [clientIp, status]\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        Pipeline pipeline = builder.build(config.toString());

        assertNotNull(pipeline);
    }

    // TEST 2: Bilinmeyen asama turu
    @Test
    void bilinmeyenStageReddedilmeli()
            throws Exception {

        Path config = createYaml("unknown.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: input.log\n" +
            "stages:\n" +
            "  - type: bilinmeyen\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build(config.toString())
        );
    }

    // TEST 3: Eksik filtre parametresi
    @Test
    void eksikFilterParametresiReddedilmeli()
            throws Exception {

        Path config = createYaml("filter.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: input.log\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "  - type: filter\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build(config.toString())
        );
    }

    // TEST 4: Gecersiz projection alani
    @Test
    void gecersizProjectionAlaniReddedilmeli()
            throws Exception {

        Path config = createYaml("projection.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: input.log\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "  - type: projection\n" +
            "    fields: [olmayanAlan]\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        assertThrows(
            IllegalArgumentException.class,
            () -> builder.build(config.toString())
        );
    }

    // TEST 5: Yanlis parametre turu
    @Test
    void yanlisParametreTuruReddedilmeli()
            throws Exception {

        Path config = createYaml("wrong-type.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: input.log\n" +
            "stages:\n" +
            "  - type: parser\n" +
            "  - type: filter\n" +
            "    minStatus: abc\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.build(config.toString())
        );

        assertTrue(
            exception.getMessage().contains("minStatus")
        );
    }

    // TEST 6: Bos pipeline
    @Test
    void bosPipelineReddedilmeli()
            throws Exception {

        Path config = createYaml("empty-pipeline.yaml",
            "source:\n" +
            "  type: file\n" +
            "  path: input.log\n" +
            "stages: []\n" +
            "sink:\n" +
            "  type: file\n" +
            "  path: output.log\n"
        );

        PipelineConfigBuilder builder =
            new PipelineConfigBuilder();

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> builder.build(config.toString())
        );

        assertTrue(
            exception.getMessage().contains("en az bir asama")
        );
    }
}
