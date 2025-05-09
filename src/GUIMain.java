import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.IOException;

// Ensure these classes are in the correct package or imported correctly
// import your_package_name.WeaverModel;
// import your_package_name.GUIView;
// import your_package_name.GUIController;

public class GUIMain {

    /**
     * The main entry point for the GUI version of the Weaver game.
     * @param args Command line arguments (not used in this application).
     */
    public static void main(String[] args) {
        // Run the GUI creation on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Create the Model
                WeaverModel model = new WeaverModel();

                // 2. Create the View
                GUIView view = new GUIView();

                // 3. Create the Controller, linking Model and View
                GUIController controller = new GUIController(model, view);

                // 4. Initialize the Model to set up the first game state
                // This will trigger the first update to the View via the Observer pattern.
                model.initialize();

                // The View's JFrame is set to be visible in its constructor,
                // so the GUI window will appear after initialization.

            } catch (IOException e) {
                // Handle potential error during dictionary loading
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "Error loading dictionary file: " + e.getMessage(),
                        "Initialization Error",
                        JOptionPane.ERROR_MESSAGE);
                // Exit the application if the dictionary cannot be loaded
                System.exit(1);
            } catch (Exception e) {
                // Catch any other unexpected errors during initialization
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "An unexpected error occurred during startup: " + e.getMessage(),
                        "Initialization Error",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}