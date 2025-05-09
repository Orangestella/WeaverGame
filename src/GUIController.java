

public class GUIController {

    private WeaverModel model;
    private GUIView view;
    private StringBuilder currentInputWord; // To build the word from keyboard input

    public GUIController(WeaverModel model, GUIView view) {
        this.model = model;
        this.view = view;
        this.currentInputWord = new StringBuilder();

        // The View should observe the Model
        model.addObserver(view);

        // Set the controller in the view so the view can hook up event listeners
        view.setController(this);

        // Initialize the view with the initial game state
        // This will be done automatically when the model notifies its observers upon initialization
        // model.initialize(); // Ensure model.initialize() is called somewhere, e.g., in GUIMain
    }

    /**
     * Handles the action when the "Reset" button is clicked.
     * Resets the current game state while keeping the same words.
     */
    public void handleResetAction() {
        // Reset the game state in the model without generating new words
        model.resetGame();
        // Clear the current input word buffer in the controller
        currentInputWord.setLength(0);
        // Update the message in the view (already done by notifyUpdate in resetGame)
        // view.setMessage("Game reset. Enter your first word.");
        // Disable the reset button initially (until first valid input)
        view.setResetButtonEnabled(false);
        // Request focus back to the frame for key input
        view.requestFocusInWindow();
    }

    /**
     * Handles the action when the "New Game" button is clicked.
     * This is essentially the same as reset in this game's context.
     */
    public void handleNewGameAction() {
        // Start a new game by re-initializing the model
        model.initialize();
        // Clear the current input word in the controller
        currentInputWord.setLength(0);
        // Update the message in the view
        view.setMessage("New game started. Enter your first word.");
        // Disable the reset button initially
        view.setResetButtonEnabled(false);
        // Request focus back to the frame for key input
        view.requestFocusInWindow();
    }

    /**
     * Handles the state change of the "Show Errors" checkbox.
     * @param showErrors The new state of the checkbox (true if selected).
     */
    public void handleShowErrorsFlag(boolean showErrors) {
        // Update the flag in the model
        model.setShowErrorsFlag(showErrors);
        // Re-initialize validators in the model to apply the new flag state
        model.updateValidator();
        // Request focus back to the frame for key input
        view.requestFocusInWindow();
    }

    /**
     * Handles the state change of the "Random Words" checkbox.
     * @param randomWords The new state of the checkbox (true if selected).
     */
    public void handleRandomWordFlag(boolean randomWords) {
        // Update the flag in the model
        model.setRandomWordFlag(randomWords);
        // Re-initialize strategy factory and strategy in the model
        model.updateStrategy();
        // Request focus back to the frame for key input
        view.requestFocusInWindow();
    }

    /**
     * Handles a key press event from the virtual keyboard.
     * @param keyText The text of the button pressed.
     */
    public void handleVirtualKeyPress(String keyText) {
        // Handle special keys first
        if ("ENTER".equals(keyText)) {
            processEnterKey();
        } else if ("<-".equals(keyText)) { // Backspace
            processBackspaceKey();
        } else if (keyText.length() == 1 && Character.isLetter(keyText.charAt(0))) {
            // Handle letter keys
            processLetterKey(keyText.charAt(0));
        }
        // Request focus back to the frame for key input
        view.requestFocusInWindow();
    }

//    /**
//     * Gets a KeyListener for handling physical keyboard input.
//     * @return The KeyListener instance.
//     */
//    public KeyListener getKeyListener() {
//        return new KeyAdapter() {
//            @Override
//            public void keyPressed(KeyEvent e) {
//                int keyCode = e.getKeyCode();
//                if (keyCode == KeyEvent.VK_ENTER) {
//                    processEnterKey();
//                } else if (keyCode == KeyEvent.VK_BACK_SPACE) {
//                    processBackspaceKey();
//                } else if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
//                    processLetterKey(e.getKeyChar());
//                }
//                // Request focus back to the frame after processing key press
//                view.requestFocusInWindow();
//            }
//        };
//    }

    /**
     * Processes a letter key press.
     * Appends the letter to the current input word if the length is less than 4.
     * @param letter The pressed letter character.
     */
    private void processLetterKey(char letter) {
        if (currentInputWord.length() < 4) { // Limit input to 4 letters
            currentInputWord.append(Character.toUpperCase(letter));
            view.setMessage("Current input: " + currentInputWord.toString()); // Simple message display
        }
    }

    /**
     * Processes the Enter key press.
     * Submits the current input word to the model if it has 4 letters.
     */
    private void processEnterKey() {
        if (currentInputWord.length() == 4) {
            String wordToSubmit = currentInputWord.toString();
            currentInputWord.setLength(0); // Clear the input buffer
            // view.updateCurrentInputDisplay(""); // Clear input display in view
            view.setMessage("Submitting: " + wordToSubmit); // Simple message display
            model.tick(wordToSubmit); // Submit the word to the model
        } else {
            view.setMessage("Please enter a 4-letter word.");
        }
    }

    /**
     * Processes the Backspace key press.
     * Removes the last character from the current input word.
     */
    private void processBackspaceKey() {
        if (!currentInputWord.isEmpty()) {
            currentInputWord.setLength(currentInputWord.length() - 1);
            // Optionally update a display of the current input word in the view
            // view.updateCurrentInputDisplay(currentInputWord.toString());
            view.setMessage("Current input: " + currentInputWord.toString()); // Simple message display
        }
    }

    /**
     * Handles the state change of the "Show Path" checkbox.
     * @param showPath The new state of the checkbox (true if selected).
     */
    public void handleShowPathFlag(boolean showPath) {
        // 更新 Model 中的 showPathFlag
        model.setShowPathFlag(showPath);

        // 触发 Model 通知观察者，更新显示。
        // Model 的 notifyUpdate 方法现在会根据 showPathFlag 来决定显示玩家进度还是完整路径。
        model.notifyObserversWithCurrentState(); // 调用 Model 中的新公共方法

        // 将焦点请求回主窗口以确保键盘输入正常工作
        view.requestFocusInWindow();
    }
}