import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * A pop-up window that displays the full solution path from the initial word to the target word.
 * Each word is displayed in a colored panel, showing how each letter matches the target word.
 */
public class PathSolutionView extends JFrame {

    // The list of words forming the solution path
    private final ArrayList<String> solutionPath;

    // The target word the player is trying to reach
    private final String targetWord;

    // Dictionary used for validating steps in the solution path
    private final ArrayList<String> dictionary;

    /**
     * Constructs a new PathSolutionView with the given solution path, target word, and dictionary.
     *
     * @param solutionPath The sequence of words from the initial word to the target word
     * @param targetWord   The final word the player should reach
     * @param dictionary   The dictionary of valid words
     */
    public PathSolutionView(ArrayList<String> solutionPath, String targetWord, ArrayList<String> dictionary) {
        this.solutionPath = solutionPath;
        this.targetWord = targetWord;
        this.dictionary = dictionary;

        // Set up the window properties
        if (solutionPath != null && !solutionPath.isEmpty()) {
            setTitle("Solution Path: " + solutionPath.get(0) + " to " + (targetWord != null ? targetWord : "____"));
        } else {
            setTitle("Solution Path");
        }

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        // Generate validation results for each step in the path
        ArrayList<ValidationResult> validationResults = PathFinder.getValidations(this.targetWord, this.solutionPath, this.dictionary);

        // Display each word in the path
        if (this.solutionPath != null && !this.solutionPath.isEmpty()) {
            add(createWordPanel(this.solutionPath.get(0), null)); // Initial word has no validation result

            for (int i = 1; i < this.solutionPath.size(); i++) {
                String currentWord = this.solutionPath.get(i);
                ValidationResult result = (validationResults != null && validationResults.size() > i - 1)
                        ? validationResults.get(i - 1) : null;

                add(createWordPanel(currentWord, result));
            }
        }

        // Add vertical glue to align content to the top
        add(Box.createVerticalGlue());

        pack();
        setLocationRelativeTo(null); // Center the window on screen
    }

    /**
     * Creates a JPanel representing a single word, with colored letters based on validation results.
     * This method is very similar to GUIView's createWordPanel method.
     *
     * @param word   The word to display
     * @param result The validation result for this word (can be null)
     * @return A JPanel containing colored labels for each letter
     */
    private JPanel createWordPanel(String word, ValidationResult result) {
        JPanel wordPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

        Map<Integer, LetterState> letterStates = (result != null) ? result.getLetterStates() : null;

        if (word == null) {
            System.err.println("Error: PathSolutionView.createWordPanel received null word.");
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

            LetterState state = null;
            if (letterStates != null && letterStates.containsKey(i)) {
                state = letterStates.get(i);
            }

            Color bgColor = Color.WHITE;

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
            }

            letterLabel.setBackground(bgColor);
            wordPanel.add(letterLabel);
        }

        return wordPanel;
    }
}