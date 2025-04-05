import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractValidator implements WordValidator {

    @Override
    public ValidationResult validate(String target, String word, ArrayList<String> dictionary) {
        validInDictionary(target, dictionary);
        validInDictionary(word, dictionary);
        validLength(target, word);
        Map<Character, Integer> originalCount = countCharacters(target);
        Map<Character, Integer> availableCount = new HashMap<>(originalCount);
        Map<Integer, LetterState> result = new HashMap<>();

        processCorrectPositions(target, word, availableCount, result);
        processRemainingCharacters(word, originalCount, availableCount, result);
        return new ValidationResult(result);
    }
    public abstract void validInDictionary(String word, ArrayList<String> dictionary);
    public abstract void validLength(String word, String target);
    public abstract Map<Character, Integer> countCharacters(String word);
    public abstract void processCorrectPositions(String target, String word, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);
    public abstract void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);

}
