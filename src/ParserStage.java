import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ParserStage implements Stage<String, LogRecord> {

    private static final Pattern LOG_PATTERN = Pattern.compile(
            "^(\\S+)\\s+\\S+\\s+\\S+\\s+\\[([^\\]]+)]\\s+\"(\\S+)\\s+([^\\s\"]+)(?:\\s+[^\"]+)?\"\\s+(\\S+)\\s+(\\S+)(?:\\s+\"([^\"]*)\")?\\s*$"
    );

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "dd/MMM/uuuu:HH:mm:ss",
                    Locale.ENGLISH
            );

    private int errorCount = 0;

    private final boolean strict;

    public ParserStage() {
        this(false);
    }

    public ParserStage(boolean strict) {
        this.strict = strict;
    }

    @Override
    public void process(String input, Emitter<LogRecord> out)
            throws StageException {

        if (input == null || input.trim().isEmpty()) {
            reject("Bos log satiri", null);
            return;
        }

        if (input.indexOf('\uFFFD') >= 0) {
            reject("Gecersiz UTF-8 bayt dizisi", null);
            return;
        }

        Matcher matcher = LOG_PATTERN.matcher(input);

        if (!matcher.matches()) {
            reject("CLF bicimi gecersiz", null);
            return;
        }

        LogRecord record;

        try {

            String clientIp = matcher.group(1);

            LocalDateTime localDateTime = LocalDateTime.parse(
                    matcher.group(2),
                    DATE_FORMAT
            );

            Instant timestamp =
                    localDateTime.toInstant(ZoneOffset.UTC);

            String method = matcher.group(3);
            String path = matcher.group(4);

            int status = Integer.parseInt(
                    matcher.group(5)
            );

            String bytesText = matcher.group(6);

            long bytes;

            if ("-".equals(bytesText)) {
                bytes = 0L;
            } else {
                bytes = Long.parseLong(bytesText);
            }

            String userAgent = matcher.group(7);

            if (userAgent == null) {
                userAgent = "";
            }

            record = new LogRecord(
                    timestamp,
                    clientIp,
                    method,
                    path,
                    status,
                    bytes,
                    userAgent,
                    Collections.emptyMap(),
                    input
            );

        } catch (RuntimeException e) {
            reject("Log ayrıştırma hatasi", e);
            return;
        }

        out.emit(record);
    }

    private void reject(String reason, Throwable cause)
            throws StageException {

        errorCount++;

        if (strict) {
            throw new StageException(reason, cause);
        }
    }

    public int getErrorCount() {
        return errorCount;
    }
}