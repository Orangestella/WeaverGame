import java.util.ArrayList;

public class GameState {
    private final ArrayList<String> path;
    private final boolean won;
    private final ArrayList<ValidationResult> results;
    private final String initialWord;
    private final String targetWord;

    public GameState(String initialWord, String targetWord, ArrayList<String> path, ArrayList<ValidationResult> results) {
        this.path = path;
        this.won = results.get(results.size() - 1).getValid();
        this.results = results;
        this.initialWord = initialWord;
        this.targetWord = targetWord;
    }
    public ArrayList<String> getPath() {
        return new ArrayList<>(path);
    }
    public boolean isWon() {
        return won;
    }
    public ArrayList<ValidationResult> getResults() {
        return new ArrayList<>(results);
    }
    public String getInitialWord() {
        return initialWord;
    }
    public String getTargetWord() {
        return targetWord;
    }
}
