public record ErrorPolicy(Mode mode, int attempts) {

    public enum Mode {
        FAIL_FAST,
        SKIP,
        DEAD_LETTER,
        RETRY
    }

    public static ErrorPolicy parse(Object value) {

        if (!(value instanceof String text)) {
            throw new IllegalArgumentException(
                "errorPolicy metin olmali"
            );
        }

        String normalized = text.trim()
                .toUpperCase(java.util.Locale.ROOT);

        if (normalized.matches("RETRY\\([1-9][0-9]*\\)")) {

            int n = Integer.parseInt(
                normalized.substring(
                    6,
                    normalized.length() - 1
                )
            );

            if (n > 100) {
                throw new IllegalArgumentException(
                    "RETRY en cok 100 olabilir"
                );
            }

            return new ErrorPolicy(Mode.RETRY, n);
        }

        try {
            return new ErrorPolicy(
                Mode.valueOf(normalized), 0
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                "Bilinmeyen errorPolicy: " + text, e
            );
        }
    }
}