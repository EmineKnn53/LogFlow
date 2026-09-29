public class Main {

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Kullanim: java Main <log-dosyasi>");
            return;
        }

        Source<String> source = new FileLineSource(args[0]);
        Sink<String> sink = new ConsoleSink();

        Pipeline pipeline = new Pipeline(source, sink);

        pipeline.run();
    }
}