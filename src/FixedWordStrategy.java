import java.util.ArrayList;

public class FixedWordStrategy implements WordGenerationStrategy{
    private final String initial;
    private final String target;
    private final ArrayList<String> dictionary;

    public FixedWordStrategy(String start, String target, ArrayList<String> dictionary) {
        this.initial = start;
        this.target = target;
        this.dictionary = dictionary;
    }


    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        return new String[]{initial, target};
    }

    @Override
    public ArrayList<String> getPath() {
        return null;
    }

}
