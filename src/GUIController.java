import exceptions.InvalidWordException;
import exceptions.WordGenerationException;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

/**
 * Controller class for the GUI version of the Weaver game.
 * Handles user input from both physical and virtual keyboards,
 * and communicates with the model to update game state.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code model} ≠ null ∧ view ≠ null</li>
 *   <li>{@code currentInputWord} ≠ null (may be empty)</li>
 *   <li>{@code physicalKeyboardProcessingEnabled} reflects whether physical keyboard input is active</li>
 *   <li>{@code view} is always registered as an observer of the model</li>
 * </ul>
 */
public class GUIController implements KeyListener {

    private WeaverModel model;
    private GUIView view;
    private StringBuilder currentInputWord;
    private boolean physicalKeyboardProcessingEnabled = true;

    /**
     * Constructs a GUIController with specified model and view.
     * Registers the controller as a key listener and observer.
     *
     * @pre.    model ≠ null ∧ view ≠ null
     * @post.   this.model == model ∧ this.view == view
     *          view's controller is set to this
     *          model.addObserver(view) is called
     *          currentInputWord is initialized as empty
     *
     * @param model The game model
     * @param view  The game view
     */
    public GUIController(WeaverModel model, GUIView view) {
        this.model = model;
        this.view = view;
        this.currentInputWord = new StringBuilder(); // Input buffer initialization

        model.addObserver(view);
        view.setController(this);
    }

    /**
     * Returns the KeyListener instance (this controller).
     * The GUIView will add this listener to the main frame.
     *
     * @post.   returns this (non-null)
     *
     * @return The KeyListener instance
     */
    public KeyListener getKeyListener() {
        return this;
    }

    /**
     * Enables/disables physical keyboard processing.
     * Called by GUIView based on game state.
     *
     * @pre.    enabled is either true or false
     * @post.   physicalKeyboardProcessingEnabled == enabled
     *
     * @param enabled True to enable, false to disable
     */
    public void setPhysicalKeyboardProcessingEnabled(boolean enabled) {
        this.physicalKeyboardProcessingEnabled = enabled;
    }

    /**
     * Handles reset button action.
     * Resets game state while keeping the same words.
     *
     * @pre.    model.isResettable() == true (assumed)
     * @post.   model.resetGame() is called
     *          currentInputWord is cleared
     *          input display is updated
     *          reset button is disabled
     *          any path solution window is closed
     */
    public void handleResetAction() {
        model.resetGame();
        currentInputWord.setLength(0);
        view.updateInputDisplay("");
        view.setResetButtonEnabled(false);

        PathSolutionView pathWindow = view.getPathSolutionWindow();
        if (pathWindow != null) {
            pathWindow.dispose();
            view.setPathSolutionWindow(null);
            view.setShowPathSelected(false);
        }
        view.requestFocusInWindow();
    }

    /**
     * Handles new game button action.
     * Starts a new game with potentially new words.
     *
     * @pre.    model can generate new valid words
     * @post.   model.initialize() is called
     *          currentInputWord is cleared
     *          input display is updated
     *          reset button is disabled
     *          any path solution window is closed
     *          if WordGenerationException occurs, error message is displayed
     */
    public void handleNewGameAction() {
        try {
            model.initialize();
            currentInputWord.setLength(0);
            view.updateInputDisplay("");
            view.setResetButtonEnabled(false);

            PathSolutionView pathWindow = view.getPathSolutionWindow();
            if (pathWindow != null) {
                pathWindow.dispose();
                view.setPathSolutionWindow(null);
                view.setShowPathSelected(false);
            }
        } catch (WordGenerationException e) {
            view.setMessage("Failed to start new game: " + e.getMessage());
        }
        view.requestFocusInWindow();
    }

    /**
     * Handles show errors checkbox state change.
     * Updates model's show errors flag.
     *
     * @pre.    showErrors is either true or false
     * @post.   model.setShowErrorsFlag(showErrors) is called
     *          focus is requested in window
     *
     * @param showErrors New checkbox state
     */
    public void handleShowErrorsFlag(boolean showErrors) {
        model.setShowErrorsFlag(showErrors);
        view.requestFocusInWindow();
    }

    /**
     * Handles random words checkbox state change.
     * Updates model's random word flag.
     *
     * @pre.    randomWords is either true or false
     * @post.   model.setRandomWordFlag(randomWords) is called
     *          focus is requested in window
     *
     * @param randomWords New checkbox state
     */
    public void handleRandomWordFlag(boolean randomWords) {
        model.setRandomWordFlag(randomWords);
        view.requestFocusInWindow();
    }

