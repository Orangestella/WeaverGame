import java.util.ArrayList;

/**
 * A factory class that creates a fixed word strategy for generating initial and target words.
 * This strategy ensures that the same fixed words are used in every game session.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code initialWord} ≠ null ∧ {@code initialWord} is not empty</li>
 *   <li>{@code targetWord} ≠ null ∧ {@code targetWord} is not empty</li>
 *   <li>{@code dictionary} passed to createStrategy must contain both words</li>
 * </ul>
 */
public class FixedStrategyFactory implements StrategyFactory {
    private final String initialWord;
    private final String targetWord;

    /**
     * Creates a new FixedWordStrategy instance using the provided dictionary.
     *
     * @pre.    dictionary ≠ null
     *          dictionary contains both initialWord and targetWord
     * @post.   returns a FixedWordStrategy with:
     *          - initialWord = this.initialWord
     *          - targetWord = this.targetWord
     *          - dictionary = dictionary
     *
     * @param dictionary The dictionary of valid words to be used by the strategy.
     * @return A new FixedWordStrategy instance initialized with the fixed initial and target words.
     */
    @Override
    public WordGenerationStrategy createStrategy(ArrayList<String> dictionary) {
        return new FixedWordStrategy(initialWord, targetWord, dictionary);
    }

    /**
     * Constructs a FixedStrategyFactory with the specified initial and target words.
     *
     * @pre.    initialWord ≠ null ∧ initialWord is not empty
     *          targetWord ≠ null ∧ targetWord is not empty
     * @post.   this.initialWord == initialWord
     *          this.targetWord == targetWord
     *          IllegalArgumentException is thrown if preconditions fail
     *
     * @param initialWord The fixed initial word for the game.
     * @param targetWord  The fixed target word for the game.
     * @throws IllegalArgumentException if either initialWord or targetWord is null or empty.
     */
    public FixedStrategyFactory(String initialWord, String targetWord) {
        if (initialWord == null || initialWord.isEmpty() || targetWord == null || targetWord.isEmpty()) {
            throw new IllegalArgumentException("The initial word or target word cannot be empty");
        }
        this.initialWord = initialWord;
        this.targetWord = targetWord;
    }
}