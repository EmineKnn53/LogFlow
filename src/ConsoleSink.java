public class ConsoleSink implements Sink<LogRecord> {

    @Override
    public void consume(LogRecord record) {
        System.out.printf(
                "%s | %s | %s %s | Status: %d | Bytes: %d | User-Agent: %s%n",
                record.timestamp(),
                record.clientIp(),
                record.method(),
                record.path(),
                record.status(),
                record.bytes(),
                record.userAgent()
        );
    }
}