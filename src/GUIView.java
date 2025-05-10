import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * The main graphical user interface for the Weaver game.
 * Displays game state, handles user input from buttons,
 * and updates display based on game model changes.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code controller} ≠ null ∧ all UI components are initialized</li>
 *   <li>{@code pathSolutionWindow} is either null or a valid PathSolutionView instance</li>
 *   <li>{@code gameBoardPanel} always reflects the current game state from the model</li>
 *   <li>{@code virtual keyboard} buttons are enabled/disabled based on game state</li>
 * </ul>
 */
public class GUIView extends JFrame implements Observer {
    // UI Components
    private JLabel initialWordLabel;  // Label showing start word
    private JLabel targetWordLabel;   // Label showing target word
    private JPanel gameBoardPanel;    // Panel displaying the path of words
    private JPanel keyboardPanel;     // Virtual keyboard panel
    private JButton resetButton;      // Reset button
    private JButton newGameButton;    // New Game button
    private JLabel messageLabel;      // Status message label
    private JPanel controlPanel;      // Panel containing buttons and checkboxes
    private JCheckBox showErrorsCheckBox;  // Checkbox to toggle error messages
    private JCheckBox randomWordCheckBox;  // Checkbox to toggle random word mode
    private JCheckBox showPathCheckBox;    // Checkbox to show solution path
    private GUIController controller;      // Reference to the controller
    // Input display
    private JLabel currentInputDisplayLabel;  // Shows current player input
    // Solution Path Window
    private PathSolutionView pathSolutionWindow;  // Reference to solution path window
    // Keyboard listener
    private KeyListener gameKeyListener;  // Listener for physical keyboard events

