
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StageRegistry {

    private final Map<String, StageFactory> factories = new HashMap<>();

    public void register(String name, StageFactory factory) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                "Asama adi bos olamaz."
            );
        }

        factories.put(name, Objects.requireNonNull(factory));
    }

    public Stage<?, ?> create(
            String name,
            Map<String, Object> parameters) {

        StageFactory factory = factories.get(name);

        if (factory == null) {
            throw new IllegalArgumentException(
                "Bilinmeyen asama turu: " + name
            );
        }

        return factory.create(parameters);
    }
}
