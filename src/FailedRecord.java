import java.time.Instant;

public record FailedRecord(
        String rawInput,
        String stageName,
        String exception,
        Instant timestamp
) {

    @Override
    public String toString() {

        return "{\"timestamp\":\"" + timestamp
            + "\",\"stageName\":\"" + escape(stageName)
            + "\",\"exception\":\"" + escape(exception)
            + "\",\"rawInput\":\"" + escape(rawInput)
            + "\"}";
    }

    private static String escape(String s) {

        return String.valueOf(s)
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\uFFFD", "\\uFFFD");
    }
}
