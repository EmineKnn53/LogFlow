
public interface Sink<I> {

    // Pipeline'dan gelen veriyi tuketir.
    void consume(I item);

    // Sink calismaya baslamadan once cagrilir.
    // FileSink dosyayi burada acacak.
    default void open() {
    }

    // Sink islemi tamamlandiginda cagrilir.
    // FileSink dosyayi burada kapatacak.
    default void close() {
    }
}
