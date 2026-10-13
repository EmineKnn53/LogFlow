
import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PredicateFilterStageTest {

    private CollectingEmitter<LogRecord> emitter;

    @BeforeEach
    void setUp() {
        emitter = new CollectingEmitter<>();
    }

    private LogRecord createRecord(
            int status, String path, Instant timestamp) {

        return new LogRecord(
                timestamp,
                "127.0.0.1",
                "GET",
                path,
                status,
                512L,
                "Mozilla",
                Map.of(),
                "ornek log satiri"
        );
    }

    // 1. Status 400 ve uzeri olan kayit gecmeli.
    @Test
    void status400AndAboveShouldPass() throws StageException {

        PredicateFilterStage filter =
                new PredicateFilterStage(r -> r.status() >= 400);

        filter.process(
                createRecord(403, "/admin", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertEquals(1, emitter.size());
        assertEquals(403, emitter.getItems().get(0).status());
    }

    // 2. Status 400 altindaki kayit elenmeli.
    @Test
    void statusBelow400ShouldBeRejected() throws StageException {

        PredicateFilterStage filter =
                new PredicateFilterStage(r -> r.status() >= 400);

        filter.process(
                createRecord(200, "/index", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertTrue(emitter.isEmpty());
    }

    // 3. Path /api ile basliyorsa kayit gecmeli.
    @Test
    void apiPathShouldPass() throws StageException {

        PredicateFilterStage filter =
                new PredicateFilterStage(
                        r -> r.path().startsWith("/api")
                );

        filter.process(
                createRecord(200, "/api/users", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertEquals(1, emitter.size());
    }

    // 4. Path /api ile baslamiyorsa kayit elenmeli.
    @Test
    void nonApiPathShouldBeRejected() throws StageException {

        PredicateFilterStage filter =
                new PredicateFilterStage(
                        r -> r.path().startsWith("/api")
                );

        filter.process(
                createRecord(200, "/home", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertTrue(emitter.isEmpty());
    }

    // 5. Belirlenen zaman araligindaki kayit gecmeli.
    @Test
    void timestampInsideRangeShouldPass() throws StageException {

        Instant start = Instant.parse("2026-09-29T09:00:00Z");
        Instant end = Instant.parse("2026-09-29T11:00:00Z");

        PredicateFilterStage filter =
                new PredicateFilterStage(
                        r -> !r.timestamp().isBefore(start)
                                && !r.timestamp().isAfter(end)
                );

        filter.process(
                createRecord(200, "/home", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertEquals(1, emitter.size());
    }

    // 6. Zaman araligi disindaki kayit elenmeli.
    @Test
    void timestampOutsideRangeShouldBeRejected() throws StageException {

        Instant start = Instant.parse("2026-09-29T09:00:00Z");
        Instant end = Instant.parse("2026-09-29T11:00:00Z");

        PredicateFilterStage filter =
                new PredicateFilterStage(
                        r -> !r.timestamp().isBefore(start)
                                && !r.timestamp().isAfter(end)
                );

        filter.process(
                createRecord(200, "/home", Instant.parse("2026-09-29T12:00:00Z")),
                emitter
        );

        assertTrue(emitter.isEmpty());
    }

    // 7. Tam sinir degeri 400 kabul edilmeli.
    @Test
    void statusExactly400ShouldPass() throws StageException {

        PredicateFilterStage filter =
                new PredicateFilterStage(r -> r.status() >= 400);

        filter.process(
                createRecord(400, "/bad-request", Instant.parse("2026-09-29T10:00:00Z")),
                emitter
        );

        assertEquals(1, emitter.size());
        assertEquals(400, emitter.getItems().get(0).status());
    }
}
