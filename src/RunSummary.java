import java.util.LinkedHashMap;
import java.util.Map;

public class RunSummary {

    private long incoming;
    private long outgoing;
    private long deadLetters;
    private long filtered;
    private long elapsedNanos;

    private final Map<String, Long> skipped =
            new LinkedHashMap<>();

    public long incoming() {
        return incoming;
    }

    public long outgoing() {
        return outgoing;
    }

    public long deadLetters() {
        return deadLetters;
    }

    public long filtered() {
        return filtered;
    }

    public long elapsedNanos() {
        return elapsedNanos;
    }

    public Map<String, Long> skippedByStage() {
        return Map.copyOf(skipped);
    }

    void incomingOne() {
        incoming++;
    }

    void outgoingOne() {
        outgoing++;
    }

    void deadLetterOne() {
        deadLetters++;
    }

    void filteredOne() {
        filtered++;
    }

    void skippedOne(String name) {
        skipped.merge(name, 1L, Long::sum);
    }

    void elapsed(long nanos) {
        elapsedNanos = nanos;
    }

    public void print() {

        System.out.println("=== RunSummary ===");

        System.out.println(
            "Gelen: " + incoming +
            " | Cikan: " + outgoing
        );

        System.out.println(
            "Atlanan (asama): " + skipped +
            " | Filtrelenen: " + filtered
        );

        System.out.println(
            "Dead-letter: " + deadLetters
        );

        System.out.printf(
            java.util.Locale.ROOT,
            "Sure: %.3f ms%n",
            elapsedNanos / 1_000_000.0
        );
    }
}