import java.util.ArrayList;

/**
 * An interface for validating a guessed word against a target word.
 * Implementing classes define the rules for how words are checked,
 * such as letter positions, dictionary validity, and transformation rules.
 */
public interface WordValidator {
    /**
     * Validates the given word against the target word using the provided dictionary.
     *
     * @param word       The guessed word to validate
     * @param target     The target word to compare against
     * @param dictionary A list of valid words that can be used in validation
     * @return A ValidationResult object containing:
     *         - Letter-by-letter match states (correct position, wrong position, not in word)
     *         - Optional message (e.g., "You win!" or "Invalid input")
     */
    ValidationResult validate(String word, String target, ArrayList<String> dictionary);
}