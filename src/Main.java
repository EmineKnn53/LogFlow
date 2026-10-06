public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Kullanim: java Main <log-dosyasi>");
            return;
        }

        Source<String> source = new FileLineSource(args[0]);

        ParserStage parser = new ParserStage();

        Sink<LogRecord> sink = new ConsoleSink();

        Pipeline pipeline = new Pipeline(source, parser, sink);

        pipeline.run();

        System.out.println(
                "Toplam hatali satir sayisi: " + parser.getErrorCount()
        );
    }
}