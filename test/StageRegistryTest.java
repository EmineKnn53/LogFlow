
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class StageRegistryTest {

    @Test
    void parserKaydedilipOlusturulmali() {
        StageRegistry registry = DefaultStageRegistry.create();

        Stage<?, ?> stage = registry.create(
            "parser", Map.of()
        );

        assertInstanceOf(ParserStage.class, stage);
    }

    @Test
    void filterKaydedilipOlusturulmali() {
        StageRegistry registry = DefaultStageRegistry.create();

        Stage<?, ?> stage = registry.create(
            "filter", Map.of("minStatus", 400)
        );

        assertInstanceOf(PredicateFilterStage.class, stage);
    }

    @Test
    void projectionKaydedilipOlusturulmali() {
        StageRegistry registry = DefaultStageRegistry.create();

        Stage<?, ?> stage = registry.create(
            "projection",
            Map.of("fields", java.util.List.of(
                "clientIp", "status"
            ))
        );

        assertInstanceOf(ProjectionStage.class, stage);
    }

    @Test
    void bilinmeyenStageReddedilmeli() {
        StageRegistry registry = DefaultStageRegistry.create();

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.create("bilinmeyen", Map.of())
        );
    }

    @Test
    void bosStageAdiReddedilmeli() {
        StageRegistry registry = new StageRegistry();

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.register(
                "", params -> new ParserStage()
            )
        );
    }

    @Test
    void eksikFilterParametresiReddedilmeli() {
        StageRegistry registry = DefaultStageRegistry.create();

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.create("filter", Map.of())
        );
    }

    @Test
    void gecersizProjectionAlaniReddedilmeli() {
        StageRegistry registry = DefaultStageRegistry.create();

        assertThrows(
            IllegalArgumentException.class,
            () -> registry.create(
                "projection",
                Map.of("fields", java.util.List.of(
                    "olmayanAlan"
                ))
            )
        );
    }
}
