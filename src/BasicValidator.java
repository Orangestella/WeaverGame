import exceptions.InvalidWordException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A concrete implementation of the {@link AbstractValidator} for validating a guessed word
 * against a target word using dictionary and character position rules.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code dictionary} is not empty and contains valid words</li>
 *   <li>{@code target} and {@code word} have equal length</li>
 *   <li>{@code originalCount} and {@code availableCount} contain non-negative integer counts for characters</li>
 *   <li>{@code result} map in {@link ValidationResult} maintains 1:1 mapping between positions and valid {@link LetterState}</li>
 *   <li>{@code availableCount} never contains negative values during processing</li>
 * </ul>
 *
 * <p>This validator is suitable for word games like Wordle where:
 * <ul>
 *     <li>The guessed word must exist in the dictionary</li>
 *     <li>It must match the length of the target word</li>
 *     <li>Correct characters in correct positions are marked as {@link LetterState#CORRECT_POSITION}</li>
 *     <li>Correct characters in wrong positions are marked as {@link LetterState#WRONG_POSITION}</li>
 *     <li>Incorrect characters are marked as {@link LetterState#NOT_IN_WORD}</li>
 * </ul>
 */
public class BasicValidator extends AbstractValidator {

    /**
     * Validates whether the given word exists in the provided dictionary.
     *
     * @pre.    word ≠ null ∧ dictionary ≠ null
     *          dictionary is not empty
     * @post.   word ∈ dictionary
     *          throws InvalidWordException if word is not in the dictionary
     *
     * @param word       The word to check.
     * @param dictionary The list of valid words.
     * @throws InvalidWordException if the word is not found in the dictionary.
     */
    @Override
    public void validInDictionary(String word, ArrayList<String> dictionary) {
        if (!dictionary.contains(word)) {
            throw new InvalidWordException("This word is not in the dictionary.");
        }
    }

    /**
     * Ensures the guessed word has the same length as the target word.
     *
     * @pre.    word ≠ null ∧ target ≠ null
     * @post.   word.length() == target.length()
     *          throws InvalidWordException if lengths do not match
     *
     * @param word   The guessed word.
     * @param target The target word.
     * @throws InvalidWordException if the lengths do not match.
     */
    @Override
    public void validLength(String word, String target) {
        if (word.length() != target.length()) {
            throw new InvalidWordException("Length of word is not equal to target word.");
        }
    }

    /**
     * Counts the occurrences of each character in the given word.
     *
     * @pre.    word ≠ null
     * @post.   returned map contains all characters with non-negative counts
     *          preserves order of first appearance (LinkedHashMap behavior)
     *
     * @param word The word to analyze.
     * @return A map where keys are characters and values are their respective counts.
     */
    @Override
    public Map<Character, Integer> countCharacters(String word) {
        Map<Character, Integer> count = new LinkedHashMap<>();
        for (char c : word.toCharArray()) {
            count.put(c, count.getOrDefault(c, 0) + 1);
        }
        return count;
    }

    /**
     * Identifies characters that are in the correct position (marked as CORRECT_POSITION).
     * Updates the available character count to avoid double counting.
     *
     * @pre.    target ≠ null ∧ word ≠ null
     *          target.length() == word.length()
     *          availableCount contains full character counts from target
     *          result is empty or partially populated
     * @post.   For each index i:
     *          - If word[i] == target[i], result[i] = CORRECT_POSITION and availableCount[word[i]] decreases by 1
     *          - Else, result[i] remains unassigned (to be processed later)
     *
     * @param target         The target word.
     * @param word           The guessed word.
     * @param availableCount A map tracking how many times each character can still be used.
     * @param result         A map storing the resulting letter states by position.
     */
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

    /**
     * Processes remaining characters to determine if they appear in the target word
     * but in the wrong position (WRONG_POSITION) or not at all (NOT_IN_WORD).
     *
     * @pre.    word ≠ null
     *          originalCount contains full character counts from target
     *          availableCount has remaining counts after processCorrectPositions
     *          result contains CORRECT_POSITION assignments from processCorrectPositions
     * @post.   For each index i not marked CORRECT_POSITION:
     *          - If character exists in availableCount > 0 → result[i] = WRONG_POSITION, availableCount[char] decreases by 1
     *          - Else → result[i] = NOT_IN_WORD
     *
     * @param word           The guessed word.
     * @param originalCount  A map of original character counts in the target word.
     * @param availableCount A map tracking how many times each character is still available for matching.
     * @param result         A map storing the resulting letter states by position.
     */
    @Override
    public void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result) {
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);

            if (!result.containsKey(i)) {
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