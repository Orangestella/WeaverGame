/**
 * A class representing a notification sent from the model to observers (e.g., GUIView).
 * Contains updated game state, hints, and runtime warnings or error messages.
 */
public class Notification {

    // The current game state (path, initial word, target word, win status)
    private final GameState gameState;

    // Optional hint message for the player
    private final String hint;

    // Optional warning or error message generated during gameplay
    private final String runtimeWarning;

    /**
     * Constructs a new Notification object with specified game state, hint, and warning.
     *
     * @param gameState      The current game state (can be null if not available)
     * @param hint           A hint for the player (can be null)
     * @param runtimeWarning An error or warning message (can be null)
     */
    public Notification(GameState gameState, String hint, String runtimeWarning) {
        this.gameState = gameState;
        this.hint = hint;
        this.runtimeWarning = runtimeWarning;
    }

    /**
     * Gets the game state included in this notification.
     *
     * @return The GameState object, or null if none was provided
     */
    public GameState getGameState() {
        return gameState;
    }

    /**
     * Gets the hint message included in this notification.
     *
     * @return The hint string, or null if no hint was provided
     */
    public String getHint() {
        return hint;
    }

    /**
     * Gets the runtime warning or error message included in this notification.
     *
     * @return The warning/error message, or null if none was provided
     */
    public String getRuntimeWarning() {
        return runtimeWarning;
    }

    /**
     * Checks whether this notification contains a valid game state.
     *
     * @return True if gameState is not null, false otherwise
     */
    public boolean containsGameState() {
        return this.gameState != null;
    }
}