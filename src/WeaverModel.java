import exceptions.InvalidWordException; // Assuming this exception class exists
import exceptions.WordGenerationException; // Assuming this exception class exists

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;
import java.util.Set; // Added import
import java.util.HashSet; // Added import


// Ensure these classes are in the correct package or imported correctly
// import your_package_name.GameState;
// import your_package_name.ValidationResult;
// import your_package_name.LetterState; // If needed directly in Model, though likely not
// import your_package_name.Notification;
// import your_package_name.PathFinder;
// import your_package_name.StrategyFactory;
// import your_package_name.FixedStrategyFactory;
// import your_package_name.RandomStrategyFactory;
// import your_package_name.WordGenerationStrategy;
// import your_package_name.WithPath;
// import your_package_name.WordValidator;
// import your_package_name.BasicValidator;
// import your_package_name.WithWarning;


public class WeaverModel extends Observable {
    private ArrayList<String> dictionary;
    private ArrayList<String> currentPath;
    private ArrayList<ValidationResult> resultsPath;
    private String initialWord;
    private String targetWord;
    private StrategyFactory strategyFactory;
    private WordGenerationStrategy wordGenerationStrategy;
    private boolean isWon;

    private boolean showErrorsFlag = false;
    private boolean showPathFlag = false; // Default to not showing path
    private boolean randomWordFlag = false; // Default to fixed words

    private WordValidator validator;


    /**
     * Constructs a new WeaverModel.
     * Loads the dictionary and initializes validator and strategy based on default flags.
     *
     * @throws IOException if the dictionary file cannot be loaded.
     */
    public WeaverModel() throws IOException {
        loadDictionary();
        // Initial setup of validator and strategy based on default flags
        updateValidator();
        updateStrategy();
        // Game will be initialized later, e.g., by the Controller in GUIMain or CLIMain
        // Initialization will set the initial state and notify observers.
    }

