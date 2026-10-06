import java.time.Instant;
import java.util.Map;

/**
 * Log satirlarinin yapilandirilmis halini temsil eder.
 *
 * Bu record immutable (degistirilemez) bir alan modelidir.
 * attributes alani, ilerleyen haftalarda yeni bilgilerin
 * mevcut parser degistirilmeden kayda eklenebilmesi icin
 * genisleme noktasi olarak kullanilir.
 */
public record LogRecord(
        Instant timestamp,
        String clientIp,
        String method,
        String path,
        int status,
        long bytes,
        String userAgent,
        Map<String, String> attributes,
        String raw
) {
}