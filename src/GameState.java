import java.util.ArrayList;

public class GameState {
    private final ArrayList<String> path;
    private final boolean won;
    private final ArrayList<ValidationResult> results;

    public GameState(boolean won, ArrayList<String> path, ArrayList<ValidationResult> results) {
        this.path = path;
        this.won = won;
        this.results = results;
    }
    public ArrayList<String> getPath() {
        return path;
    }
    public boolean isWon() {
        return won;
    }
    public ArrayList<ValidationResult> getResults() {
        return results;
    }
}
