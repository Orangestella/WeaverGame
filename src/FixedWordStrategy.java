import java.util.ArrayList;

/**
 * A word generation strategy that provides fixed initial and target words.
 * This strategy is useful for testing or scenarios where consistent word pairs are required.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code initial} ≠ null ∧ {@code initial} is not empty</li>
 *   <li>{@code target} ≠ null ∧ {@code target} is not empty</li>
 *   <li>{@code dictionary} is not null (though not used internally)</li>
 * </ul>
 */
public class FixedWordStrategy implements WordGenerationStrategy {
    private final String initial;
    private final String target;
    private final ArrayList<String> dictionary;

    /**
     * Constructs a FixedWordStrategy with specified initial and target words.
     *
     * @pre.    start ≠ null ∧ start.length() > 0
     *          target ≠ null ∧ target.length() > 0
     *          dictionary ≠ null
     * @post.   this.initial == start ∧ this.target == target
     *          this.dictionary == dictionary
     *
     * @param start The fixed initial word
     * @param target The fixed target word
     * @param dictionary The dictionary of valid words (stored but not used)
     */
    public FixedWordStrategy(String start, String target, ArrayList<String> dictionary) {
        this.initial = start;
        this.target = target;
        this.dictionary = dictionary;
    }

    /**
     * Returns the fixed word pair [initial, target].
     *
     * @pre.    dictionary ≠ null (ignored in this implementation)
     * @post.   returned array has length 2: [initial, target]
     *          initial and target are non-null and non-empty
     *
     * @param dictionary Ignored in this implementation
     * @return An array containing the fixed initial and target words
     */
    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        return new String[]{initial, target};
    }

    /**
     * Gets the solution path (not implemented in this strategy).
     *
     * @pre.    none
     * @post.   returns null (no solution path is available)
     *
     * @return Always returns null as this strategy does not provide a solution path
     */
    @Override
    public ArrayList<String> getPath() {
        return null;
    }
}