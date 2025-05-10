import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Map;
import exceptions.InvalidWordException;
import exceptions.WordGenerationException;


public class CLIMain {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        WeaverModel model;
        try {
            System.out.println("Welcome to Weaver CLI!");
            model = new WeaverModel();
            initializeGame(model);
            runGameLoop(model, scanner);
        } catch (IOException e) {
            System.err.println("Error loading dictionary: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    /**
     * Initializes a new game by calling model.initialize() and displays the initial state.
     *
     * @pre.    model ≠ null ∧ model is not already initialized
     * @post.   model is initialized with valid start and target words
     *          initial game state is displayed to user
     *          if initialization fails, WordGenerationException is thrown
     *
     * @param model The WeaverModel instance.
     * @throws WordGenerationException if word generation fails.
     */
    private static void initializeGame(WeaverModel model) throws WordGenerationException {
        try {
            model.initialize(); // Call model's initialize method
            System.out.println("\n--- New Game Started ---");
            displayGameState(model);
            System.out.println("Game started. Enter your first word.");
        } catch (WordGenerationException e) {
            System.err.println("Failed to initialize game: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Runs the main game loop, handling user input and game progression.
     *
     * @pre.    model ≠ null ∧ scanner ≠ null
     * @post.   game loop runs until user enters 'quit' command
     *          all user inputs are processed appropriately
     *          game state is updated accordingly
     *
     * @param model   The WeaverModel instance.
     * @param scanner The Scanner for reading user input.
     */
    private static void runGameLoop(WeaverModel model, Scanner scanner) {
        boolean gameIsRunning = true; // Controls the main game loop
        while (gameIsRunning) {
            // Only prompt for normal gameplay input if the game is not won
            if (!model.isWon()) {
                promptForInput(); // Show input prompt
            } else {
                // If the game is won, prompt to enter 'new game' or 'quit'
                System.out.print("Game won. Enter 'new game' to play again or 'quit' to exit: ");
            }

            String inputLine = scanner.nextLine().trim(); // Read and trim user input

            // **Handle quit command (always valid)**
            if (inputLine.equalsIgnoreCase("quit")) {
                gameIsRunning = false;
                System.out.println("Quitting game. Goodbye!");
                continue;
            }
            boolean commandProcessed = false;

            if (inputLine.equalsIgnoreCase("reset")) {
                commandProcessed = true;
                model.resetGame(); // Call model's resetGame method
                displayGameState(model); // Display game state after reset
                System.out.println("Game reset. Enter your first word.");
            } else if (inputLine.equalsIgnoreCase("new game")) {
                commandProcessed = true;
                try {
                    initializeGame(model);
                } catch (WordGenerationException e) {
                    System.err.println("Failed to start a new game.");
                }
            } else if (inputLine.equalsIgnoreCase("show path")) {
                commandProcessed = true;
                displaySolutionPath(model);
            } else if (inputLine.toLowerCase().startsWith("set errors ")) {
                commandProcessed = true;
                String[] parts = inputLine.split("\\s+");
                if (parts.length == 3 && parts[1].equalsIgnoreCase("errors")) {
                    if (parts[2].equalsIgnoreCase("on")) {
                        model.setShowErrorsFlag(true);
                        System.out.println("Show errors enabled.");
                    } else if (parts[2].equalsIgnoreCase("off")) {
                        model.setShowErrorsFlag(false);
                        System.out.println("Show errors disabled.");
                    } else {
                        displayInvalidCommand(inputLine);
                    }
                } else {
                    displayInvalidCommand(inputLine);
                }
                displayGameState(model);
            } else if (inputLine.toLowerCase().startsWith("set random ")) {
                commandProcessed = true;
                String[] parts = inputLine.split("\\s+");
                if (parts.length == 3 && parts[1].equalsIgnoreCase("random")) {
                    if (parts[2].equalsIgnoreCase("on")) {
                        model.setRandomWordFlag(true);
                        System.out.println("Random words enabled. Start a new game for changes to take effect.");
                    } else if (parts[2].equalsIgnoreCase("off")) {
                        model.setRandomWordFlag(false);
                        System.out.println("Fixed words enabled. Start a new game for changes to take effect.");
                    } else {
                        displayInvalidCommand(inputLine);
                    }
                } else {
                    displayInvalidCommand(inputLine);
                }
                displayGameState(model);
            }

            if (!commandProcessed && !model.isWon()) {
                if (model.getTargetWord() != null && inputLine.length() == model.getTargetWord().length()) {
                    try {
                        ValidationResult result = model.tick(inputLine);
                        displayGameState(model);
                        if (model.isShowErrorsFlag() && result.getMessage() != null && !result.getMessage().isEmpty()) {
                            System.out.println("Message: " + result.getMessage());
                        } else if (!model.isShowErrorsFlag() && model.isWon()) {
                            System.out.println("Congratulations! You won the game!");
                        }
                    } catch (InvalidWordException e) {
                        displayGameState(model);
                        if (model.isShowErrorsFlag()) {
                            System.out.println("Error: " + e.getMessage());
                        } else {
                            System.out.println("Invalid input.");
                        }
                    } catch (RuntimeException e) {
                        displayGameState(model);
                        System.err.println("An unexpected error occurred during tick: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    displayGameState(model);
                    System.out.println("Invalid input: Please enter a " + (model.getTargetWord() != null ? model.getTargetWord().length() : "correct") + "-letter word or a valid command.");
                }
            } else if (!commandProcessed && model.isWon()) {
                displayInvalidCommand(inputLine);
            }
        }
    }

    /**
     * Displays the current game state to the console.
     *
     * @pre.    model ≠ null
     * @post.   start word, target word, and current path are printed in formatted way
     *          validation results are shown using simplified letter states (G/Y/L)
     *
     * @param model The WeaverModel instance.
     */
    private static void displayGameState(WeaverModel model) {
        System.out.println("\n--- Current Game State ---");
        System.out.println("Start Word: " + model.getInitialWord());
        System.out.println("Target Word: " + model.getTargetWord());
        System.out.println("Path:");

        ArrayList<String> path = model.getCurrentPath();
        ArrayList<ValidationResult> results = model.getResultsPath();

        if (path != null && !path.isEmpty()) {
            System.out.println("  " + path.get(0));
            for (int i = 1; i < path.size(); i++) {
                String word = path.get(i);
                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
                System.out.println("  " + word + " " + formatValidationResult(result));
            }
        } else {
            System.out.println("  (Path is empty)");
        }
        System.out.println("--------------------------");
    }

    /**
     * Formats a ValidationResult for CLI display using single-letter codes (G/Y/L).
     *
     * @pre.    result may be null or contain valid LetterState values
     * @post.   returns formatted string like "[G Y G L]"
     *          returns empty string if result is null or invalid
     *
     * @param result The validation result. Can be null.
     * @return A string representation of the validation result.
     */
    private static String formatValidationResult(ValidationResult result) {
        if (result == null || result.getLetterStates() == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder("[");
        Map<Integer, LetterState> states = result.getLetterStates();

        for (int i = 0; i < 4; i++) {
            LetterState state = states.get(i);
            if (state != null) {
                switch (state) {
                    case CORRECT_POSITION:
                        sb.append("G");
                        break;
                    case WRONG_POSITION:
                        sb.append("Y");
                        break;
                    case NOT_IN_WORD:
                        sb.append("L");
                        break;
                    default:
                        sb.append("?");
                        break;
                }
            } else {
                sb.append("?");
            }
            if (i < 3) {
                sb.append(" ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Displays the full solution path including each step's validation result.
     *
     * @pre.    model ≠ null ∧ model has a valid full solution path
     * @post.   full path is displayed with corresponding validation results
     *          message "No path available" is shown if path is empty or null
     *
     * @param model The WeaverModel instance.
     */
    private static void displaySolutionPath(WeaverModel model) {
        System.out.println("\n--- Full Solution Path ---");
        ArrayList<String> fullPath = model.getFullSolutionPath();
        String targetWord = model.getTargetWord();
        ArrayList<String> dictionary = model.getDictionary();

        if (fullPath == null || fullPath.isEmpty()) {
            System.out.println("No path available.");
        } else {
            ArrayList<ValidationResult> results = PathFinder.getValidations(targetWord, fullPath, dictionary);
            if (!fullPath.isEmpty()) {
                System.out.println("  " + fullPath.get(0));
            }
            for (int i = 1; i < fullPath.size(); i++) {
                String word = fullPath.get(i);
                ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
                System.out.println("  " + word + " " + formatValidationResult(result));
            }
        }
        System.out.println("--------------------------");
    }

    /**
     * Displays a prompt message to the user for input.
     */
    private static void promptForInput() {
        System.out.print("Enter your next word or command ('quit', 'reset', 'new game', 'show path', 'set errors [on|off]', 'set random [on|off]'): ");
    }

    /**
     * Displays a message indicating that the user's input was invalid.
     *
     * @param input The invalid input string entered by the user.
     */
    private static void displayInvalidCommand(String input) {
        System.out.println("Invalid input or command: '" + input + "'.");
    }
}