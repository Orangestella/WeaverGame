import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public abstract class AbstractValidator implements WordValidator {

    @Override
    public ValidationResult validate(String word, String target, ArrayList<String> dictionary) {
        // **立即将参数值保存到新的局部变量中**
        String initialTarget = target; // 使用这个变量代表游戏目标词
        String inputWord = word;       // 使用这个变量代表玩家输入的单词

        System.out.println("DEBUG: Entering validate method.");
        System.out.println("DEBUG: Received target param: " + target + ", word param: " + word);
        System.out.println("DEBUG: Captured initialTarget: " + initialTarget + ", inputWord: " + inputWord);

        // 在后续代码中，始终使用 initialTarget 和 inputWord
        validInDictionary(initialTarget, dictionary);
        validInDictionary(inputWord, dictionary);
        validLength(initialTarget, inputWord);

        System.out.println("DEBUG: Before originalCount initialization. Using initialTarget: " + initialTarget);
        // 使用 initialTarget 计算 originalCount
        Map<Character, Integer> originalCount = countCharacters(initialTarget); // **修正这里，使用 initialTarget**
        System.out.println("DEBUG: After countCharacters(initialTarget). originalCount: " + originalCount);

        Map<Character, Integer> availableCount = new LinkedHashMap<>(originalCount);
        Map<Integer, LetterState> result = new LinkedHashMap<>();

        // 调用 processCorrectPositions 时，使用 inputWord 和 initialTarget
        processCorrectPositions(inputWord, initialTarget, availableCount, result); // (inputWord, targetWord)

        // 调用 processRemainingCharacters 时，使用 inputWord 和 originalCount (它现在是基于 initialTarget 的)
        processRemainingCharacters(inputWord, originalCount, availableCount, result); // (inputWord, originalCountForTarget, ...)

        return new ValidationResult(result);
    }
    public abstract void validInDictionary(String word, ArrayList<String> dictionary);
    public abstract void validLength(String word, String target);
    public abstract Map<Character, Integer> countCharacters(String word);
    public abstract void processCorrectPositions(String target, String word, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);
    public abstract void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);

}
