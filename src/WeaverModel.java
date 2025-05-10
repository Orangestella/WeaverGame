import exceptions.InvalidWordException;
import exceptions.WordGenerationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;

/**
 * The central game model for the Weaver game.
 * Manages game state, word validation, dictionary loading, and observer updates.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code dictionary} ≠ null ∧ contains only valid 4-letter words</li>
 *   <li>{@code initialWord} ≠ null ∧ is in dictionary ∧ length == targetWord.length()</li>
 *   <li>{@code targetWord} ≠ null ∧ is in dictionary ∧ length == initialWord.length()</li>
 *   <li>{@code currentPath} ≠ null ∧ starts with initialWord</li>
 *   <li>{@code resultsPath} size == currentPath.size() - 1 (each step has a result)</li>
 *   <li>{@code validator} ≠ null ∧ validates words against target using dictionary rules</li>
 *   <li>{@code strategyFactory} ≠ null ∧ generates valid word pairs</li>
 * </ul>
 */
public class WeaverModel extends Observable {

    // Game data
    private ArrayList<String> dictionary;
    private ArrayList<String> currentPath;
    private ArrayList<ValidationResult> resultsPath;
    private String initialWord;
    private String targetWord;
    private StrategyFactory strategyFactory;   // Factory for generating word pairs
    private WordGenerationStrategy wordGenerationStrategy; // Strategy for generating words
    private boolean isWon;
    private boolean showErrorsFlag = false; // Controls whether errors are shown
    private boolean showPathFlag = false; // Controls whether solution path is shown
    private boolean randomWordFlag = false;
    private WordValidator validator;
    private WordValidator baseValidator;

    /**
     * Constructs a new WeaverModel.
     * Loads dictionary and initializes validators and strategies based on default flags.
     *
     * @pre.    Dictionary file exists and contains at least two 4-letter words
     *          BaseValidator can be initialized without error
     * @post.   dictionary is loaded with valid 4-letter words
     *          baseValidator is initialized
     *          strategyFactory is set to FixedStrategyFactory by default
     *          randomWordFlag == false
     *          showErrorsFlag == false
     *          game is not won
     *
     * @throws IOException if dictionary cannot be loaded
     */
    public WeaverModel() throws IOException {
        loadDictionary();
        this.baseValidator = new BasicValidator();
        updateValidator();
        updateStrategy();
    }

