
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectionStage
        implements Stage<LogRecord, Map<String, Object>> {

    private static final Set<String> ALLOWED = Set.of(
            "timestamp", "clientIp", "method", "path",
            "status", "bytes", "userAgent", "attributes", "raw"
    );

    private final List<String> fields;

    public ProjectionStage(String... fields) {
        this.fields = List.of(fields);

        if (this.fields.isEmpty()
                || !ALLOWED.containsAll(this.fields)) {
            throw new IllegalArgumentException(
                    "Gecersiz veya bos alan secimi."
            );
        }
    }

    @Override
    public void process(
            LogRecord input,
            Emitter<Map<String, Object>> out) {

        Map<String, Object> result = new LinkedHashMap<>();

        for (String field : fields) {
            Object value = switch (field) {
                case "timestamp" -> input.timestamp();
                case "clientIp" -> input.clientIp();
                case "method" -> input.method();
                case "path" -> input.path();
                case "status" -> input.status();
                case "bytes" -> input.bytes();
                case "userAgent" -> input.userAgent();
                case "attributes" -> input.attributes();
                case "raw" -> input.raw();
                default -> throw new IllegalArgumentException(field);
            };
            result.put(field, value);
        }

        out.emit(result);
    }
}
