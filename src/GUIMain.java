import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

/**
 * The main entry point for the GUI version of the Weaver game.
 * This class initializes the application components and handles initialization errors.
 *
 * <p><b>Class Invariant:</b>
 * <ul>
 *   <li>{@code model} is initialized successfully or the application terminates</li>
 *   <li>{@code view} is created and displayed properly or the application terminates</li>
 *   <li>{@code controller} is registered with the view and model</li>
 *   <li>All GUI operations run on the Event Dispatch Thread (EDT)</li>
 * </ul>
 */
public class GUIMain {

    /**
     * The main entry point for the GUI version of the Weaver game.
     *
     * @pre.    System supports graphical interface
     *          args may be null or unused
     * @post.   If initialization succeeds:
     *          - model is initialized with valid dictionary
     *          - view is created and visible
     *          - controller is registered and event handlers are active
     *          - GUI runs on EDT
     *          If initialization fails:
     *          - Error dialog is shown
     *          - Application exits with non-zero status
     *
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                WeaverModel model = new WeaverModel();
                GUIView view = new GUIView();
                GUIController controller = new GUIController(model, view);
                model.initialize();

            } catch (IOException e) {
                JOptionPane.showMessageDialog(null,
                        "Error loading dictionary file: " + e.getMessage(),
                        "Initialization Error",
                        JOptionPane.ERROR_MESSAGE);
                // Exit the application if the dictionary cannot be loaded
                System.exit(1);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred during startup: " + e.getMessage(),
                        "Initialization Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}