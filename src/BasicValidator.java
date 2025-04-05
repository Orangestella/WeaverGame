import exceptions.InvalidWordException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class BasicValidator extends AbstractValidator{

    @Override
    public void validInDictionary(String word, ArrayList<String> dictionary) {
        if(!dictionary.contains(word)){
            throw new InvalidWordException("This word is not in the dictionary.");
        }
    }

    @Override
    public void validLength(String word, String target) {
        if(word.length() != target.length()){
            throw new InvalidWordException("Length of word is not equal to target word.");
        }
    }

    @Override
    public Map<Character, Integer> countCharacters(String word) {
        Map<Character, Integer> count = new HashMap<>();
        for (char c : word.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        return count;
    }

    @Override
    public void processCorrectPositions(String target, String word, Map<Character, Integer> availableCount, Map<Integer, LetterState> result) {
        for (int i = 0; i < target.length(); i++) {
            char c = word.charAt(i);
            if (c == target.charAt(i)) {
                result.put(i, LetterState.CORRECT_POSITION);
                availableCount.put(c, availableCount.get(c) - 1);
            }
        }
    }

    @Override
    public void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result) {
        for (int i = 0; i < word.length(); i++) {
            if (!result.containsKey(i)) {
                char c = word.charAt(i);
                if (originalCount.containsKey(c) && availableCount.getOrDefault(c, 0) > 0) {
                    result.put(i, LetterState.WRONG_POSITION);
                    availableCount.put(c, availableCount.get(c) - 1);
                } else {
                    result.put(i, LetterState.NOT_IN_WORD);
                }
            }
        }
    }
}