    /**
     * Handles virtual keyboard key presses.
     * Called by virtual keyboard button ActionListeners.
     *
     * @pre.    keyText ∈ {"A", "B", ..., "Z", "ENTER", "DEL"} (case-insensitive)
     * @post.   appropriate key handler (processLetterKey, processEnterKey, processBackspaceKey) is called
     *          focus is requested in window
     *
     * @param keyText Button text ("A", "ENTER", "DEL")
     */
    public void handleVirtualKeyPress(String keyText) {
        String upperKeyText = keyText.toUpperCase();

        if ("ENTER".equals(upperKeyText)) {
            processEnterKey();
        } else if ("<-".equals(upperKeyText)) {
            processBackspaceKey();
        } else if (upperKeyText.length() == 1 && Character.isLetter(upperKeyText.charAt(0))) {
            processLetterKey(upperKeyText.charAt(0));
        }
        view.requestFocusInWindow();
    }

    /**
     * Processes letter key input.
     * Adds letter to input buffer if length limit not reached.
     *
     * @pre.    letter is an uppercase English letter
     *          model.getTargetWord() ≠ null
     *          currentInputWord.length() < model.getTargetWord().length()
     * @post.   if conditions met: letter is appended to currentInputWord and displayed
     *          otherwise: no change
     *
     * @param letter Pressed letter (uppercase)
     */
    private void processLetterKey(char letter) {
        if (model.getTargetWord() != null &&
                currentInputWord.length() < model.getTargetWord().length()) {
            currentInputWord.append(letter);
            view.updateInputDisplay(currentInputWord.toString());
        }
    }

    /**
     * Processes Enter key press.
     * Submits current input word to model if valid length.
     *
     * @pre.    model.getTargetWord() ≠ null
     *          currentInputWord.length() == model.getTargetWord().length()
     * @post.   if conditions met: word is submitted via model.tick(wordToSubmit)
     *          input buffer is cleared
     *          input display is updated
     *          message is set
     *          if InvalidWordException occurs, it is caught and logged
     */
    private void processEnterKey() {
        if (model.getTargetWord() != null) {
            if (currentInputWord.length() == model.getTargetWord().length()) {
                String wordToSubmit = currentInputWord.toString();
                currentInputWord.setLength(0);
                view.updateInputDisplay("");
                view.setMessage("Submitting: " + wordToSubmit);

                try {
                    model.tick(wordToSubmit);
                } catch (InvalidWordException e) {
                    System.err.println("Controller caught InvalidWordException: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.err.println("Controller caught RuntimeException: " + e.getMessage());
                }
            } else {
                view.setMessage("Word must be " + model.getTargetWord().length() + " letters long.");
            }
        } else {
            view.setMessage("Game not initialized. Start a new game.");
        }
    }

    /**
     * Processes Backspace/Delete key.
     * Removes last character from input buffer.
     *
     * @pre.    currentInputWord.length() > 0
     * @post.   if condition met: last character is removed
     *          input display is updated
     */
    private void processBackspaceKey() {
        if (currentInputWord.length() > 0) {
            currentInputWord.setLength(currentInputWord.length() - 1);
            view.updateInputDisplay(currentInputWord.toString());
        }
    }

    /**
     * Handles show solution path checkbox state change.
     * Creates/disposes PathSolutionView window.
     *
     * @pre.    showPath is either true or false
     * @post.   model.setShowPathFlag(showPath) is called
     *          if showPath is true and solution path exists: PathSolutionView is created and shown
     *          if showPath is false or path is invalid: existing PathSolutionView is disposed
     *
     * @param showPath New checkbox state
     */
    public void handleShowPathFlag(boolean showPath) {
        model.setShowPathFlag(showPath);

        if (showPath) {
            ArrayList<String> solutionPath = model.getFullSolutionPath();
            String targetWord = model.getTargetWord();
            ArrayList<String> dictionary = model.getDictionary();

            if (solutionPath == null || solutionPath.size() < 2) {
                view.setMessage("No solution path found for current words.");
                view.setShowPathSelected(false);
                return;
            }

            PathSolutionView pathSolutionWindow = new PathSolutionView(solutionPath, targetWord, dictionary);
            view.setPathSolutionWindow(pathSolutionWindow);
            pathSolutionWindow.setVisible(true);
        } else {
            PathSolutionView pathWindow = view.getPathSolutionWindow();
            if (pathWindow != null) {
                pathWindow.dispose();
                view.setPathSolutionWindow(null);
            }
        }
        view.requestFocusInWindow();
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    /**
     * Processes physical keyboard key presses.
     * Delegates to appropriate handler methods.
     *
     * @pre.    physicalKeyboardProcessingEnabled == true
     * @post.   if key is A-Z: processLetterKey is called
     *          if key is ENTER: processEnterKey is called
     *          if key is BACKSPACE/DELETE: processBackspaceKey is called
     *          focus is requested in window
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (!physicalKeyboardProcessingEnabled) return;

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            processEnterKey();
        } else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE ||
                e.getKeyCode() == KeyEvent.VK_DELETE) {
            processBackspaceKey();
        } else if (e.getKeyCode() >= KeyEvent.VK_A &&
                e.getKeyCode() <= KeyEvent.VK_Z) {
            char letter = (char) e.getKeyCode();
            processLetterKey(letter);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}
}