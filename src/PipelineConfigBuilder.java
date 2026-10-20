
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PipelineConfigBuilder {

    private final YamlConfigLoader loader = new YamlConfigLoader();
    private final StageRegistry registry = DefaultStageRegistry.create();

    public Pipeline build(String configFile) {

        Map<String, Object> config = loader.load(configFile);

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

        String inputPath = requiredString(sourceConfig, "path");
        String outputPath = requiredString(sinkConfig, "path");

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

        for (Object value : stageConfigs) {

            Map<String, Object> stageConfig =
                    asMap(value, "stage");

            String type = requiredString(stageConfig, "type");

            Map<String, Object> params = new HashMap<>(stageConfig);
            params.remove("type");

            Stage<?, ?> stage = registry.create(type, params);

            stages.add(stage);
        }

        return new Pipeline(
                new FileLineSource(inputPath),
                stages,
                new FileSink<>(outputPath)
        );
    }

    private String requiredString(
            Map<String, Object> config,
            String key
    ) {
        Object value = config.get(key);

        if (!(value instanceof String text) || text.isBlank()) {
            throw new IllegalArgumentException(
                    "Gecersiz veya eksik alan: " + key
            );
        }

        return text;
    }

    private Map<String, Object> asMap(
            Object value,
            String name
    ) {
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
