import java.util.ArrayList;

public abstract class ValidatorDecorator implements WordValidator{
    private WordValidator baseValidator;
    public ValidatorDecorator(WordValidator baseValidator) {
        this.baseValidator = baseValidator;
    }
    public WordValidator getBaseValidator() {
        return baseValidator;
    }
    @Override
    public abstract ValidationResult validate(String word, String target, ArrayList<String> dictionary);
}
