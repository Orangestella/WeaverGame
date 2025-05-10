import java.util.ArrayList;

/**
 * A factory interface for creating word generation strategies.
 * This interface defines a single method that returns a {@link WordGenerationStrategy} object,
 * which is responsible for generating the initial and target words of the game.
 *
 * <p>Implementing classes can provide different strategies for word selection, such as:
 * <ul>
 *     <li>Selecting fixed words (e.g., from configuration or test cases)</li>
 *     <li>Selecting random words from a dictionary</li>
 *     <li>Selecting words based on difficulty levels</li>
 * </ul>
 *
 * @see FixedStrategyFactory for an example of a fixed word strategy.
 * @see RandomStrategyFactory for an example of a random word selection strategy.
 */
public interface StrategyFactory {

    /**
     * Creates and returns a new instance of a word generation strategy.
     *
     * @param dictionary The list of valid words that the strategy may use to generate words
     * @return A new instance of a class implementing {@link WordGenerationStrategy}
     */
    WordGenerationStrategy createStrategy(ArrayList<String> dictionary);
}