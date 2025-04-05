import java.util.ArrayList;

public interface WordValidator {
    public ValidationResult validate(String word, String target, ArrayList<String> dictionary);
}
