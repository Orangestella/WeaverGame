import java.util.ArrayList;

/**
 * A validator decorator that adds informative messages to validation results.
 * This class enhances the base validator by providing feedback such as:
 * - "Continue" if the guess is incorrect
 * - "You win the game!" if the guess matches the target word
 *
 * <p>It wraps a base {@link WordValidator} and modifies the result message
 * based on whether the guess was correct.</p>
 */
public class WithWarning extends ValidatorDecorator {

    /**
     * Constructs a new WithWarning validator that decorates the given base validator.
     *
     * @param baseValidator The underlying validator to enhance with warning messages
     */
    public WithWarning(WordValidator baseValidator) {
        super(baseValidator);
    }

    /**
     * Validates the guessed word using the base validator,
     * then modifies the result message for user feedback.
     *
     * @param word       The player's guessed word
     * @param target     The target word to match
     * @param dictionary The dictionary of valid words
     * @return A ValidationResult object with updated message
     */
    @Override
    public ValidationResult validate(String word, String target, ArrayList<String> dictionary) {
        ValidationResult result = this.getBaseValidator().validate(word, target, dictionary);
        if (!result.getValid()) {
            result.setMessage("Continue");
        } else {
            result.setMessage("You win the game!");
        }
        return result;
    }
}