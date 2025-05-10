import java.util.ArrayList;

/**
 * A factory class that creates a word generation strategy based on randomness.
 * This factory produces instances of {@link RandomWordStrategy}, which selects
 * start and target words randomly from the provided dictionary.
 */
public class RandomStrategyFactory implements StrategyFactory {

    /**
     * Creates and returns a new instance of {@link RandomWordStrategy}.
     * The strategy uses the provided dictionary to randomly select valid start
     * and target words for the game.
     *
     * @param dictionary The list of valid words used by the strategy to choose from
     * @return A new instance of {@link WordGenerationStrategy} that uses random word selection
     */
    @Override
    public WordGenerationStrategy createStrategy(ArrayList<String> dictionary) {
        return new RandomWordStrategy(dictionary);
    }
}