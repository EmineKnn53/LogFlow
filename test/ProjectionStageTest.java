
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProjectionStageTest {

    private LogRecord record;
    private CollectingEmitter<Map<String, Object>> emitter;

    @BeforeEach
    void setUp() {
        record = new LogRecord(
                Instant.parse("2026-09-29T10:15:01Z"),
                "127.0.0.1",
                "GET",
                "/api/users",
                200,
                512L,
                "Mozilla Firefox",
                Map.of("source", "test"),
                "ornek log satiri"
        );

        emitter = new CollectingEmitter<>();
    }

    // 1. Secilen alanlar dogru aktarilmali.
    @Test
    void selectedFieldsShouldBeProjected() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("clientIp", "status", "path");

        stage.process(record, emitter);

        assertEquals(1, emitter.size());

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals("127.0.0.1", result.get("clientIp"));
        assertEquals(200, result.get("status"));
        assertEquals("/api/users", result.get("path"));
    }

    // 2. Secilmeyen alanlar cikartilmali.
    @Test
    void unselectedFieldsShouldBeExcluded() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("clientIp", "status");

        stage.process(record, emitter);

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals(2, result.size());
        assertFalse(result.containsKey("method"));
        assertFalse(result.containsKey("userAgent"));
        assertFalse(result.containsKey("raw"));
    }

    // 3. Alanlar kullanicinin sectigi sirada olmali.
    @Test
    void fieldOrderShouldBePreserved() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("path", "status", "clientIp");

        stage.process(record, emitter);

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals(
                List.of("path", "status", "clientIp"),
                List.copyOf(result.keySet())
        );
    }

    // 4. Gecersiz alan adi reddedilmeli.
    @Test
    void invalidFieldShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectionStage("clientIp", "unknownField")
        );
    }

    // 5. Bos alan secimi reddedilmeli.
    @Test
    void emptyFieldsShouldBeRejected() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new ProjectionStage()
        );
    }

    // 6. Tek alan secimi dogru calismali.
    @Test
    void singleFieldShouldBeProjected() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("status");

        stage.process(record, emitter);

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals(1, result.size());
        assertEquals(200, result.get("status"));
    }

    // 7. Timestamp alani dogru aktarilmali.
    @Test
    void timestampShouldBePreserved() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("timestamp");

        stage.process(record, emitter);

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals(
                Instant.parse("2026-09-29T10:15:01Z"),
                result.get("timestamp")
        );
    }

    // 8. Attributes alani korunmali.
    @Test
    void attributesShouldBePreserved() throws StageException {

        ProjectionStage stage =
                new ProjectionStage("attributes");

        stage.process(record, emitter);

        Map<String, Object> result = emitter.getItems().get(0);

        assertEquals(
                Map.of("source", "test"),
                result.get("attributes")
        );
    }
}
