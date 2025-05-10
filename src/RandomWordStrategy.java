import exceptions.WordGenerationException;
import java.util.ArrayList;
import java.util.Random;

/**
 * A word generation strategy that randomly selects two distinct words from the dictionary.
 * One word is used as the starting word, and the other as the target word.
 */
public class RandomWordStrategy implements WordGenerationStrategy {

    // The dictionary of valid words used for random selection
    private final ArrayList<String> dictionary;

    /**
     * Constructs a new RandomWordStrategy with the given dictionary.
     *
     * @param dictionary The list of valid words to choose from
     */
    public RandomWordStrategy(ArrayList<String> dictionary) {
        this.dictionary = dictionary;
    }

    /**
     * Generates a pair of two distinct random words from the dictionary.
     *
     * <p>This method randomly selects two different words: one as the initial word,
     * and one as the target word. If the dictionary contains fewer than 2 words,
     * an exception is thrown.</p>
     *
     * @param dictionary Ignored in this implementation; uses internal dictionary instead
     * @return An array containing [initialWord, targetWord]
     * @throws WordGenerationException if there are not enough valid words in the dictionary
     */
    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        if (this.dictionary.size() < 2) {
            throw new WordGenerationException("Insufficient valid words");
        }

        Random random = new Random();
        int index1 = random.nextInt(this.dictionary.size());

        int index2;
        do {
            index2 = random.nextInt(this.dictionary.size());
        } while (index1 == index2); // Ensure the two words are different

        String initialWord = this.dictionary.get(index1);
        String finalWord = this.dictionary.get(index2);

        return new String[]{initialWord, finalWord};
    }

    /**
     * Gets the solution path for the game.
     * This strategy does not provide a predefined path.
     *
     * @return null, since no specific solution path is available
     */
    @Override
    public ArrayList<String> getPath() {
        return null;
    }
}