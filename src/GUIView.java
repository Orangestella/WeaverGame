import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

// Ensure these classes are in the correct package or imported correctly
// import your_package_name.GameState;
// import your_package_name.ValidationResult;
// import your_package_name.LetterState;
// import your_package_name.Notification;
// import your_package_name.GUIController; // Assuming the controller class name


public class GUIView extends JFrame implements Observer {

    private static final int WORD_LENGTH = 4; // As per requirements
    private static final int MAX_DISPLAY_ROWS = 10; // Example: Display up to 10 words without scrolling initially

    private JLabel initialWordLabel;
    private JLabel targetWordLabel;
    private JPanel gameBoardPanel; // Panel to display the word ladder
    private JPanel keyboardPanel;
    private JButton resetButton;
    private JButton newGameButton;
    private JLabel messageLabel; // To display win/error messages
    private JPanel controlPanel; // Panel for buttons and flags
    private JCheckBox showErrorsCheckBox;
    private JCheckBox randomWordCheckBox;
    private JCheckBox showPathCheckBox; // Checkbox for showing the path

    private GUIController controller; // Reference to the controller

    // We might use a list of JLabels for each letter in each word (optional, if dynamic coloring is needed without recreating panels)
    // private List<List<JLabel>> wordDisplayLabels;


    /**
     * Constructs the GUI view for the Weaver game.
     */
    public GUIView() {
        super("Weaver Game"); // Window title
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout()); // Use BorderLayout for the main frame

