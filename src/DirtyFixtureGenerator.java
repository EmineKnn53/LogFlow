
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirtyFixtureGenerator {

    private static final String DATE =
            "[29/Sep/2026:10:15:01]";

    public static void main(String[] args)
            throws Exception {

        Path folder = Path.of("data");

        Files.createDirectories(folder);

        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        // 1. Gecerli kayit
        add(bytes, line(
            "127.0.0.1",
            DATE,
            "/valid",
            "200",
            "512"
        ));

        // 2. Kesilmis log satiri
        add(bytes,
            "truncated - - [29/Sep/2026:10:15:01] \"GET"
        );

        // 3. Gecersiz UTF-8 bayti
        bytes.write(
            "127.0.0.1 - - ".getBytes(
                StandardCharsets.UTF_8
            )
        );

        bytes.write(0xFF);

        bytes.write(
            " invalid-utf8\n".getBytes(
                StandardCharsets.UTF_8
            )
        );

        // 4. Negatif byte degeri
        add(bytes, line(
            "127.0.0.1",
            DATE,
            "/negative",
            "200",
            "-10"
        ));

        // 5. 1970 tarihli kayit
        add(bytes, line(
            "127.0.0.1",
            "[01/Jan/1970:00:00:00]",
            "/old",
            "200",
            "15"
        ));

        // 6. 2099 tarihli kayit
        add(bytes, line(
            "127.0.0.1",
            "[01/Jan/2099:00:00:00]",
            "/future",
            "200",
            "15"
        ));

        // 7. Yaklasik 100 KB uzunlugunda satir
        add(bytes, line(
            "127.0.0.1",
            DATE,
            "/" + "x".repeat(102400),
            "200",
            "15"
        ));

        // 8. Gecersiz IP adresi
        add(bytes, line(
            "999.0.0.1",
            DATE,
            "/bad-ip",
            "200",
            "15"
        ));

        // 9. Ikinci gecerli kayit
        add(bytes, line(
            "127.0.0.2",
            DATE,
            "/second-valid",
            "404",
            "25"
        ));

        // Bozuk log dosyasini olustur
        Files.write(
            folder.resolve("access-dirty.log"),
            bytes.toByteArray()
        );

        // Bos dosya testini olustur
        Files.write(
            folder.resolve("access-empty.log"),
            new byte[0]
        );

        System.out.println(
            "Kaos testi dosyalari olusturuldu."
        );
    }

    private static String line(
            String ip,
            String time,
            String path,
            String status,
            String size) {

        return ip + " - - " + time
            + " \"GET " + path + " HTTP/1.1\" "
            + status + " " + size
            + " \"Test Agent\"";
    }

    private static void add(
            ByteArrayOutputStream output,
            String line) throws Exception {

        output.write(
            (line + "\n").getBytes(
                StandardCharsets.UTF_8
            )
        );
    }
}
