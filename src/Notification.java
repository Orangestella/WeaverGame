public class Notification {
    private final GameState gameState;
    private final String hint;
    private final String runtimeWarning;
    public Notification(GameState gameState, String hint, String runtimeWarning) {
        this.gameState = gameState;
        this.hint = hint;
        this.runtimeWarning = runtimeWarning;
    }
    public GameState getGameState() {
        return gameState;
    }
    public String getHint() {
        return hint;
    }
    public String getRuntimeWarning() {
        return runtimeWarning;
    }
    public boolean containsGameState() {
        return this.gameState != null;
    }
}
