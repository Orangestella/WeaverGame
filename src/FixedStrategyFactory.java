import java.util.ArrayList;

public class FixedStrategyFactory implements StrategyFactory{
    private final String initialWord;
    private final String targetWord;
    @Override
    public WordGenerationStrategy createStrategy(ArrayList<String> dictionary) {
        return new FixedWordStrategy(initialWord, targetWord, dictionary);
    }
    public FixedStrategyFactory(String initialWord, String targetWord) {
        if (initialWord == null || initialWord.isEmpty() || targetWord == null || targetWord.isEmpty()) {
            throw new IllegalArgumentException("The initial word or target word cannot be empty");
        }
        this.initialWord = initialWord;
        this.targetWord = targetWord;

    }
}
