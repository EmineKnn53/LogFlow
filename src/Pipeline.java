import java.util.ArrayList;
import java.util.List;

public class Pipeline {

    private final Source<String> source;
    private final Sink<String> sink;
    private final List<Stage<String, String>> stages;

    public Pipeline(Source<String> source, Sink<String> sink) {
        this.source = source;
        this.sink = sink;
        this.stages = new ArrayList<>();
    }

    public Pipeline addStage(Stage<String, String> stage) {
        stages.add(stage);
        return this;
    }

    public void run() {

        for (Stage<String, String> stage : stages) {
            stage.open();
        }

        source.produce(item -> processStage(0, item));

        for (Stage<String, String> stage : stages) {
            stage.close();
        }
    }

    private void processStage(int index, String item) {

        if (index >= stages.size()) {
            sink.consume(item);
            return;
        }

        Stage<String, String> stage = stages.get(index);

        try {
            stage.process(item, nextItem ->
                processStage(index + 1, nextItem)
            );
        } catch (StageException e) {
            throw new RuntimeException(
                "Pipeline aşamasında hata oluştu.",
                e
            );
        }
    }
}