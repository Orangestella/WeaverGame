import java.util.ArrayList;

/**
 * An abstract base class for implementing the Decorator pattern over word validators.
 * This class wraps a base validator and allows subclasses to add additional validation behavior
 * before or after delegating to the wrapped validator.
 *
 * <p>Subclasses should override the {@link #validate(String, String, ArrayList)} method
 * to implement their specific validation logic.</p>
 */
public abstract class ValidatorDecorator implements WordValidator {
    private final WordValidator baseValidator;

    /**
     * Constructs a new ValidatorDecorator that wraps the specified base validator.
     *
     * @param baseValidator The validator to be decorated (cannot be null)
     */
    public ValidatorDecorator(WordValidator baseValidator) {
        this.baseValidator = baseValidator;
    }

    /**
     * Gets the base validator wrapped by this decorator.
     *
     * @return The base WordValidator instance
     */
    public WordValidator getBaseValidator() {
        return baseValidator;
    }

    /**
     * Validates the given word against the target using the dictionary.
     * Subclasses must provide their own implementation to extend or modify the validation behavior.
     *
     * @param word       The guessed word to validate
     * @param target     The target word to compare against
     * @param dictionary The list of valid words used in the game
     * @return A ValidationResult containing the result of validation, including letter states and messages
     */
    @Override
    public abstract ValidationResult validate(String word, String target, ArrayList<String> dictionary);
}