
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class YamlConfigLoader {

    public Map<String, Object> load(String filename) {
        Path path = Path.of(filename);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                "YAML dosyasi bulunamadi: " + filename
            );
        }

        LoaderOptions options = new LoaderOptions();
        Yaml yaml = new Yaml(options);

        try (InputStream input = Files.newInputStream(path)) {
            Object result = yaml.load(input);

            if (!(result instanceof Map<?, ?> data)) {
                throw new IllegalArgumentException(
                    "YAML dosyasinin kok yapisi bir nesne olmali."
                );
            }

            for (Object key : data.keySet()) {
                if (!(key instanceof String)) {
                    throw new IllegalArgumentException(
                        "YAML anahtarlari metin olmali."
                    );
                }
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> config =
                (Map<String, Object>) data;

            return config;

        } catch (IOException e) {
            throw new IllegalStateException(
                "YAML dosyasi okunamadi: " + filename, e
            );
        }
    }
}