    /**
     * Constructs the GUI view for the Weaver game.
     * Initializes all components and sets up layout.
     *
     * @pre.    System supports graphical interface
     * @post.   All UI components (labels, panels, buttons) are created and added to frame
     *          Frame is visible with default size and centered position
     *          Initial button states are set (reset disabled)
     */
    public GUIView() {
        super("Weaver Game");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        // Top panel: Start Word & Target Word
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        initialWordLabel = new JLabel("Start Word: ____");
        targetWordLabel = new JLabel("Target Word: ____");
        topPanel.add(initialWordLabel);
        topPanel.add(Box.createHorizontalStrut(50));
        topPanel.add(targetWordLabel);
        add(topPanel, BorderLayout.NORTH);
        // Center panel: Game board (word path)
        gameBoardPanel = new JPanel();
        gameBoardPanel.setLayout(new BoxLayout(gameBoardPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(gameBoardPanel);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        add(scrollPane, BorderLayout.CENTER);
        // Bottom panel: Controls + Keyboard
        JPanel bottomPanel = new JPanel(new BorderLayout());
        // Control panel: Buttons and checkboxes
        controlPanel = new JPanel(new FlowLayout());
        resetButton = new JButton("Reset");
        newGameButton = new JButton("New Game");
        showErrorsCheckBox = new JCheckBox("Show Errors");
        randomWordCheckBox = new JCheckBox("Random Words");
        showPathCheckBox = new JCheckBox("Show Solution Path");
        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        controlPanel.add(showErrorsCheckBox);
        controlPanel.add(randomWordCheckBox);
        controlPanel.add(showPathCheckBox);
        bottomPanel.add(controlPanel, BorderLayout.NORTH);
        // Virtual keyboard and input display
        keyboardPanel = createKeyboardPanel();
        JPanel keyboardAndInputPanel = new JPanel(new BorderLayout());
        keyboardAndInputPanel.add(keyboardPanel, BorderLayout.CENTER);
        currentInputDisplayLabel = new JLabel("Current Input: ");
        JPanel inputDisplayPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        inputDisplayPanel.add(currentInputDisplayLabel);
        keyboardAndInputPanel.add(inputDisplayPanel, BorderLayout.NORTH);
        bottomPanel.add(keyboardAndInputPanel, BorderLayout.CENTER);
        // Message label panel
        JPanel messagePanel = new JPanel(new FlowLayout());
        messageLabel = new JLabel("Enter your first word.");
        messageLabel.setForeground(Color.BLUE);
        messagePanel.add(messageLabel);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        setFocusable(true);
        requestFocusInWindow();
        // Initial button states
        resetButton.setEnabled(false);
    }

    /**
     * Sets the controller and attaches event listeners to GUI components.
     *
     * @pre.    controller ≠ null
     * @post.   All buttons and checkboxes are linked to controller methods
     *          physical keyboard listener is registered
     *          virtual keyboard buttons are linked to handleVirtualKeyPress
     *
     * @param controller The GUIController instance
     */
    public void setController(GUIController controller) {
        this.controller = controller;
        resetButton.addActionListener(e -> this.controller.handleResetAction());
        newGameButton.addActionListener(e -> this.controller.handleNewGameAction());
        showErrorsCheckBox.addActionListener(e -> this.controller.handleShowErrorsFlag(showErrorsCheckBox.isSelected()));
        randomWordCheckBox.addActionListener(e -> this.controller.handleRandomWordFlag(randomWordCheckBox.isSelected()));
        showPathCheckBox.addActionListener(e -> this.controller.handleShowPathFlag(showPathCheckBox.isSelected()));
        if (this.controller != null) {
            this.gameKeyListener = this.controller.getKeyListener();
            this.addKeyListener(this.gameKeyListener);
        }
        if (keyboardPanel != null) {
            for (Component panelComponent : keyboardPanel.getComponents()) {
                if (panelComponent instanceof JPanel) {
                    JPanel rowPanel = (JPanel) panelComponent;
                    for (Component buttonComponent : rowPanel.getComponents()) {
                        if (buttonComponent instanceof JButton) {
                            JButton button = (JButton) buttonComponent;
                            button.addActionListener(e -> this.controller.handleVirtualKeyPress(button.getText()));
                        }
                    }
                }
            }
        }
        requestFocusInWindow();
    }

    /**
     * Creates the virtual keyboard panel.
     *
     * @post.   returns JPanel with 3 rows of letter/del/enter buttons
     *          buttons have correct dimensions and layout
     *
     * @return A JPanel containing rows of JButton keys.
     */
    private JPanel createKeyboardPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5));
        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "<-"};
        panel.add(createKeyboardRow(row1));
        panel.add(createKeyboardRow(row2));
        panel.add(createKeyboardRow(row3));
        return panel;
    }

    /**
     * Creates a single row of keyboard buttons.
     *
     * @pre.    letters ≠ null ∧ not empty
     * @post.   returns JPanel with FlowLayout containing buttons for each letter
     *          buttons have correct preferred sizes
     *
     * @param letters Array of strings representing keys in this row.
     * @return A JPanel representing one row of the keyboard.
     */
    private JPanel createKeyboardRow(String[] letters) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3));
        for (String letter : letters) {
            JButton button = new JButton(letter);
            if (letter.length() == 1 || letter.equals("<-")) {
                button.setPreferredSize(new Dimension(45, 45));
            } else {
                button.setPreferredSize(new Dimension(80, 45));
            }
            row.add(button);
        }
        return row;
    }

    /**
     * Updates the game board with the current word path and validation results.
     *
     * @pre.    path may be null or non-empty
     *          results may be null or match path size - 1
     * @post.   gameBoardPanel is cleared and repopulated with word panels
     *          each word is displayed with appropriate letter colors
     *
     * @param path    List of words entered by the player
     * @param results Validation results for each step
     */
    private void updateGameBoard(ArrayList<String> path, ArrayList<ValidationResult> results) {
        gameBoardPanel.removeAll();
        if (path != null && !path.isEmpty()) {
            JPanel initialWordPanel = createWordPanel(path.get(0), null);
            gameBoardPanel.add(initialWordPanel);
        }
        for (int i = 1; i < (path != null ? path.size() : 0); i++) {
            String word = path.get(i);
            ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;
            JPanel wordPanel = createWordPanel(word, result);
            gameBoardPanel.add(wordPanel);
        }
        gameBoardPanel.revalidate();
        gameBoardPanel.repaint();
    }

    /**
     * Creates a visual representation of a single word with colored letters.
     *
     * @pre.    word ≠ null (maybe empty)
     *          result may be null or contain valid LetterState values
     * @post.   returns JPanel with colored labels for each character
     *          colors correspond to CORRECT_POSITION (GREEN), WRONG_POSITION (YELLOW), NOT_IN_WORD (GRAY)
     *
     * @param word   The word to display
     * @param result Validation result used to color the letters
     * @return A JPanel containing colored letter labels
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;
        if (word == null) {
            return wordPanel;
        }
        for (int i = 0; i < word.length(); i++) {
            char character = word.charAt(i);
            JLabel letterLabel = new JLabel(String.valueOf(character));
            letterLabel.setPreferredSize(new Dimension(35, 35));
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER);
            letterLabel.setVerticalAlignment(SwingConstants.CENTER);
            letterLabel.setOpaque(true);
            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            LetterState state = (letterStates != null && letterStates.containsKey(i)) ? letterStates.get(i) : null;
            Color bgColor;
            if (state != null) {
                switch (state) {
                    case CORRECT_POSITION:
                        bgColor = Color.GREEN;
                        break;
                    case WRONG_POSITION:
                        bgColor = Color.YELLOW;
                        break;
                    case NOT_IN_WORD:
                        bgColor = Color.LIGHT_GRAY;
                        break;
                    default:
                        bgColor = Color.WHITE;
                        break;
                }
            } else {
                bgColor = Color.WHITE;
            }
            letterLabel.setBackground(bgColor);
            wordPanel.add(letterLabel);
        }
        return wordPanel;
    }

    /**
     * Sets the status message displayed at the bottom of the view.
     *
     * @pre.    message may be null or empty (sets empty string in that case)
     * @post.   messageLabel text is updated to the provided message
     *
     * @param message The message to display
     */
    public void setMessage(String message) {
        messageLabel.setText(message != null ? message : "");
    }

    /**
     * Sets the text for the start word label.
     *
     * @pre.    word may be null (sets "____" in that case)
     * @post.   initialWordLabel text is updated to "Start Word: " + word or "____"
     *
     * @param word The start word
     */
    public void setInitialWord(String word) {
        initialWordLabel.setText("Start Word: " + (word != null ? word : "____"));
    }

    /**
     * Sets the text for the target word label.
     *
     * @pre.    word may be null (sets "____" in that case)
     * @post.   targetWordLabel text is updated to "Target Word: " + word or "____"
     *
     * @param word The target word
     */
    public void setTargetWord(String word) {
        targetWordLabel.setText("Target Word: " + (word != null ? word : "____"));
    }

    /**
     * Enables or disables the reset button.
     *
     * @pre.    enabled is either true or false
     * @post.   resetButton's enabled state matches parameter
     *
     * @param enabled True to enable, false to disable
     */
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
    }

    /**
     * Updates the display showing the current word being typed by the player.
     *
     * @pre.    currentInput may be null (sets empty string in that case)
     * @post.   currentInputDisplayLabel text is updated to "Current Input: " + currentInput
     *
     * @param currentInput The current string of characters typed
     */
    public void updateInputDisplay(String currentInput) {
        currentInputDisplayLabel.setText("Current Input: " + (currentInput != null ? currentInput : ""));
    }

    /**
     * Sets whether the 'Show Solution Path' checkbox is selected.
     *
     * @pre.    selected is either true or false
     * @post.   showPathCheckBox's selected state matches parameter
     *
     * @param selected True to select, false to deselect
     */
    public void setShowPathSelected(boolean selected) {
        showPathCheckBox.setSelected(selected);
    }

    /**
     * Called when the observed model changes.
     * Updates the GUI based on the new game state.
     *
     * @pre.    o is a WeaverModel instance ∧ arg is a Notification
     * @post.   UI components are updated based on game state in notification
     *          virtual keyboard and physical keyboard are enabled/disabled based on win status
     *
     * @param o   The observable object (WeaverModel)
     * @param arg An argument passed by notifyObservers (should be Notification)
     */
    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof Notification) {
            Notification notification = (Notification) arg;
            setMessage(notification.getRuntimeWarning() != null ? notification.getRuntimeWarning() : notification.getHint());
            if (notification.containsGameState()) {
                GameState gameState = notification.getGameState();
                setInitialWord(gameState.getInitialWord());
                setTargetWord(gameState.getTargetWord());
                updateGameBoard(gameState.getPath(), gameState.getResults());
                setResetButtonEnabled(gameState.getPath() != null && gameState.getPath().size() > 1);
                if (gameState.isWon()) {
                    if (this.controller != null) {
                        this.controller.setPhysicalKeyboardProcessingEnabled(false);
                    }
                    setVirtualKeyboardEnabled(false);
                } else {
                    if (this.controller != null) {
                        this.controller.setPhysicalKeyboardProcessingEnabled(true);
                    }
                    setVirtualKeyboardEnabled(true);
                }
            } else {
                System.err.println("Notification does not contain GameState.");
            }
        } else {
            System.err.println("Update received unexpected argument type: " + (arg != null ? arg.getClass().getName() : "null"));
        }
        requestFocusInWindow();
    }

    /**
     * Enables or disables the virtual keyboard buttons.
     *
     * @pre.    enabled is either true or false
     * @post.   all buttons in keyboardPanel are enabled/disabled accordingly
     *
     * @param enabled True to enable, false to disable
     */
    private void setVirtualKeyboardEnabled(boolean enabled) {
        if (keyboardPanel != null) {
            for (Component panelComponent : keyboardPanel.getComponents()) {
                if (panelComponent instanceof JPanel) {
                    JPanel rowPanel = (JPanel) panelComponent;
                    for (Component buttonComponent : rowPanel.getComponents()) {
                        if (buttonComponent instanceof JButton) {
                            JButton button = (JButton) buttonComponent;
                            button.setEnabled(enabled);
                        }
                    }
                }
            }
        } else {
            System.err.println("Error: keyboardPanel is null in setVirtualKeyboardEnabled.");
        }
    }

    /**
     * Gets the solution path window reference.
     *
     * @post.   returns current pathSolutionWindow (may be null)
     *
     * @return The PathSolutionView window
     */
    public PathSolutionView getPathSolutionWindow() {
        return pathSolutionWindow;
    }

    /**
     * Sets the solution path window reference.
     *
     * @pre.    pathSolutionWindow may be null (closes window) or valid instance
     * @post.   this.pathSolutionWindow == pathSolutionWindow
     *
     * @param pathSolutionWindow The new PathSolutionView window
     */
    public void setPathSolutionWindow(PathSolutionView pathSolutionWindow) {
        this.pathSolutionWindow = pathSolutionWindow;
    }
}