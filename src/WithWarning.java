import java.util.ArrayList;

public class WithWarning extends ValidatorDecorator{
    public WithWarning(WordValidator baseValidator) {
        super(baseValidator);
    }

    @Override
    public ValidationResult validate(String word, String target, ArrayList<String> dictionary) {
        ValidationResult result = this.getBaseValidator().validate(word, target, dictionary);
        if (result.getValid())
            result.setMessage("Your answer is incorrect, please try again.");
        return result;
    }
}
