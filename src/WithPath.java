import exceptions.WordGenerationException;
import java.util.ArrayList;

/**
 * A decorator for WordGenerationStrategy that ensures a valid transformation path exists
 * between the generated start word and target word.
 *
 * <p>This class wraps another word generation strategy (e.g., RandomWordStrategy) and attempts to find
 * a valid transformation path using BFS. If no path is found after multiple attempts, it throws an exception.</p>
 */
public class WithPath extends WordGenerationStrategyDecorator {

    // Stores the solution path from start word to target word
    private ArrayList<String> path;

    /**
     * Constructs a new WithPath decorator that wraps the given base strategy.
     *
     * @param baseStrategy The underlying word generation strategy to decorate
     */
    public WithPath(WordGenerationStrategy baseStrategy) {
        super(baseStrategy);
    }

    /**
     * Generates a pair of words [startWord, targetWord] such that a valid transformation path exists.
     * Tries up to 20 times to generate such a pair before throwing an exception.
     *
     * @param dictionary The list of valid words used for validation
     * @return An array containing [startWord, targetWord] with a valid path
     * @throws WordGenerationException if no valid path can be found after max attempts
     */
    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        int maxAttempt = 20;
        for (int i = 0; i < maxAttempt; i++) {
            String[] wordsPair = this.getBaseStrategy().generateWords(dictionary);
            this.path = PathFinder.findPathByBFS(wordsPair[0], wordsPair[1], dictionary);

            if (!path.isEmpty()) {
                return wordsPair;
            }
        }
        throw new WordGenerationException("No path found between any generated word pairs.");
    }

    /**
     * Gets the solution path found during word generation.
     * Returns an empty list if no path was found.
     *
     * @return An ArrayList of strings representing the transformation steps
     */
    public ArrayList<String> getPath() {
        return (this.path != null) ? this.path : new ArrayList<>();
    }
}