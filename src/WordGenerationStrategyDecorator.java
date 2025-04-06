import java.util.ArrayList;

public abstract class WordGenerationStrategyDecorator implements WordGenerationStrategy{
    private final WordGenerationStrategy baseStrategy;
    public WordGenerationStrategyDecorator(WordGenerationStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }
    public WordGenerationStrategy getBaseStrategy() {
        return baseStrategy;
    }
    public abstract String[] generateWords(ArrayList<String> dictionary);
}
