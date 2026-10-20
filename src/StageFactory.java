
import java.util.Map;

@FunctionalInterface
public interface StageFactory {

    Stage<?, ?> create(Map<String, Object> parameters);

}
