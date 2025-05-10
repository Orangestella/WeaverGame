import java.util.ArrayList;

/**
 * A base class for decorating a word generation strategy.
 * This abstract class wraps another {@link WordGenerationStrategy} and allows subclasses
 * to modify or enhance its behavior.
 *
 * <p>Subclasses should override the {@link #generateWords(ArrayList)} method
 * to provide extended functionality.</p>
 */
public abstract class WordGenerationStrategyDecorator implements WordGenerationStrategy {

    // The underlying word generation strategy being decorated
    private final WordGenerationStrategy baseStrategy;

    /**
     * Constructs a new decorator that wraps the given base strategy.
     *
     * @param baseStrategy The strategy to be decorated
     */
    public WordGenerationStrategyDecorator(WordGenerationStrategy baseStrategy) {
        this.baseStrategy = baseStrategy;
    }

    /**
     * Gets the base word generation strategy wrapped by this decorator.
     *
     * @return The original word generation strategy
     */
    public WordGenerationStrategy getBaseStrategy() {
        return baseStrategy;
    }

    /**
     * Generates a pair of words based on the base strategy's implementation.
     * Subclasses may extend or alter this behavior.
     *
     * @param dictionary The list of valid words used for generation
     * @return An array containing [initialWord, targetWord]
     */
    @Override
    public abstract String[] generateWords(ArrayList<String> dictionary);
}