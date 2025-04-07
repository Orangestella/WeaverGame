import exceptions.WordGenerationException;
import java.util.ArrayList;
import java.util.Random;

public class RandomWordStrategy implements WordGenerationStrategy{

    private final ArrayList<String> dictionary;

    public RandomWordStrategy(ArrayList<String> dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        if (this.dictionary.size() < 2) {
            throw new WordGenerationException("Insufficient valid words");
        }
        String initialWord;
        String finalWord;
        Random random = new Random();
        int index1 = random.nextInt(dictionary.size());
        int index2;

        do {
            index2 = random.nextInt(dictionary.size());
        } while (index1 == index2);
        initialWord = dictionary.get(index1);
        finalWord = dictionary.get(index2);

        return new String[]{initialWord, finalWord};
    }

    @Override
    public ArrayList<String> getPath() {
        return null;
    }

}
