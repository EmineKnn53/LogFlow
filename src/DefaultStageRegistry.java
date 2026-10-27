
import java.util.List;

public class DefaultStageRegistry {

    public static StageRegistry create() {

        StageRegistry registry = new StageRegistry();

        // ARTIM 2: Log satirlarini ayrıştırma
        // ARTIM 5: Hatalari StageException ile bildirme
        registry.register("parser", params -> {
            return new ParserStage(true);
        });

        // ARTIM 5: Kayit dogrulama
        registry.register("validation", params -> {
            return new ValidationStage(params);
        });

        // ARTIM 3-4: HTTP durumu ve URL yoluna gore filtreleme
        registry.register("filter", params -> {

            Object minStatus = params.get("minStatus");
            Object pathPrefix = params.get("pathPrefix");

            if (minStatus == null && pathPrefix == null) {
                throw new IllegalArgumentException(
                    "Filter icin minStatus veya pathPrefix gerekli."
                );
            }

            Integer status = null;
            String prefix = null;

            if (minStatus != null) {

                if (!(minStatus instanceof Number)) {
                    throw new IllegalArgumentException(
                        "minStatus sayisal olmalidir."
                    );
                }

                status = ((Number) minStatus).intValue();
            }

            if (pathPrefix != null) {

                if (!(pathPrefix instanceof String)
                        || ((String) pathPrefix).isBlank()) {

                    throw new IllegalArgumentException(
                        "pathPrefix bos olmayan bir metin olmalidir."
                    );
                }

                prefix = (String) pathPrefix;
            }

            final Integer requiredStatus = status;
            final String requiredPrefix = prefix;

            return new PredicateFilterStage(record ->
                (requiredStatus == null
                    || record.status() >= requiredStatus)
                &&
                (requiredPrefix == null
                    || record.path().startsWith(requiredPrefix))
            );
        });

        // ARTIM 3-4: Istenen alanlari secme
        registry.register("projection", params -> {

            Object fieldsValue = params.get("fields");

            if (!(fieldsValue instanceof List<?> fields)
                    || fields.isEmpty()) {

                throw new IllegalArgumentException(
                    "Projection icin fields listesi gerekli."
                );
            }

            for (Object field : fields) {

                if (!(field instanceof String)) {

                    throw new IllegalArgumentException(
                        "Tum fields elemanlari metin olmalidir."
                    );
                }
            }

            String[] selectedFields = fields.stream()
                .map(String.class::cast)
                .toArray(String[]::new);

            return new ProjectionStage(selectedFields);
        });

        return registry;
    }
}
