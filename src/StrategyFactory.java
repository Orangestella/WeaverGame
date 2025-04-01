import java.util.ArrayList;

public interface StrategyFactory {
    WordGenerationStrategy createStrategy(ArrayList<String> Dictionary);
}