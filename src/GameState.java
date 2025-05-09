import java.util.ArrayList;

public class GameState {
    private final ArrayList<String> path;
    private final boolean won;
    private final ArrayList<ValidationResult> results;
    private final String initialWord;
    private final String targetWord;

    public GameState(String initialWord, String targetWord, ArrayList<String> path, ArrayList<ValidationResult> results, boolean isWon) {
        this.path = path;
        this.results = results;
        this.initialWord = initialWord;
        this.targetWord = targetWord;
        this.won = isWon; // 使用传入的获胜状态
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