    /**
     * Loads the dictionary words from the dictionary.txt file.
     * Only loads 4-letter words and converts them to uppercase.
     *
     * @throws IOException if the dictionary file cannot be read or is not found.
     */
    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        // Use getResourceAsStream with a path relative to the classpath root
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/dictionary.txt"),
                        "Dictionary file not found in classpath: /dictionary.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    dictionary.add(line.toUpperCase());
                }
            }
        }
        // Optional: Add an assertion here to check if the dictionary is loaded and has enough words
        // assert dictionary != null && dictionary.size() >= 2 : "Dictionary must contain at least two 4-letter words.";
        if (dictionary == null || dictionary.size() < 2) {
            throw new IOException("Dictionary does not contain enough 4-letter words (requires at least 2).");
        }
    }

    /**
     * Initializes or resets the game.
     * Selects initial and target words based on the randomWordFlag,
     * clears the current path and results, and notifies observers.
     */
    public void initialize() {
        // Update strategy and validator based on current flags
        updateStrategy();
        updateValidator();

        // Generate initial and target words using the current strategy
        String[] words;
        try {
            words = wordGenerationStrategy.generateWords(dictionary);
        } catch (WordGenerationException e) {
            // Handle case where strategy cannot generate words (e.g., no path found by WithPath in constructor)
            String runtimeWarning = "Could not generate initial/target words: " + e.getMessage();
            System.err.println(runtimeWarning);
            // Set default words or keep previous, and notify with an error message.
            if (this.initialWord == null || this.targetWord == null) {
                // If this is the very first initialization and it fails
                this.initialWord = "####"; // Indicate error state
                this.targetWord = "####";
                this.isWon = false;
                currentPath = new ArrayList<>();
                currentPath.add(initialWord);
                resultsPath = new ArrayList<>();
                notifyUpdate("Initialization failed.", runtimeWarning);
                return; // Stop initialization process
            } else {
                // If re-initialization fails, keep previous words and notify error
                notifyUpdate("Could not generate new words. Keeping previous.", runtimeWarning);
                // Re-initialize path and results for the previous words
                this.isWon = false;
                currentPath = new ArrayList<>();
                currentPath.add(initialWord);
                resultsPath = new ArrayList<>();
                return; // Stop initialization process
            }
        }


        this.initialWord = words[0];
        this.targetWord = words[1];

        this.isWon = false;
        currentPath = new ArrayList<>(); // Re-initialize the path
        currentPath.add(initialWord);
        resultsPath = new ArrayList<>(); // Re-initialize the results path

        // Notify observers about the initial game state
        // notifyUpdate will decide whether to show player path or full path based on showPathFlag
        notifyUpdate("Game started. Enter your first word.", null); // Initial message
    }

    /**
     * Processes a player's word input.
     * Validates the word, updates the game state (player's path and results),
     * and notifies observers.
     * The notification will display either the player's progress or the full path
     * based on the showPathFlag.
     *
     * @param word The word entered by the player.
     */
    public void tick(String word) {
        // Update strategy and validator before processing the tick,
        // in case flags were changed before input (e.g., in GUI via checkboxes)
        // These updates might be redundant if setters already call them, but safer here.
        updateStrategy();
        updateValidator();

        word = word.toUpperCase();
        String message = null;
        String runtimeWarning = null;
        boolean inputWasValid = false; // Flag to track if input was valid

        try {
            // Validate the word against the dictionary and target (basic validation)
            ValidationResult result = validator.validate(word, this.targetWord, this.dictionary);

            // Additional validation: Check if the word is valid in the context of the game ladder rule (one letter difference)
            if (!currentPath.isEmpty()) { // Check if there's a previous word
                String lastWord = currentPath.get(currentPath.size() - 1);
                if (!isOneLetterDifferent(lastWord, word)) {
                    throw new InvalidWordException("Word must differ by exactly one letter from the previous word.");
                }
            } else {
                // This case should ideally not happen if initialize() adds the initial word.
                // But as a fallback, if path is empty, the first word must be the initial word.
                if (!word.equals(this.initialWord)) {
                    throw new InvalidWordException("The first word must be the starting word.");
                }
                // If the first word is the initial word, don't add it again, just validate its state.
                // However, the game logic expects the player to input words *after* the initial word.
                // The first 'tick' should likely be the first step after the initial word.
                // Let's assume the tick is for words *after* the initial word in the path.
                // If the path is empty, it indicates an unexpected state, or the first word input.
                // For simplicity based on typical Weaver gameplay, we assume tick is for steps > 0.
                // If the path is empty here, it might indicate an issue in the game flow.
                // Let's proceed assuming path has at least the initial word before tick is meaningful.
            }


            // If validation passes, add the word and result to the paths
            currentPath.add(word);
            resultsPath.add(result);
            this.isWon = result.getValid(); // Win condition based on the last validation result
            inputWasValid = true;

            if (this.isWon) {
                message = "You won the game!";
            } else {
                // Get message from the validator/decorator (e.g., incorrect attempt)
                message = result.getMessage();
            }

        } catch (InvalidWordException e) {
            // If validation fails, set the error message
            message = e.getMessage();
            // The path and results are NOT updated for invalid input
            this.isWon = false; // Ensure won state is false on invalid input
        } catch (RuntimeException e) {
            // Catch other potential runtime errors during processing
            runtimeWarning = "An unexpected error occurred while processing your input: " + e.getMessage();
            System.err.println(runtimeWarning); // Also print to console for debugging
            this.isWon = false; // Ensure won state is false on error
        }

        // Always notify observers after processing tick, regardless of input validity or showPathFlag.
        // The notifyUpdate method will decide what to show based on showPathFlag and the updated state.
        notifyUpdate(message, runtimeWarning);

        // If the game is won, we might want to prevent further input.
        // This logic is best handled in the Controller or View based on the isWon state received in Notification.
    }

    /**
     * Helper method to check if two words of the same length differ by exactly one letter.
     * Assumes words are of the same length.
     * @param word1 The first word.
     * @param word2 The second word.
     * @return True if they differ by exactly one letter, false otherwise.
     */
    private boolean isOneLetterDifferent(String word1, String word2) {
        // Basic check, though the calling code in tick should ensure length is same.
        if (word1.length() != word2.length()) {
            return false; // Should not happen if tick logic is correct
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
     * Notifies observers about the current game state.
     * Creates the GameState based on showPathFlag (player's progress or full path).
     *
     * @param hint A hint or success message to display to the user.
     * @param runtimeWarning A warning message about potential runtime issues.
     */
    private void notifyUpdate(String hint, String runtimeWarning) {
        setChanged(); // Mark this Observable object as having been changed

        GameState currentState;

        if (showPathFlag) {
            // If showPathFlag is true, generate and provide the full path state
            ArrayList<String> fullPath = null;
            try {
                // Attempt to get the path from the strategy (if WithPath decorator is used)
                if (wordGenerationStrategy instanceof WithPath) {
                    fullPath = ((WithPath) wordGenerationStrategy).getPath();
                }

                // If strategy didn't provide a path or no path found by strategy, use PathFinder BFS
                if (fullPath == null || fullPath.isEmpty()) {
                    // Note: PathFinder.findPathByBFS returns an empty list if no path is found.
                    fullPath = PathFinder.findPathByBFS(this.initialWord, this.targetWord, this.dictionary);
                }

                if (fullPath == null || fullPath.isEmpty()) {
                    // Handle case where no path is found even by BFS
                    // If hint is null from the tick method, provide a default message
                    if (hint == null || hint.isEmpty()) {
                        hint = "No path found between " + this.initialWord + " and " + this.targetWord + ".";
                    }
                    currentState = new GameState(this.initialWord, this.targetWord, new ArrayList<>(), new ArrayList<>(), false); // No path means no win
                } else {
                    // If a full path is found, generate validations for it
                    ArrayList<ValidationResult> validationResults = PathFinder.getValidations(this.targetWord, fullPath, this.dictionary, this.validator);
                    // Calculate win state for the full path display (true if the last word is the target)
                    boolean fullPathWin = fullPath.get(fullPath.size() - 1).equals(this.targetWord);
                    // Create GameState with the full path
                    currentState = new GameState(this.initialWord, this.targetWord, fullPath, validationResults, fullPathWin);

                    // If hint is null, provide a default message for showing path
                    if (hint == null || hint.isEmpty()) {
                        hint = "Showing reference path.";
                    }
                }

            } catch (RuntimeException e){
                // Catch errors during path finding or validation
                runtimeWarning = "An error occurred while finding/validating the full path: " + e.getMessage();
                e.printStackTrace();
                System.err.println(runtimeWarning); // Also print to console
                // Provide a default state on error, keeping previous hint if available
                if (hint == null || hint.isEmpty()) {
                    hint = "Error displaying full path.";
                }
                currentState = new GameState(this.initialWord, this.targetWord, new ArrayList<>(), new ArrayList<>(), false); // Default state on error
            }

        } else {
            // If showPathFlag is false, provide the player's current game state
            // currentState = new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath, this.isWon); // Old constructor usage
            // Use the new GameState constructor that accepts isWon
            currentState = new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath, this.isWon);

            // If hint is null, provide a default message for game progress
            if (hint == null || !showErrorsFlag || hint.isEmpty() && !this.isWon) {
                hint = "Continue playing.";
            } else if (this.isWon) {
                // If won, the tick method should have set the message.
                // But as a fallback:
                if (hint == null || hint.isEmpty()) {
                    hint = "You won the game!";
                }
            }
        }

        // Wrap the GameState and messages in a Notification object
        Notification notification = new Notification(currentState, hint, runtimeWarning);
        notifyObservers(notification); // Notify observers with the Notification
    }

    /**
     * Triggers a notification to observers with the current game state.
     * This method is called by the Controller when the view needs to be updated
     * based on flag changes or initial display, without player input.
     */
    public void notifyObserversWithCurrentState() {
        // Call the internal notification method. Messages are generated within notifyUpdate
        notifyUpdate(null, null);
    }


    // --- Getters and Setters for Flags ---

    public boolean isShowErrorsFlag() {
        return showErrorsFlag;
    }

    /**
     * Sets the show errors flag and updates the validator.
     *
     * @param showErrorsFlag The new value for the flag.
     */
    public void setShowErrorsFlag(boolean showErrorsFlag) {
        if (this.showErrorsFlag != showErrorsFlag) { // Only update if the value changes
            this.showErrorsFlag = showErrorsFlag;
            updateValidator(); // Update validator when flag changes
            // Optionally notify observers that a setting has changed,
            // though updating the validator and strategy might implicitly trigger updates.
            // notifyUpdate("Show errors flag changed to " + showErrorsFlag + ".", null);
            notifyObserversWithCurrentState(); // Notify view to potentially update display based on new error visibility
        }
    }

    public boolean isShowPathFlag() {
        return showPathFlag;
    }

    /**
     * Sets the show path flag and updates the strategy.
     *
     * @param showPathFlag The new value for the flag.
     */
    public void setShowPathFlag(boolean showPathFlag) {
        if (this.showPathFlag != showPathFlag) { // Only update if the value changes
            this.showPathFlag = showPathFlag;
            updateStrategy(); // Update strategy when flag changes
            // notifyUpdate will now use the new showPathFlag next time it's called (e.g., by notifyObserversWithCurrentState)
            // Trigger an immediate update of the view
            notifyObserversWithCurrentState();
        }
    }

    public boolean isRandomWordFlag() {
        return randomWordFlag;
    }

    /**
     * Sets the random word flag and updates the strategy.
     *
     * @param randomWordFlag The new value for the flag.
     */
    public void setRandomWordFlag(boolean randomWordFlag) {
        if (this.randomWordFlag != randomWordFlag) { // Only update if the value changes
            this.randomWordFlag = randomWordFlag;
            updateStrategy(); // Update strategy when flag changes
            // Changing random word flag requires a new game to take effect on start/target words.
            // The initialize method will be called by the Controller/Main to start a new game.
            // notifyUpdate("Random word flag changed to " + randomWordFlag + ". Start a new game for it to take effect.", null);
        }
    }

    // --- Internal Update Methods ---

    /**
     * Updates the word generation strategy based on current flags.
     */
    public void updateStrategy() {
        // Choose strategy factory based on randomWordFlag
        StrategyFactory factory;
        if (randomWordFlag) {
            factory = new RandomStrategyFactory();
        } else {
            // Use fixed words. Ensure dictionary has at least two words.
            if (dictionary == null || dictionary.size() < 2) {
                // This case should ideally be handled during loadDictionary, but as a fallback:
                System.err.println("Error: Dictionary does not have enough words for fixed strategy.");
                // Cannot create FixedStrategyFactory without words.
                // Fallback to a strategy that might throw or indicate error.
                // Or keep the previous strategy if it exists.
                // For now, let's assume dictionary is valid after loadDictionary.
                // Using index 0 and 1 for fixed words as per original code, assuming they exist.
                factory = new FixedStrategyFactory("PORE", "RUDE");
            } else {
                factory = new FixedStrategyFactory("PORE", "RUDE");
            }
        }


        // Apply WithPath decorator if showPathFlag is true
        // Note: The WithPath decorator finds the path during its generateWords call.
        // This might be inefficient if the flag is toggled frequently.
        // However, based on the coursework, the flag controls *display* of the path,
        // and the path finding logic might be intended to run when needed for display.
        // If the intention was to find the path only once per game when needed for display,
        // the path finding logic might be better placed within notifyUpdate when showPathFlag becomes true,
        // or a mechanism to cache the found path could be added.
        // For now, following the structure where WithPath finds the path via generateWords:
        WordGenerationStrategy base = factory.createStrategy(dictionary);
        this.wordGenerationStrategy = showPathFlag ?
                new WithPath(base):
                base;

        // If showPathFlag is true, the WithPath strategy will attempt to find a path
        // when its generateWords is called (e.g., during initialize).
        // When toggling the flag *after* initialization, simply updating the strategy
        // is not enough to immediately find the path for display.
        // The notifyObserversWithCurrentState() call helps trigger the display logic in notifyUpdate.
    }

    /**
     * Updates the word validator based on the showErrorsFlag.
     */
    public void updateValidator() {
        // Apply WithWarning decorator if showErrorsFlag is true
        this.validator = showErrorsFlag
                ? new WithWarning(new BasicValidator())
                : new BasicValidator();
    }

    /**
     * Resets the current game state, clearing the player's path and results
     * while keeping the same initial and target words.
     */
    public void resetGame() {
        this.isWon = false;
        currentPath = new ArrayList<>(); // Re-initialize the path, keeping the list reference
        currentPath.add(initialWord); // Add the initial word back to the path
        resultsPath = new ArrayList<>(); // Re-initialize the results path, keeping the list reference

        // Notify observers about the reset game state
        // 提供一个合适的重置消息
        notifyUpdate("Game reset. Enter your first word.", null);

        // Note: Validator and Strategy do not need to be updated here,
        // as they are based on flags which are not changed by reset.
    }
}