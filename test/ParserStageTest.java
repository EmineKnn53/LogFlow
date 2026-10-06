import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ParserStageTest {

    private ParserStage parser;
    private CollectingEmitter<LogRecord> emitter;

    @BeforeEach
    void setUp() {
        parser = new ParserStage();
        emitter = new CollectingEmitter<>();
    }

    // 1. Gecerli bir CLF satiri basariyla parse edilmelidir.
    @Test
    void validLineShouldBeParsed() throws StageException {

        String line =
                "127.0.0.1 - - [29/Sep/2026:10:15:01] " +
                "\"GET /index.html HTTP/1.1\" 200 512";

        parser.process(line, emitter);

        assertEquals(1, emitter.size());
        assertEquals(0, parser.getErrorCount());

        LogRecord record = emitter.getItems().get(0);

        assertEquals("127.0.0.1", record.clientIp());
        assertEquals("GET", record.method());
        assertEquals("/index.html", record.path());
        assertEquals(200, record.status());
        assertEquals(512L, record.bytes());
    }

    // 2. Eksik alan iceren satir hatali kabul edilmelidir.
    @Test
    void missingFieldShouldBeRejected() throws StageException {

        String line =
                "127.0.0.1 - - [29/Sep/2026:10:15:01] " +
                "\"GET /index.html HTTP/1.1\" 200";

        parser.process(line, emitter);

        assertTrue(emitter.isEmpty());
        assertEquals(1, parser.getErrorCount());
    }

    // 3. Hatali timestamp iceren satir atlanmalidir.
    @Test
    void invalidTimestampShouldBeRejected() throws StageException {

        String line =
                "127.0.0.1 - - [HATALI-TARIH] " +
                "\"GET /index.html HTTP/1.1\" 200 512";

        parser.process(line, emitter);

        assertTrue(emitter.isEmpty());
        assertEquals(1, parser.getErrorCount());
    }

    // 4. Sayisal olmayan status degeri hatali kabul edilmelidir.
    @Test
    void invalidStatusShouldBeRejected() throws StageException {

        String line =
                "127.0.0.1 - - [29/Sep/2026:10:15:01] " +
                "\"GET /index.html HTTP/1.1\" ABC 512";

        parser.process(line, emitter);

        assertTrue(emitter.isEmpty());
        assertEquals(1, parser.getErrorCount());
    }

    // 5. Bos satir atlanmali ve hata sayisi artmalidir.
    @Test
    void emptyLineShouldBeRejected() throws StageException {

        parser.process("", emitter);

        assertTrue(emitter.isEmpty());
        assertEquals(1, parser.getErrorCount());
    }

    // 6. Fazladan bosluklar bulunan gecerli satir parse edilmelidir.
    @Test
    void extraSpacesShouldBeAccepted() throws StageException {

        String line =
                "127.0.0.1   -   -   [29/Sep/2026:10:15:01]   " +
                "\"GET /products HTTP/1.1\"   200   1024";

        parser.process(line, emitter);

        assertEquals(1, emitter.size());
        assertEquals(0, parser.getErrorCount());

        LogRecord record = emitter.getItems().get(0);

        assertEquals("GET", record.method());
        assertEquals("/products", record.path());
        assertEquals(200, record.status());
        assertEquals(1024L, record.bytes());
    }

    // 7. Bosluk iceren quoted User-Agent korunmalidir.
    @Test
    void quotedUserAgentWithSpacesShouldBeParsed()
            throws StageException {

        String line =
                "127.0.0.1 - - [29/Sep/2026:10:15:01] " +
                "\"GET /index.html HTTP/1.1\" 200 512 " +
                "\"Mozilla Firefox Test Agent\"";

        parser.process(line, emitter);

        assertEquals(1, emitter.size());
        assertEquals(0, parser.getErrorCount());

        LogRecord record = emitter.getItems().get(0);

        assertEquals(
                "Mozilla Firefox Test Agent",
                record.userAgent()
        );
    }

    // 8. Query string iceren path aynen korunmalidir.
    @Test
    void queryStringShouldBePreserved() throws StageException {

        String line =
                "127.0.0.1 - - [29/Sep/2026:10:15:01] " +
                "\"GET /search?q=java&page=2 HTTP/1.1\" 200 256";

        parser.process(line, emitter);

        assertEquals(1, emitter.size());
        assertEquals(0, parser.getErrorCount());

        LogRecord record = emitter.getItems().get(0);

        assertEquals(
                "/search?q=java&page=2",
                record.path()
        );
    }
}