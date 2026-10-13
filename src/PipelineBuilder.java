
import java.util.ArrayList;
import java.util.List;

public class PipelineBuilder<T> {

    private final Source<?> source;
    private final List<Stage<?, ?>> stages;

    private PipelineBuilder(
            Source<?> source,
            List<Stage<?, ?>> stages) {

        this.source = source;
        this.stages = stages;
    }

    public static <T> PipelineBuilder<T> from(Source<T> source) {
        return new PipelineBuilder<>(
                source,
                new ArrayList<>()
        );
    }

    public <R> PipelineBuilder<R> via(Stage<T, R> stage) {
        List<Stage<?, ?>> updated = new ArrayList<>(stages);
        updated.add(stage);

        return new PipelineBuilder<>(source, updated);
    }

    public Pipeline to(Sink<T> sink) {
        return new Pipeline(source, stages, sink);
    }
}
