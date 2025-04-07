import java.util.ArrayList;

public interface WordGenerationStrategy {
    public String[] generateWords(ArrayList<String> dictionary);
    public ArrayList<String> getPath();

}
