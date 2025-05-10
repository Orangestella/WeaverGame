import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Abstract base class for implementing a word validator.
 * This class provides a common validation framework for comparing a guessed word against a target word,
 * using a dictionary as reference. Subclasses must implement abstract methods to define specific behavior.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code dictionary} is not empty and contains valid words</li>
 *   <li>{@code target} and {@code word} have the same length</li>
 *   <li>{@code originalCount} and {@code availableCount} contain non-negative integer counts for characters</li>
 *   <li>{@code result} map in {@link ValidationResult} maintains 1:1 mapping between character positions and valid {@link LetterState}</li>
 * </ul>
 */
public abstract class AbstractValidator implements WordValidator {

    /**
     * Validates the given word against the target based on dictionary rules and character matching logic.
     *
     * @pre.    word ≠ null ∧ target ≠ null ∧ dictionary ≠ null
     *          word.length() == target.length()
     *          dictionary.contains(word) ∧ dictionary.contains(target)
     * @post.   result is a valid ValidationResult where:
     *          - All exact matches (GREEN) are identified first
     *          - Remaining characters are marked YELLOW (exists in target but wrong position) or GREY (not present)
     *          - Total GREEN + YELLOW matches ≤ total characters in target
     *          - Original character counts in target are preserved
     *
     * @param word       The user's guessed word.
     * @param target     The target word to compare against.
     * @param dictionary The list of valid words used for dictionary validation.
     * @return A ValidationResult object that contains the result of character-by-character evaluation.
     */
    @Override
    public ValidationResult validate(String word, String target, ArrayList<String> dictionary) {
        // Ensure both words are valid according to dictionary rules
        validInDictionary(target, dictionary);
        validInDictionary(word, dictionary);
        // Validate that both words have equal length
        validLength(target, word);
        // Count characters in the target word
        Map<Character, Integer> originalCount = countCharacters(target);
        // Create a copy of character counts to track usage
        Map<Character, Integer> availableCount = new LinkedHashMap<>(originalCount);
        // Store the result mapping from position to letter state
        Map<Integer, LetterState> result = new LinkedHashMap<>();

        processCorrectPositions(word, target, availableCount, result);
        processRemainingCharacters(word, originalCount, availableCount, result); // (inputWord, originalCountForTarget, ...)

        return new ValidationResult(result);
    }

    /**
     * Ensures the provided word exists in the dictionary.
     *
     * @pre.    word ≠ null ∧ dictionary ≠ null
     *          dictionary is not empty
     * @post.   word ∈ dictionary
     *
     * @param word       The word to check.
     * @param dictionary The dictionary of valid words.
     */
    public abstract void validInDictionary(String word, ArrayList<String> dictionary);

    /**
     * Checks if the lengths of the two words match.
     *
     * @pre.    word ≠ null ∧ target ≠ null
     * @post.   word.length() == target.length()
     *
     * @param word   The first word (e.g., guess).
     * @param target The second word (e.g., target).
     */
    public abstract void validLength(String word, String target);

    /**
     * Counts occurrences of each character in the given word.
     *
     * @pre.    word ≠ null
     * @post.   returned map contains all characters in word with counts ≥ 0
     *          returned map preserves character order if applicable
     *
     * @param word The word to analyze.
     * @return A map where keys are characters and values are their counts.
     */
    public abstract Map<Character, Integer> countCharacters(String word);

    /**
     * Processes the word to identify correct characters in correct positions (GREEN).
     *
     * @pre.    word ≠ null ∧ target ≠ null
     *          word.length() == target.length()
     *          availableCount contains character counts for all characters in target
     *          result is empty or partially populated
     * @post.   For each index i:
     *          - If word[i] == target[i], result[i] = GREEN and availableCount[word[i]] decreases by 1
     *          - Else, result[i] remains unassigned (to be processed later)
     *
     * @param word          The guessed word.
     * @param target        The target word.
     * @param availableCount A map tracking how many times each character can still be used.
     * @param result        A map storing the resulting letter states by position.
     */
    public abstract void processCorrectPositions(String target, String word, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);

    /**
     * Processes the remaining characters to determine if they exist in the target but are misplaced (YELLOW)
     * or not present at all (GREY).
     *
     * @pre.    word ≠ null
     *          originalCount contains full character counts from target
     *          availableCount has remaining counts after processCorrectPositions
     *          result contains GREEN assignments from processCorrectPositions
     * @post.   For each index i not marked GREEN:
     *          - If character exists in availableCount > 0: result[i] = YELLOW, availableCount[char] decreases by 1
     *          - Else: result[i] = GREY
     *
     * @param word           The guessed word.
     * @param originalCount  A map of original character counts in the target word.
     * @param availableCount A map tracking how many times each character is still available for matching.
     * @param result         A map storing the resulting letter states by position.
     */
    public abstract void processRemainingCharacters(String word, Map<Character, Integer> originalCount, Map<Character, Integer> availableCount, Map<Integer, LetterState> result);

}