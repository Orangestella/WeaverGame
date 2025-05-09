import exceptions.InvalidWordException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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
        Map<Character, Integer> count = new LinkedHashMap<>();
        for (char c : word.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        return count;
    }

    @Override
    public void processCorrectPositions(String target, String word, Map<Character, Integer> availableCount, Map<Integer, LetterState> result) {
        System.out.println("--- processCorrectPositions ---");
        System.out.println("Word: " + word + ", Target: " + target);
        System.out.println("Initial availableCount: " + availableCount);
        System.out.println("Initial result: " + result);

        for (int i = 0; i < target.length(); i++) {
            char c = word.charAt(i);
            System.out.println("Processing index: " + i + ", character: " + c);

            if (c == target.charAt(i)) {
                System.out.println("Character '" + c + "' at index " + i + " is in correct position.");
                result.put(i, LetterState.CORRECT_POSITION);
                // Before decrementing
                System.out.println("availableCount before decrementing '" + c + "': " + availableCount.get(c));
                availableCount.put(c, availableCount.get(c) - 1);
                // After decrementing
                System.out.println("availableCount after decrementing '" + c + "': " + availableCount.get(c));
            }
            System.out.println("availableCount after processing index " + i + ": " + availableCount);
            System.out.println("result after processing index " + i + ": " + result);
        }
        System.out.println("--- End processCorrectPositions ---");
    }

    @Override
    public void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result) {
        System.out.println("--- Start processRemainingCharacters ---");
        System.out.println("Processing word: " + word);
        System.out.println("Initial originalCount: " + originalCount);
        System.out.println("Initial availableCount: " + availableCount);
        System.out.println("Initial result: " + result);

        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            System.out.println("\nProcessing index: " + i + ", character: " + c);
            System.out.println("Current result map before check: " + result); // Check result map state

            if (!result.containsKey(i)) {
                System.out.println("Result map does NOT contain key " + i + ". Processing remaining character logic.");

                // Before accessing maps, print their state and check for nulls (though unlikely for these maps)
                System.out.println("Checking counts for character '" + c + "':");
                System.out.println("originalCount.containsKey('" + c + "'): " + originalCount.containsKey(c));
                System.out.println("availableCount.getOrDefault('" + c + "', 0): " + availableCount.getOrDefault(c, 0));



                if (originalCount.containsKey(c) && availableCount.getOrDefault(c, 0) > 0) {
                    System.out.println("Character '" + c + "' at index " + i + " is in the word but wrong position or already accounted for.");
                    result.put(i, LetterState.WRONG_POSITION);
                    // Before decrementing
                    System.out.println("availableCount before decrementing '" + c + "': " + availableCount.get(c));
                    availableCount.put(c, availableCount.get(c) - 1);
                    // After decrementing
                    System.out.println("availableCount after decrementing '" + c + "': " + availableCount.get(c));

                } else {
                    System.out.println("Character '" + c + "' at index " + i + " is NOT in the word or all occurrences already accounted for.");
                    result.put(i, LetterState.NOT_IN_WORD);
                }
                System.out.println("Result map after processing index " + i + ": " + result); // Print result state after processing
            } else {
                System.out.println("Result map ALREADY contains key " + i + ". Skipping remaining character logic for this index.");
            }
            System.out.println("availableCount after processing index " + i + ": " + availableCount); // Print availableCount state after processing
        }
        System.out.println("--- End processRemainingCharacters ---");
    }
}
