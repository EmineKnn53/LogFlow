
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FileSink<T> implements Sink<T> {

    private final Path path;
    private BufferedWriter writer;

    public FileSink(String filename) {
        this.path = Path.of(filename);
    }

    @Override
    public void open() {
        try {
            writer = Files.newBufferedWriter(path);
        } catch (IOException e) {
            throw new RuntimeException("Dosya acilamadi: " + path, e);
        }
    }

    @Override
    public void consume(T item) {
        if (writer == null) {
            throw new IllegalStateException("FileSink acilmadi.");
        }
        try {
            writer.write(String.valueOf(item));
            writer.newLine();
        } catch (IOException e) {
            throw new RuntimeException("Dosyaya yazma hatasi.", e);
        }
    }

    @Override
    public void close() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Dosya kapatilamadi.", e);
            } finally {
                writer = null;
            }
        }
    }
}
