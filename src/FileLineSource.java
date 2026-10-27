
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;
import java.nio.charset.CodingErrorAction;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileLineSource implements Source<String> {

    private final String filePath;

    public FileLineSource(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void produce(Emitter<String> out) {

        try (BufferedReader reader =
                new BufferedReader(
                    new InputStreamReader(
                        Files.newInputStream(Path.of(filePath)),
                        StandardCharsets.UTF_8
                            .newDecoder()
                            .onMalformedInput(
                                CodingErrorAction.REPLACE
                            )
                            .onUnmappableCharacter(
                                CodingErrorAction.REPLACE
                            )
                    )
                )) {

            String line;

            while ((line = reader.readLine()) != null) {
                out.emit(line);
            }

        } catch (IOException e) {

            throw new PipelineException(
                "Dosya okunurken hata olustu: "
                    + filePath,
                e
            );
        }
    }
}
