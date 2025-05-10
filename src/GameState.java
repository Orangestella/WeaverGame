import java.util.ArrayList;

/**
 * Represents the current state of a game session.
 * Stores information about the player's path, validation results,
 * initial word, target word, and win status.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code initialWord} ≠ null ∧ not empty</li>
 *   <li>{@code targetWord} ≠ null ∧ not empty</li>
 *   <li>{@code path} is non-null and contains all words in the player's path</li>
 *   <li>{@code results} is non-null and contains validation results for each step</li>
 *   <li>{@code won} indicates whether the player has reached the target word</li>
 * </ul>
 */
public class GameState {
    private final ArrayList<String> path;
    private final boolean won;
    private final ArrayList<ValidationResult> results;
    private final String initialWord;
    private final String targetWord;

    /**
     * Constructs a new GameState instance.
     *
     * @pre.    initialWord ≠ null ∧ initialWord.length() > 0
     *          targetWord ≠ null ∧ targetWord.length() > 0
     *          path ≠ null
     *          results ≠ null
     * @post.   this.initialWord == initialWord ∧ this.targetWord == targetWord
     *          this.path is a copy of path (immutable from outside)
     *          this.results is a copy of results (immutable from outside)
     *          this.won == isWon
     *
     * @param initialWord The starting word of the game
     * @param targetWord The target word to be reached
     * @param path The list of words entered by the player
     * @param results The validation results for each step
     * @param isWon True if the player has won the game
     */
    public GameState(String initialWord, String targetWord, ArrayList<String> path, ArrayList<ValidationResult> results, boolean isWon) {
        this.path = new ArrayList<>(path);
        this.results = new ArrayList<>(results);
        this.initialWord = initialWord;
        this.targetWord = targetWord;
        this.won = isWon;
    }

    /**
     * Gets a copy of the player's path.
     *
     * @post.   returned list contains all words in the same order as path
     *          modifying the returned list does not affect this.path
     *
     * @return A new ArrayList containing the sequence of words entered by the player
     */
    public ArrayList<String> getPath() {
        return new ArrayList<>(path);
    }

    /**
     * Checks if the player has won the game.
     *
     * @post.   returns the current win status (true/false)
     *
     * @return True if the game has been won
     */
    public boolean isWon() {
        return won;
    }

    /**
     * Gets a copy of the validation results.
     *
     * @post.   returned list contains all validation results in the same order
     *          modifying the returned list does not affect this.results
     *
     * @return A new ArrayList containing validation results for each step
     */
    public ArrayList<ValidationResult> getResults() {
        return new ArrayList<>(results);
    }

    /**
     * Gets the initial word of the game.
     *
     * @post.   returns non-null, non-empty initial word
     *
     * @return The starting word
     */
    public String getInitialWord() {
        return initialWord;
    }

    /**
     * Gets the target word of the game.
     *
     * @post.   returns non-null, non-empty target word
     *
     * @return The word the player is trying to reach
     */
    public String getTargetWord() {
        return targetWord;
    }
}