    /**
     * Loads 4-letter words from dictionary.txt and converts them to uppercase.
     *
     * @pre.    dictionary.txt exists in resources folder
     *          file contains lines of text (some possibly not 4 letters)
     * @post.   dictionary contains only uppercase 4-letter words from file
     *          if fewer than 2 words: IOException is thrown
     *
     * @throws IOException if dictionary file cannot be read or not found
     */
    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(
                        getClass().getResourceAsStream("/dictionary.txt"),
                        "Dictionary file not found in classpath: /dictionary.txt"
                ))
        )) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    dictionary.add(line.toUpperCase());
                }
            }
        }

        if (dictionary == null || dictionary.size() < 2) {
            throw new IOException("Dictionary does not contain enough 4-letter words (requires at least 2).");
        }
    }

    /**
     * Initializes or resets the game with potentially new words.
     * Selects initial and target words based on the randomWordFlag,
     * clears the current path and results. Notifies observers (for GUI).
     *
     * @pre.    wordGenerationStrategy ≠ null
     *          dictionary contains both generated words
     * @post.   initialWord and targetWord are valid 4-letter words from dictionary
     *          currentPath starts with initialWord
     *          resultsPath is empty
     *          isWon == false
     *          observers are notified with start message
     *
     * @throws WordGenerationException if strategy fails to generate valid word pair
     */
    public void initialize() throws WordGenerationException {
        updateStrategy();
        updateValidator();

        String[] words = wordGenerationStrategy.generateWords(dictionary);
        this.initialWord = words[0];
        this.targetWord = words[1];
        this.isWon = false;

        currentPath = new ArrayList<>();
        currentPath.add(initialWord);

        resultsPath = new ArrayList<>();

        notifyUpdate("Game started. Enter your first word.", null);
    }

    /**
     * Processes a player's word input, updates game state, and returns validation result.
     *
     * @pre.    word ≠ null ∧ word.length() == targetWord.length()
     *          last word in currentPath is valid (i.e., not empty)
     *          dictionary contains all valid words
     * @post.   if valid move: word is added to currentPath
     *          result is added to resultsPath
     *          isWon reflects whether word == targetWord
     *          observers are notified with appropriate message
     *
     * @param word The word entered by the player
     * @return ValidationResult containing letter states and optional message
     * @throws InvalidWordException if word is invalid (not in dict or length mismatch)
     * @throws RuntimeException for unexpected errors during processing
     */
    public ValidationResult tick(String word) throws InvalidWordException, RuntimeException {
        word = word.toUpperCase();
        ArrayList<String> nextPath = new ArrayList<>(currentPath);
        ArrayList<ValidationResult> nextResultsPath = new ArrayList<>(resultsPath);
        ValidationResult result = null;

        try {
            result = validator.validate(word, this.targetWord, this.dictionary);

            if (!nextPath.isEmpty()) {
                String lastWord = nextPath.get(nextPath.size() - 1);
                if (!isOneLetterDifferent(lastWord, word)) {
                    throw new InvalidWordException("Word must differ by exactly one letter from the previous word.");
                }
            } else {
                throw new InvalidWordException("Game state error: Path is empty before the first player input step.");
            }

            nextPath.add(word);
            nextResultsPath.add(result);
            this.currentPath = nextPath;
            this.resultsPath = nextResultsPath;
            this.isWon = result.getValid();

            String message = result.getMessage();
            String runtimeWarning = null;

            if (this.isWon && (message == null || message.isEmpty() || !message.equalsIgnoreCase("You won the game!"))) {
                message = "You won the game!";
            }

            notifyUpdate(message, runtimeWarning);
            return result;

        } catch (InvalidWordException e) {
            this.isWon = false;
            String message = e.getMessage();
            String runtimeWarning = null;
            notifyUpdate(message, runtimeWarning);
            throw e;

        } catch (RuntimeException e) {
            this.isWon = false;
            String message = null;
            String runtimeWarning = "An unexpected error occurred during tick: " + e.getMessage();
            System.err.println(runtimeWarning);
            notifyUpdate(message, runtimeWarning);
            throw e;
        }
    }

    /**
     * Checks if two words of equal length differ by exactly one letter.
     *
     * @pre.    word1 ≠ null ∧ word2 ≠ null
     *          word1.length() == word2.length()
     * @post.   returns true iff exactly one character differs between the words
     *
     * @param word1 First word
     * @param word2 Second word
     * @return true if words differ by exactly one letter
     */
    private boolean isOneLetterDifferent(String word1, String word2) {
        if (word1 == null || word2 == null || word1.length() != word2.length()) {
            return false;
        }

        int diffCount = 0;
        for (int i = 0; i < word1.length(); i++) {
            if (word1.charAt(i) != word2.charAt(i)) {
                diffCount++;
            }
        }
        return diffCount == 1;
    }

    /**
     * Notifies observers about current game state.
     * Conditionally sets hint and warning messages based on showErrorsFlag.
     *
     * @pre.    currentState ≠ null
     *          messageToSend may be null
     *          warningToSend may be null
     * @post.   observers are notified with Notification object containing:
     *          - gameState
     *          - messageToSend (based on showErrorsFlag)
     *          - warningToSend (if any)
     *
     * @param hint         Optional hint message
     * @param runtimeWarning Optional warning message
     */
    private void notifyUpdate(String hint, String runtimeWarning) {
        setChanged();
        GameState currentState = new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath, this.isWon);

        String messageToSend = null;
        String warningToSend = null;

        if (this.showErrorsFlag) {
            warningToSend = runtimeWarning;
            if (hint != null && !hint.isEmpty()) {
                messageToSend = hint;
            } else if (this.isWon) {
                messageToSend = "You won the game!";
            } else {
                messageToSend = "Continue playing.";
            }
        }

        Notification notification = new Notification(currentState, messageToSend, warningToSend);
        notifyObservers(notification);
    }

    /**
     * Triggers a notification to observers with current game state.
     * Used when view needs updating without player input.
     *
     * @pre.    none
     * @post.   observers are notified with current game state
     *          no custom message or warning is sent
     */
    public void notifyObserversWithCurrentState() {
        notifyUpdate(null, null);
    }

    /**
     * Gets the current value of showErrorsFlag.
     * @return true if showing errors is enabled
     */
    public boolean isShowErrorsFlag() {
        return showErrorsFlag;
    }

    /**
     * Sets the show errors flag and updates validator accordingly.
     * Notifies observers for GUI update.
     *
     * @pre.    showErrorsFlag is either true or false
     * @post.   this.showErrorsFlag == showErrorsFlag
     *          validator is updated based on new setting
     *          observers are notified
     *
     * @param showErrorsFlag New value for the flag
     */
    public void setShowErrorsFlag(boolean showErrorsFlag) {
        if (this.showErrorsFlag != showErrorsFlag) {
            this.showErrorsFlag = showErrorsFlag;
            updateValidator();
            notifyObserversWithCurrentState();
        }
    }

    /**
     * Gets the current value of randomWordFlag.
     * @return true if using random word generation
     */
    public boolean isShowPathFlag() {
        return showPathFlag;
    }

    /**
     * Sets the show path flag.
     * This flag controls whether the solution path window is displayed.
     *
     * @param showPathFlag New value for the flag
     */
    public void setShowPathFlag(boolean showPathFlag) {
        this.showPathFlag = showPathFlag;
    }

    /**
     * Gets the current value of randomWordFlag.
     * @return true if using random word generation
     */
    public boolean isRandomWordFlag() {
        return randomWordFlag;
    }

    /**
     * Sets the random word flag and updates strategy accordingly.
     *
     * @pre.    randomWordFlag is either true or false
     * @post.   this.randomWordFlag == randomWordFlag
     *          strategy is updated (with or without WithPath decorator)
     *          if dictionary is insufficient, uses fixed fallback strategy
     *
     * @param randomWordFlag New value for the flag
     */
    public void setRandomWordFlag(boolean randomWordFlag) {
        if (this.randomWordFlag != randomWordFlag) {
            this.randomWordFlag = randomWordFlag;
            updateStrategy();
        }
    }

    /**
     * Updates the word generation strategy based on current flags.
     * Wraps with WithPath decorator if needed.
     *
     * @pre.    dictionary is non-null and sufficient for fixed strategy
     * @post.   wordGenerationStrategy is updated with correct factory
     *          if randomWordFlag is true: uses RandomStrategyFactory
     *          if randomWordFlag is false: uses FixedStrategyFactory with default words
     */
    public void updateStrategy() {
        StrategyFactory factory;
        if (randomWordFlag) {
            factory = new RandomStrategyFactory();
        } else {
            if (dictionary == null || dictionary.size() < 2) {
                System.err.println("Error: Dictionary not loaded or insufficient words for fixed strategy.");
                factory = new FixedStrategyFactory("EAST", "WEST");
            } else {
                factory = new FixedStrategyFactory("EAST", "WEST");
            }
        }

        WordGenerationStrategy base = factory.createStrategy(dictionary);
        this.wordGenerationStrategy = randomWordFlag ? new WithPath(base) : base;
    }

    /**
     * Updates the validator based on current flags.
     * Applies WithWarning decorator if showErrorsFlag is enabled.
     *
     * @pre.    baseValidator is not null
     * @post.   validator is either baseValidator or WithWarning wrapper
     *          depending on showErrorsFlag
     */
    public void updateValidator() {
        this.validator = this.baseValidator;
        if (showErrorsFlag) {
            this.validator = new WithWarning(this.validator);
        }
    }

    /**
     * Resets game state, keeping the same initial and target words.
     * Clears the path and notifies observers.
     *
     * @pre.    initialWord and targetWord are valid and non-null
     * @post.   currentPath contains only initialWord
     *          resultsPath is cleared
     *          isWon == false
     *          observers are notified with reset message
     */
    public void resetGame() {
        this.isWon = false;
        currentPath = new ArrayList<>();
        currentPath.add(initialWord);
        resultsPath = new ArrayList<>();
        notifyUpdate("Game reset. Enter your first word.", null);
    }

    /**
     * Gets the initial word of the game.
     * @return The starting word
     */
    public String getInitialWord() {
        return initialWord;
    }

    /**
     * Gets the target word of the game.
     * @return The goal word
     */
    public String getTargetWord() {
        return targetWord;
    }

    /**
     * Gets the player's current word path.
     * Returns a copy to prevent external modification.
     *
     * @return A list of words in the current path
     */
    public ArrayList<String> getCurrentPath() {
        return new ArrayList<>(currentPath);
    }

    /**
     * Gets validation results for each step in the path.
     * Returns a copy to prevent external modification.
     *
     * @return List of ValidationResult objects
     */
    public ArrayList<ValidationResult> getResultsPath() {
        return new ArrayList<>(resultsPath);
    }

    /**
     * Checks if the game is currently won.
     * @return true if player reached the target word
     */
    public boolean isWon() {
        return isWon;
    }

    /**
     * Gets the dictionary used in the game.
     * Returns a copy to prevent external modification.
     *
     * @return The list of valid words
     */
    public ArrayList<String> getDictionary() {
        return new ArrayList<>(dictionary);
    }

    /**
     * Gets the full solution path from initial to target word.
     * Uses PathFinder.findPathByBFS internally.
     *
     * @post.   returned path contains all steps from initial to target
     *          or null if no path exists
     *
     * @return Full solution path as an ArrayList
     */
    public ArrayList<String> getFullSolutionPath() {
        return PathFinder.findPathByBFS(this.initialWord, this.targetWord, this.dictionary);
    }
}