import java.util.ArrayList;
import java.util.List;

/**
 * Testlerde ParserStage tarafindan uretilen
 * LogRecord nesnelerini bellekte toplamak icin kullanilir.
 *
 * Dosya sistemine dokunmaz.
 */
public class CollectingEmitter<T> implements Emitter<T> {

    private final List<T> items = new ArrayList<>();

    @Override
    public void emit(T item) {
        items.add(item);
    }

    public List<T> getItems() {
        return items;
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}