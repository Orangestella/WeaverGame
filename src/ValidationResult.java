import java.util.Map;

/**
 * Represents the result of validating a guessed word against the target word.
 * Contains information about:
 * - Whether the guess is valid (i.e., matches all letter positions)
 * - A message describing validation outcome
 * - Letter-by-letter state (correct position, wrong position, not in word)
 */
public class ValidationResult {
    private final boolean valid;
    private String message;
    private Map<Integer, LetterState> letterStates;

    /**
     * Checks if the guess is fully correct by verifying that every letter is in the correct position.
     *
     * @return true if all letters are correctly placed, false otherwise
     */
    private boolean isValid() {
        if (letterStates == null || letterStates.isEmpty()) {
            return false;
        }

        try {
            java.util.List<LetterState> statesList = new java.util.ArrayList<>(letterStates.values());
            for (LetterState state : statesList) {
                if (state != LetterState.CORRECT_POSITION) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            System.err.println("Error during ValidationResult.isValid iteration: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gets the optional message associated with this validation result.
     *
     * @return The message string, or null if no message was set
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets a custom message to be associated with this validation result.
     *
     * @param message The message to display to the user
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets the map of letter indices to their corresponding states.
     *
     * @return A Map where keys are letter positions and values are LetterState enums
     */
    public Map<Integer, LetterState> getLetterStates() {
        return letterStates;
    }

    /**
     * Gets whether the input word fully matches the target word.
     *
     * @return true if all letters are correct and in the right position
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * Constructs a ValidationResult with the given letter states.
     * Automatically determines if the guess is fully valid.
     *
     * @param letterStates A map of letter positions to their states
     */
    public ValidationResult(Map<Integer, LetterState> letterStates) {
        this.message = "NULL";
        this.letterStates = letterStates;
        this.valid = isValid(); // Determine validity based on all letters being CORRECT_POSITION
    }
}