        // --- Top Panel: Initial and Target Words ---
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new FlowLayout());
        initialWordLabel = new JLabel("Start Word: ____");
        targetWordLabel = new JLabel("Target Word: ____");
        topPanel.add(initialWordLabel);
        topPanel.add(Box.createHorizontalStrut(50)); // Add some space
        topPanel.add(targetWordLabel);
        add(topPanel, BorderLayout.NORTH);

        // --- Game Board Panel: Displays the word ladder ---
        gameBoardPanel = new JPanel();
        // Use BoxLayout to stack word panels vertically
        gameBoardPanel.setLayout(new BoxLayout(gameBoardPanel, BoxLayout.Y_AXIS));
        // Add a scroll pane in case the number of words exceeds the visible area
        JScrollPane scrollPane = new JScrollPane(gameBoardPanel);
        scrollPane.setPreferredSize(new Dimension(250, 300));
        // Set preferred size for the scroll pane to influence the center area size
        // scrollPane.setPreferredSize(new Dimension(400, 300)); // Example size, adjust as needed
        add(scrollPane, BorderLayout.CENTER);


        // --- Bottom Panel: Keyboard, Controls, and Messages ---
        JPanel bottomPanel = new JPanel(new BorderLayout());

        // Control Panel (Buttons and Flags)
        controlPanel = new JPanel(new FlowLayout());
        resetButton = new JButton("Reset");
        newGameButton = new JButton("New Game");
        messageLabel = new JLabel("Enter your first word."); // Initial message
        showErrorsCheckBox = new JCheckBox("Show Errors");
        randomWordCheckBox = new JCheckBox("Random Words");
        showPathCheckBox = new JCheckBox("Show Path"); // Add the show path checkbox

        controlPanel.add(resetButton);
        controlPanel.add(newGameButton);
        controlPanel.add(showErrorsCheckBox);
        controlPanel.add(randomWordCheckBox);
        controlPanel.add(showPathCheckBox); // Add the checkbox to the panel

        bottomPanel.add(controlPanel, BorderLayout.NORTH); // Buttons and flags above keyboard

        // Keyboard Panel
        keyboardPanel = createKeyboardPanel();
        bottomPanel.add(keyboardPanel, BorderLayout.CENTER);

        // Message Label below keyboard
        JPanel messagePanel = new JPanel(new FlowLayout());
        messageLabel.setForeground(Color.BLUE); // Optional: set message color
        messagePanel.add(messageLabel);
        bottomPanel.add(messagePanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);

        // --- Frame Setup ---
        pack(); // Adjusts window size based on components' preferred sizes
        // Optional: Set a minimum size
        // setMinimumSize(new Dimension(500, 600));
        setLocationRelativeTo(null); // Center the window
        setVisible(true);
        setFocusable(true); // Allow the frame to receive key events
        requestFocusInWindow(); // Request focus for key events upon startup

        // Buttons are initially disabled until a game starts or input is made
        resetButton.setEnabled(false);
    }

    /**
     * Sets the controller for this view. The controller will handle user interactions.
     * Event listeners are hooked up here, linking GUI component actions to controller methods.
     *
     * @param controller The GUIController instance.
     */
    public void setController(GUIController controller) {
        this.controller = controller;

        // Add action listeners for buttons and checkboxes, linking to the controller's handler methods
        resetButton.addActionListener(e -> controller.handleResetAction());
        newGameButton.addActionListener(e -> controller.handleNewGameAction());
        showErrorsCheckBox.addActionListener(e -> controller.handleShowErrorsFlag(showErrorsCheckBox.isSelected()));
        randomWordCheckBox.addActionListener(e -> controller.handleRandomWordFlag(randomWordCheckBox.isSelected()));
        showPathCheckBox.addActionListener(e -> controller.handleShowPathFlag(showPathCheckBox.isSelected())); // Link show path checkbox

        // Add a KeyListener to the frame to capture physical keyboard input
//        addKeyListener(controller.getKeyListener());

        // Add ActionListeners to virtual keyboard buttons
        for (Component panelComponent : keyboardPanel.getComponents()) {
            if (panelComponent instanceof JPanel) { // Each row is a JPanel
                JPanel rowPanel = (JPanel) panelComponent;
                for (Component buttonComponent : rowPanel.getComponents()) {
                    if (buttonComponent instanceof JButton) {
                        JButton button = (JButton) buttonComponent;
                        // Use a lambda to pass the button's text to the controller
                        button.addActionListener(e -> controller.handleVirtualKeyPress(button.getText()));
                    }
                }
            }
        }
        // Request focus again after setting controller and listeners
        requestFocusInWindow();
    }

    /**
     * Creates the panel containing the virtual keyboard buttons.
     * Arranges buttons in rows using FlowLayout within a main GridLayout panel.
     *
     * @return The JPanel for the keyboard.
     */
    private JPanel createKeyboardPanel() {
        JPanel panel = new JPanel(new GridLayout(3, 1, 5, 5)); // 3 rows of buttons, 1 column for rows

        String[] row1 = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"};
        String[] row2 = {"A", "S", "D", "F", "G", "H", "J", "K", "L"};
        String[] row3 = {"ENTER", "Z", "X", "C", "V", "B", "N", "M", "<-"}; // <- for backspace

        panel.add(createKeyboardRow(row1));
        panel.add(createKeyboardRow(row2));
        panel.add(createKeyboardRow(row3));

        return panel;
    }

    /**
     * Creates a single row of keyboard buttons.
     * Uses FlowLayout to center buttons within the row.
     *
     * @param letters The array of strings for the buttons in this row (e.g., {"Q", "W", ...}).
     * @return The JPanel for the keyboard row.
     */
    private JPanel createKeyboardRow(String[] letters) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 3)); // Centered flow with gaps
        for (String letter : letters) {
            JButton button = new JButton(letter);
            // Adjust preferred size for buttons to ensure text visibility
            if (letter.length() == 1) { // For single letter keys (A-Z)
                button.setPreferredSize(new Dimension(45, 45)); // Slightly larger size to prevent "..."
            } else { // For special keys like ENTER, <-
                button.setPreferredSize(new Dimension(80, 45)); // Wider for special keys
            }
            // Optional: Adjust font size if needed
            // button.setFont(new Font("Arial", Font.PLAIN, 16));
            row.add(button);
        }
        return row;
    }

    /**
     * Updates the game board display with the current path and validation results.
     * Clears the existing display and adds new panels for each word in the path,
     * coloring letters based on the validation results.
     *
     * @param path    The list of words in the current game path (including the initial word).
     * @param results The list of validation results for each word in the path (excluding the initial word).
     */
    private void updateGameBoard(ArrayList<String> path, ArrayList<ValidationResult> results) {
        gameBoardPanel.removeAll(); // Clear previous word panels
        // wordDisplayLabels.clear(); // Clear stored labels if used

        // DEBUG Print: Check data received
        System.out.println("DEBUG: Entering updateGameBoard.");
        System.out.println("DEBUG: Received path: " + path);
        System.out.println("DEBUG: Received results: " + results);
        System.out.println("DEBUG: Path size: " + path.size() + ", Results size: " + results.size());


        // Display the initial word (first word in the path)
        // The initial word does not have a corresponding ValidationResult from player input
        if (!path.isEmpty()) {
            JPanel initialWordPanel = createWordPanel(path.get(0), null); // Pass null for result
            gameBoardPanel.add(initialWordPanel);
            // wordDisplayLabels.add(getLetterLabels(initialWordPanel)); // Store labels
            System.out.println("DEBUG: Added initial word panel for: " + path.get(0));
        }

        // Display subsequent words with validation results
        // Iterate from the second word (index 1) in the path
        for (int i = 1; i < path.size(); i++) {
            String word = path.get(i);
            // Get the corresponding validation result (results list is one shorter than path)
            // Ensure results list has an element at index i-1
            ValidationResult result = (results != null && results.size() > i - 1) ? results.get(i - 1) : null;

            if (result != null) {
                JPanel wordPanel = createWordPanel(word, result);
                gameBoardPanel.add(wordPanel);
                // wordDisplayLabels.add(getLetterLabels(wordPanel)); // Store labels
                System.out.println("DEBUG: Added word panel for: " + word + " with result.");
            } else {
                // This case should ideally not happen if path and results are in sync after a valid input
                // Handle potential mismatch or display word without coloring if result is missing
                JPanel wordPanel = createWordPanel(word, null); // Display without coloring
                gameBoardPanel.add(wordPanel);
                System.out.println("DEBUG: Added word panel for: " + word + " without result (unexpected).");
            }
        }

        // After adding all word panels, re-layout and repaint the panel
        gameBoardPanel.revalidate(); // Re-calculates layout based on new components
        gameBoardPanel.repaint(); // Redraws the components

        System.out.println("DEBUG: Exiting updateGameBoard.");
    }

    /**
     * Creates a JPanel to display a single word, coloring letters based on ValidationResult.
     * Each letter is a JLabel with a background color.
     *
     * @param word   The word to display.
     * @param result The validation result for this word (can be null for the initial word or invalid inputs not added to results).
     * @return The JPanel displaying the word.
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        // Use FlowLayout for a single row of letters with spacing
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        // DEBUG Print: Check data received
        System.out.println("DEBUG: Inside createWordPanel for word: " + word);
        System.out.println("DEBUG: Received result: " + result);
        System.out.println("DEBUG: Letter states map: " + letterStates);


        for (int i = 0; i < word.length(); i++) {
            JLabel letterLabel = new JLabel(String.valueOf(word.charAt(i)));
            letterLabel.setPreferredSize(new Dimension(35, 35)); // Size of each letter square (slightly increased)
            letterLabel.setHorizontalAlignment(SwingConstants.CENTER); // Center text horizontally
            letterLabel.setVerticalAlignment(SwingConstants.CENTER); // Center text vertically
            letterLabel.setOpaque(true); // Make background visible

            // Set background color based on letter state from ValidationResult
            if (letterStates != null && letterStates.containsKey(i)) {
                LetterState state = letterStates.get(i);
                System.out.println("DEBUG: Index " + i + ", character '" + word.charAt(i) + "', State: " + state);
                switch (state) {
                    case CORRECT_POSITION:
                        letterLabel.setBackground(Color.GREEN); // Green for correct position
                        break;
                    case WRONG_POSITION:
                        letterLabel.setBackground(Color.YELLOW); // Yellow (or similar) for wrong position
                        break;
                    case NOT_IN_WORD:
                        letterLabel.setBackground(Color.LIGHT_GRAY); // Grey for not in word
                        break;
                    default:
                        letterLabel.setBackground(Color.WHITE); // Default background (shouldn't happen with our states)
                        break;
                }
            } else {
                // Default background if no validation result (e.g., for the initial word)
                letterLabel.setBackground(Color.WHITE);
                System.out.println("DEBUG: Index " + i + ", character '" + word.charAt(i) + "': No validation state.");
            }

            letterLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add border around each letter
            wordPanel.add(letterLabel); // Add the letter label to the word panel
        }
        System.out.println("DEBUG: Exiting createWordPanel for word: " + word);
        return wordPanel;
    }

    /**
     * Extracts the JLabel components from a word panel.
     * Useful if you need to update letter colors dynamically without recreating the panel.
     * (Currently not used in updateGameBoard as panels are recreated).
     *
     * @param wordPanel The panel displaying the word.
     * @return A list of JLabels representing the letters.
     */
    // private List<JLabel> getLetterLabels(JPanel wordPanel) {
    //     List<JLabel> labels = new ArrayList<>();
    //     for (Component comp : wordPanel.getComponents()) {
    //         if (comp instanceof JLabel) {
    //             labels.add((JLabel) comp);
    //         }
    //     }
    //     return labels;
    // }


    /**
     * Clears the game board display by removing all word panels.
     */
    private void clearGameBoard() {
        gameBoardPanel.removeAll();
        // wordDisplayLabels.clear(); // Clear stored labels if used
        gameBoardPanel.revalidate();
        gameBoardPanel.repaint();
        System.out.println("DEBUG: Game board cleared.");
    }

    /**
     * Sets the text for the message label at the bottom of the view.
     *
     * @param message The message to display (e.g., instructions, errors, win message).
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
        System.out.println("DEBUG: Message set to: " + message);
    }

    /**
     * Sets the text for the initial word label.
     *
     * @param word The initial word.
     */
    public void setInitialWord(String word) {
        initialWordLabel.setText("Start Word: " + word);
        System.out.println("DEBUG: Initial word label set to: " + word);
    }

    /**
     * Sets the text for the target word label.
     *
     * @param word The target word.
     */
    public void setTargetWord(String word) {
        targetWordLabel.setText("Target Word: " + word);
        System.out.println("DEBUG: Target word label set to: " + word);
    }

    /**
     * Enables or disables the reset button.
     *
     * @param enabled True to enable, false to disable.
     */
    public void setResetButtonEnabled(boolean enabled) {
        resetButton.setEnabled(enabled);
        System.out.println("DEBUG: Reset button enabled: " + enabled);
    }

    // --- Getters and Setters for Checkbox States (Used by Controller) ---

    public boolean isShowErrorsSelected() {
        return showErrorsCheckBox.isSelected();
    }

    public void setShowErrorsSelected(boolean selected) {
        showErrorsCheckBox.setSelected(selected);
        System.out.println("DEBUG: Show Errors checkbox set to: " + selected);
    }

    public boolean isRandomWordSelected() {
        return randomWordCheckBox.isSelected();
    }

    public void setRandomWordSelected(boolean selected) {
        randomWordCheckBox.setSelected(selected);
        System.out.println("DEBUG: Random Words checkbox set to: " + selected);
    }

    public boolean isShowPathSelected() {
        return showPathCheckBox.isSelected();
    }

    public void setShowPathSelected(boolean selected) {
        showPathCheckBox.setSelected(selected);
        System.out.println("DEBUG: Show Path checkbox set to: " + selected);
    }


    /**
     * This method is called when the observed Model changes.
     * It updates the GUI display based on the state received from the Model.
     * The state is expected to be wrapped in a Notification object.
     *
     * @param o   The observable object (WeaverModel).
     * @param arg An argument passed by the notifyObservers method (should be Notification).
     */
    @Override
    public void update(Observable o, Object arg) {
        System.out.println("DEBUG: Inside GUIView update method. Arg type: " + (arg != null ? arg.getClass().getName() : "null"));

        if (arg instanceof Notification) {
            Notification notification = (Notification) arg;
            System.out.println("DEBUG: Arg is a Notification.");

            // Update messages first
            setMessage(notification.getRuntimeWarning() != null ? notification.getRuntimeWarning() : notification.getHint());

            if (notification.containsGameState()) {
                GameState gameState = notification.getGameState();
                System.out.println("DEBUG: Notification contains GameState.");
                System.out.println("DEBUG: GameState Initial: " + gameState.getInitialWord() + ", Target: " + gameState.getTargetWord());
                System.out.println("DEBUG: GameState Path size: " + gameState.getPath().size() + ", Results size: " + gameState.getResults().size() + ", Won: " + gameState.isWon());


                // Update initial and target word labels
                setInitialWord(gameState.getInitialWord());
                setTargetWord(gameState.getTargetWord());

                // Update the game board display with the current path and results
                updateGameBoard(gameState.getPath(), gameState.getResults());

                // Enable reset button after the first player input (path size > 1)
                setResetButtonEnabled(gameState.getPath().size() > 1);

                // Handle win state
                if (gameState.isWon()) {
                    setMessage("You won the game!");
                    // Optional: Disable further input (e.g., keyboard, buttons)
                    // disableUserInput();
                }
            } else {
                System.out.println("DEBUG: Notification does not contain GameState.");
                // Handle notifications that are just messages, without a full state update
                // Messages are already set above.
            }
        } else {
            System.out.println("DEBUG: Update received unexpected argument type: " + (arg != null ? arg.getClass().getName() : "null"));
            // Handle unexpected notification types if necessary
        }

        // Ensure the window is focused to capture key events after update
        requestFocusInWindow();
    }

    // Method to disable user input after winning (optional)
    // private void disableUserInput() {
    //     keyboardPanel.setEnabled(false); // Disable keyboard panel
    //     // Disable individual keyboard buttons if necessary
    //     // Disable input text field if you have one
    // }
}