
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FileSinkTest {

    @TempDir
    Path tempDir;

    // 1. Dosya acilmadan veri yazilamamali.
    @Test
    void consumeBeforeOpenShouldFail() {

        FileSink<String> sink = new FileSink<>(
                tempDir.resolve("output.log").toString()
        );

        assertThrows(
                IllegalStateException.class,
                () -> sink.consume("test kaydi")
        );
    }

    // 2. open() cagrildiginda dosya olusturulmali.
    @Test
    void openShouldCreateFile() {

        Path output = tempDir.resolve("output.log");

        FileSink<String> sink = new FileSink<>(
                output.toString()
        );

        try {
            sink.open();

            assertTrue(Files.exists(output));
        } finally {
            sink.close();
        }
    }

    // 3. Tek kayit dosyaya dogru yazilmali.
    @Test
    void singleRecordShouldBeWritten() throws IOException {

        Path output = tempDir.resolve("single.log");

        FileSink<String> sink = new FileSink<>(
                output.toString()
        );

        try {
            sink.open();
            sink.consume("ilk kayit");
        } finally {
            sink.close();
        }

        assertEquals(
                List.of("ilk kayit"),
                Files.readAllLines(output)
        );
    }

    // 4. Birden fazla kayit ayri satirlara yazilmali.
    @Test
    void multipleRecordsShouldBeWritten() throws IOException {

        Path output = tempDir.resolve("multiple.log");

        FileSink<String> sink = new FileSink<>(
                output.toString()
        );

        try {
            sink.open();
            sink.consume("birinci kayit");
            sink.consume("ikinci kayit");
            sink.consume("ucuncu kayit");
        } finally {
            sink.close();
        }

        assertEquals(
                List.of(
                        "birinci kayit",
                        "ikinci kayit",
                        "ucuncu kayit"
                ),
                Files.readAllLines(output)
        );
    }

    // 5. Map turundeki projeksiyon ciktisi yazilabilmeli.
    @Test
    void projectedMapShouldBeWritten() throws IOException {

        Path output = tempDir.resolve("projection.log");

        FileSink<Map<String, Object>> sink =
                new FileSink<>(output.toString());

        Map<String, Object> record = Map.of(
                "status", 403,
                "path", "/admin"
        );

        try {
            sink.open();
            sink.consume(record);
        } finally {
            sink.close();
        }

        String content = Files.readString(output);

        assertTrue(content.contains("status=403"));
        assertTrue(content.contains("path=/admin"));
    }

    // 6. close() sonrasi yeni kayit yazilamamali.
    @Test
    void consumeAfterCloseShouldFail() {

        Path output = tempDir.resolve("closed.log");

        FileSink<String> sink = new FileSink<>(
                output.toString()
        );

        sink.open();
        sink.close();

        assertThrows(
                IllegalStateException.class,
                () -> sink.consume("yeni kayit")
        );
    }

    // 7. close() birden fazla kez cagrilabilmeli.
    @Test
    void closeMultipleTimesShouldNotFail() {

        Path output = tempDir.resolve("close.log");

        FileSink<String> sink = new FileSink<>(
                output.toString()
        );

        sink.open();

        assertDoesNotThrow(() -> {
            sink.close();
            sink.close();
        });
    }
}
