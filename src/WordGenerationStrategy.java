import java.util.ArrayList;

/**
 * An interface representing a strategy for generating word pairs in the Weaver game.
 * This interface defines methods for:
 * - Generating an initial word and target word from a dictionary
 * - Optionally providing the solution path between those words
 */
public interface WordGenerationStrategy {

    /**
     * Generates a pair of words to be used as the starting point and target in the game.
     *
     * @param dictionary The list of valid words that can be used for generation
     * @return An array containing two words: [initialWord, targetWord]
     */
    String[] generateWords(ArrayList<String> dictionary);
    /**
     * Gets the solution path from the initial word to the target word, if available.
     *
     * @return An ArrayList of strings representing the transformation steps,
     *         or null if no path is available
     */
    ArrayList<String> getPath();
}
