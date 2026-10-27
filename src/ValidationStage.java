import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValidationStage
        implements Stage<LogRecord, LogRecord> {

    private static final Set<String> FIELDS = Set.of(
        "timestamp", "clientIp", "method", "path",
        "status", "bytes", "userAgent"
    );

    private final List<String> required;

    private final long minBytes;
    private final long maxBytes;

    private final int minStatus;
    private final int maxStatus;

    private final Instant minTimestamp;
    private final Instant maxTimestamp;

    private final boolean validateIp;
    private final long maxRawLength;

    public ValidationStage(Map<String, Object> params) {

        Object value = params.getOrDefault(
            "requiredFields", List.of()
        );

        if (!(value instanceof List<?> list)
                || list.stream().anyMatch(
                    x -> !(x instanceof String)
                    || !FIELDS.contains(x))) {

            throw new IllegalArgumentException(
                "requiredFields gecersiz"
            );
        }

        required = list.stream()
                .map(String.class::cast)
                .toList();

        minBytes = longValue(params, "minBytes", 0);
        maxBytes = longValue(
            params, "maxBytes", Long.MAX_VALUE
        );

        minStatus = (int) longValue(
            params, "minStatus", 100
        );

        maxStatus = (int) longValue(
            params, "maxStatus", 599
        );

        minTimestamp = timestamp(
            params,
            "minTimestamp",
            Instant.parse("2000-01-01T00:00:00Z")
        );

        maxTimestamp = timestamp(
            params,
            "maxTimestamp",
            Instant.parse("2100-01-01T00:00:00Z")
        );

        Object ip = params.getOrDefault(
            "validateIp", Boolean.TRUE
        );

        if (!(ip instanceof Boolean)) {
            throw new IllegalArgumentException(
                "validateIp boolean olmali"
            );
        }

        validateIp = (Boolean) ip;

        maxRawLength = longValue(
            params, "maxRawLength", Long.MAX_VALUE
        );

        if (minBytes > maxBytes
                || minStatus > maxStatus
                || minTimestamp.isAfter(maxTimestamp)) {

            throw new IllegalArgumentException(
                "Validation araliklari ters"
            );
        }
    }

    private static long longValue(
            Map<String, Object> params,
            String key,
            long fallback) {

        Object value = params.get(key);

        if (value == null) {
            return fallback;
        }

        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(
                key + " sayi olmali"
            );
        }

        return number.longValue();
    }

    private static Instant timestamp(
            Map<String, Object> params,
            String key,
            Instant fallback) {

        Object value = params.get(key);

        if (value == null) {
            return fallback;
        }

        try {
            return Instant.parse(String.valueOf(value));

        } catch (RuntimeException e) {

            throw new IllegalArgumentException(
                key + " ISO-8601 tarih olmali", e
            );
        }
    }

    @Override
    public void process(
            LogRecord r,
            Emitter<LogRecord> out)
            throws StageException {

        if (r.raw() != null
                && r.raw().length() > maxRawLength) {

            throw new StageException(
                "Ham satir cok uzun: " + r.raw().length()
            );
        }

        for (String field : required) {

            Object value = switch (field) {

                case "timestamp" -> r.timestamp();
                case "clientIp" -> r.clientIp();
                case "method" -> r.method();
                case "path" -> r.path();
                case "status" -> r.status();
                case "bytes" -> r.bytes();
                case "userAgent" -> r.userAgent();

                default -> null;
            };

            if (value == null
                    || (value instanceof String s
                    && s.isBlank())) {

                throw new StageException(
                    "Zorunlu alan eksik: " + field
                );
            }
        }

        if (r.bytes() < minBytes
                || r.bytes() > maxBytes) {

            throw new StageException(
                "Bytes aralik disi: " + r.bytes()
            );
        }

        if (r.status() < minStatus
                || r.status() > maxStatus) {

            throw new StageException(
                "HTTP status aralik disi: " + r.status()
            );
        }

        if (r.timestamp() == null
                || r.timestamp().isBefore(minTimestamp)
                || r.timestamp().isAfter(maxTimestamp)) {

            throw new StageException(
                "Timestamp aralik disi: " + r.timestamp()
            );
        }

        if (validateIp && !validIp(r.clientIp())) {

            throw new StageException(
                "IP bicimi gecersiz: " + r.clientIp()
            );
        }

        out.emit(r);
    }

    private static boolean validIp(String input) {

        if (input == null) {
            return false;
        }

        String[] octets = input.split("\\.", -1);

        if (octets.length != 4) {
            return false;
        }

        for (String octet : octets) {

            if (!octet.matches("[0-9]{1,3}")) {
                return false;
            }

            int n = Integer.parseInt(octet);

            if (n > 255
                    || (octet.length() > 1
                    && octet.startsWith("0"))) {

                return false;
            }
        }

        return true;
    }
}