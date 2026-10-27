
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineConfigBuilder {

    private final YamlConfigLoader loader =
            new YamlConfigLoader();

    private final StageRegistry registry =
            DefaultStageRegistry.create();

    public Pipeline build(String configFile) {

        Map<String, Object> config =
                loader.load(configFile);

        Map<String, Object> sourceConfig =
                asMap(config.get("source"), "source");

        Map<String, Object> sinkConfig =
                asMap(config.get("sink"), "sink");

        if (!"file".equals(sourceConfig.get("type"))) {
            throw new IllegalArgumentException(
                "Desteklenmeyen source tipi."
            );
        }

        if (!"file".equals(sinkConfig.get("type"))) {
            throw new IllegalArgumentException(
                "Desteklenmeyen sink tipi."
            );
        }

        String inputPath =
                requiredString(sourceConfig, "path");

        String outputPath =
                requiredString(sinkConfig, "path");

        Object stagesValue = config.get("stages");

        if (!(stagesValue instanceof List<?> stageConfigs)) {
            throw new IllegalArgumentException(
                "stages bir liste olmali."
            );
        }

        if (stageConfigs.isEmpty()) {
            throw new IllegalArgumentException(
                "Pipeline en az bir asama icermelidir."
            );
        }

        List<Stage<?, ?>> stages = new ArrayList<>();
        List<ErrorPolicy> policies = new ArrayList<>();
        List<String> names = new ArrayList<>();

        boolean requiresDeadLetter = false;

        for (Object value : stageConfigs) {

            Map<String, Object> stageConfig =
                    asMap(value, "stage");

            String type =
                    requiredString(stageConfig, "type");

            Map<String, Object> params =
                    new HashMap<>(stageConfig);

            params.remove("type");

            // ARTIM 5: Asama hata politikasini oku
            Object policyValue =
                    params.remove("errorPolicy");

            // Eski YAML dosyalariyla uyumluluk
            ErrorPolicy policy = ErrorPolicy.parse(
                policyValue == null
                    ? ("parser".equals(type)
                        ? "SKIP"
                        : "FAIL_FAST")
                    : policyValue
            );

            if (policy.mode()
                    == ErrorPolicy.Mode.DEAD_LETTER) {

                requiresDeadLetter = true;
            }

            Stage<?, ?> stage =
                    registry.create(type, params);

            stages.add(stage);
            policies.add(policy);
            names.add(type);
        }

        // ARTIM 5: Ikinci cikis kanali
        Sink<FailedRecord> deadSink = null;

        if (config.containsKey("deadLetter")) {

            Map<String, Object> dead =
                    asMap(
                        config.get("deadLetter"),
                        "deadLetter"
                    );

            if (!"file".equals(dead.get("type"))) {
                throw new IllegalArgumentException(
                    "deadLetter.type file olmali"
                );
            }

            String deadPath =
                    requiredString(dead, "path");

            if (java.nio.file.Path.of(deadPath)
                    .toAbsolutePath()
                    .normalize()
                    .equals(
                        java.nio.file.Path.of(outputPath)
                            .toAbsolutePath()
                            .normalize()
                    )) {

                throw new IllegalArgumentException(
                    "Ana cikti ile dead-letter yolu ayni olamaz"
                );
            }

            deadSink = new FileSink<>(deadPath);
        }

        if (requiresDeadLetter && deadSink == null) {
            throw new IllegalArgumentException(
                "DEAD_LETTER politikasi icin deadLetter hedefi gerekli"
            );
        }

        return new Pipeline(
            new FileLineSource(inputPath),
            stages,
            new FileSink<>(outputPath),
            deadSink,
            policies,
            names
        );
    }

    private String requiredString(
            Map<String, Object> config,
            String key) {

        Object value = config.get(key);

        if (!(value instanceof String text)
                || text.isBlank()) {

            throw new IllegalArgumentException(
                "Gecersiz veya eksik alan: " + key
            );
        }

        return text;
    }

    private Map<String, Object> asMap(
            Object value,
            String name) {

        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(
                name + " bir nesne olmali."
            );
        }

        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<?, ?> entry : map.entrySet()) {

            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException(
                    name + " anahtarlari metin olmali."
                );
            }

            result.put(key, entry.getValue());
        }

        return result;
    }
}
