
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {

        if (args.length == 0) {
            printUsage();
            return;
        }

        try {

            // ARTIM 4: YAML ile pipeline calistirma
            if ("--config".equals(args[0])) {

                if (args.length != 2) {
                    throw new IllegalArgumentException(
                        "Kullanim: --config <yaml-dosyasi>"
                    );
                }

                String configFile = args[1];

                PipelineConfigBuilder configBuilder =
                    new PipelineConfigBuilder();

                Pipeline pipeline =
                    configBuilder.build(configFile);

                pipeline.run();

                System.out.println(
                    "YAML pipeline basariyla calisti: "
                    + configFile
                );

                return;
            }

            // ARTIM 3: Komut satiri parametreleri
            String inputFile = args[0];
            String outputFile = "output.log";
            Integer minStatus = null;
            String pathPrefix = null;

            String[] fields = {
                "timestamp",
                "clientIp",
                "method",
                "path",
                "status",
                "bytes"
            };

            for (int i = 1; i < args.length; i++) {

                switch (args[i]) {

                    case "--min-status":
                        minStatus = Integer.parseInt(
                            requireValue(args, ++i)
                        );
                        break;

                    case "--path-prefix":
                        pathPrefix = requireValue(args, ++i);
                        break;

                    case "--fields":
                        fields = Arrays.stream(
                            requireValue(args, ++i).split(",")
                        )
                        .map(String::trim)
                        .toArray(String[]::new);
                        break;

                    case "--output":
                        outputFile = requireValue(args, ++i);
                        break;

                    default:
                        throw new IllegalArgumentException(
                            "Bilinmeyen parametre: "
                            + args[i]
                        );
                }
            }

            // Kaynak ve parser
            ParserStage parser = new ParserStage();

            PipelineBuilder<LogRecord> builder =
                PipelineBuilder
                    .from(new FileLineSource(inputFile))
                    .via(parser);

            // HTTP durum koduna gore filtreleme
            if (minStatus != null) {

                final int threshold = minStatus;

                builder = builder.via(
                    new PredicateFilterStage(
                        r -> r.status() >= threshold
                    )
                );
            }

            // URL yoluna gore filtreleme
            if (pathPrefix != null) {

                final String prefix = pathPrefix;

                builder = builder.via(
                    new PredicateFilterStage(
                        r -> r.path().startsWith(prefix)
                    )
                );
            }

            // Alan secimi ve dosyaya yazma
            Pipeline pipeline = builder
                .via(new ProjectionStage(fields))
                .to(new FileSink<>(outputFile));

            pipeline.run();

            System.out.println(
                "Islem tamamlandi. Cikti: "
                + outputFile
            );

            System.out.println(
                "Toplam hatali satir sayisi: "
                + parser.getErrorCount()
            );

        } catch (IllegalArgumentException e) {

            System.err.println(
                "Parametre hatasi: " + e.getMessage()
            );

            printUsage();

        } catch (Exception e) {

            System.err.println(
                "Calistirma hatasi: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }

    private static String requireValue(
        String[] args,
        int index
    ) {

        if (index >= args.length
                || args[index].startsWith("--")) {

            throw new IllegalArgumentException(
                "Parametre degeri eksik."
            );
        }

        return args[index];
    }

    private static void printUsage() {

        System.out.println(
            "\nLOGFLOW - KULLANIM\n"
            + "\nARTIM 4 - YAML:\n"
            + "java Main --config configs/error-report.yaml\n"
            + "java Main --config configs/api-traffic.yaml\n"
            + "java Main --config configs/bandwidth-by-client.yaml\n"
            + "\nARTIM 3 - CLI:\n"
            + "java Main <log-dosyasi> "
            + "[--min-status 400] "
            + "[--path-prefix /api] "
            + "[--fields timestamp,clientIp,status] "
            + "[--output output.log]"
        );
    }
}
