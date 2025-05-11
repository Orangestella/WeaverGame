import exceptions.InvalidWordException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class WeaverModelTest {
    WeaverModel model;

    @Before
    public void setUp() throws Exception {
        // Initialize model: load dictionary, generate initial word and target word
        model = new WeaverModel();
        model.initialize(); // Initialize game state (default is fixed word mode)
    }

    @After
    public void tearDown() {
        // Clean up model instance after each test
        model = null;
    }

    @Test
    public void testFixedNoError() {
        // Test fixed word mode + no error display + no path display

        model.setRandomWordFlag(false);     // Set to fixed word mode
        model.setShowErrorsFlag(false);      // Disable error hints
        model.setShowPathFlag(false);       // Disable path display
        model.updateStrategy();              // Update strategy (should use FixedStrategyFactory)
        model.updateValidator();             // Update validator (without WithWarning)

        ValidationResult validationResult;

        // Verify initial and target words are EAST and WEST
        assertEquals("EAST", model.getInitialWord());
        assertEquals("WEST", model.getTargetWord());

        // Input WAST, verify validity and check letter states
        validationResult = model.tick("WAST");
        assertFalse(validationResult.getValid());         // Not victory
        assertEquals("NULL", validationResult.getMessage()); // No message when showErrorsFlag is false
        assertEquals(4, validationResult.getLetterStates().size()); // Ensure 4 letter states returned

        // Check WAST letter states vs WEST:
        // W (0): Correct position
        // A (1): Not in target word
        // S (2): Correct position
        // T (3): Correct position
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(0));
        assertEquals(LetterState.NOT_IN_WORD, validationResult.getLetterStates().get(1));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(2));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(3));

        // Input WEST (target word), verify victory
        validationResult = model.tick("WEST");
        assertTrue(validationResult.getValid());
        assertTrue(model.isWon());

        // Check all letters in correct position
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(0));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(1));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(2));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(3));

        // Verify path recording (EAST → WAST → WEST)
        ArrayList<String> path = model.getCurrentPath();
        assertEquals(path.size(), 3);
        assertEquals(model.getInitialWord(), path.get(0));
        assertEquals("WAST", path.get(1));
        assertEquals(model.getTargetWord(), path.get(2));

        // After resetGame(), verify path resets to contain only start word
        model.resetGame();
        assertFalse(model.isWon());
        path = model.getCurrentPath();
        assertEquals(path.size(), 1);
        assertEquals(model.getInitialWord(), path.get(0));

        // Get full path and verify length (EAST → WAST → WEST)
        path = model.getFullSolutionPath();
        assertEquals(path.size(), 3);

        // Game remains in not-won state
        assertFalse(model.isWon());
    }

    @Test
    public void testRandomWithPathAndErrors() {
        // Test random word mode + path display + error messages

        model.setRandomWordFlag(true);   // Set to random word mode
        model.setShowErrorsFlag(true);   // Enable error hints
        model.updateStrategy();           // Should use RandomStrategyFactory + WithPath
        model.updateValidator();          // Should use WithWarning wrapper

        // Get current initial and target words
        String initialWord = model.getInitialWord();
        String targetWord = model.getTargetWord();

        // Initial and target words should be non-null and different
        assertNotNull(initialWord);
        assertNotNull(targetWord);
        assertNotEquals(initialWord, targetWord);

        // Get full solution path (should exist)
        ArrayList<String> fullPath = model.getFullSolutionPath();
        assertNotNull(fullPath);
        assertTrue(fullPath.size() > 1); // At least 2 steps (initial + target)

        // Submit first intermediate word from path, verify Continue message
        ValidationResult result = model.tick(fullPath.get(1));
        assertFalse(result.getValid());
        assertEquals("Continue", result.getMessage());

        // Submit remaining words step by step
        for (int i = 2; i < fullPath.size(); i++) {
            result = model.tick(fullPath.get(i));
            if (i < fullPath.size() - 1) {
                // Previous steps show Continue
                assertFalse(result.getValid());
                assertEquals("Continue", result.getMessage());
            } else {
                // Final step should win
                assertTrue(result.getValid());
                assertEquals("You win the game!", result.getMessage());
                assertTrue(model.isWon());
            }
        }

        // Verify final path matches fullPath exactly
        ArrayList<String> currentPath = model.getCurrentPath();
        assertEquals(fullPath.size(), currentPath.size());
        for (int i = 0; i < fullPath.size(); i++) {
            assertEquals(fullPath.get(i), currentPath.get(i)); // Each word should match
        }

        // Reset game, verify path resets to only initial word
        model.resetGame();
        assertFalse(model.isWon());
        assertEquals(1, model.getCurrentPath().size());
        assertEquals(initialWord, model.getCurrentPath().get(0));

        // Re-initialize game, verify new words are different (random mode)
        model.initialize();
        assertNotSame(model.getInitialWord(), initialWord);  // Initial word should change
        assertNotSame(model.getTargetWord(), targetWord);    // Target word should change
    }

    @Test
    public void testFixedException() {
        // Test exception handling in fixed word mode

        model.setRandomWordFlag(false);   // Fixed word mode
        model.setShowErrorsFlag(false);   // Disable error hints
        model.setShowPathFlag(false);     // Hide path window
        model.updateStrategy();           // Use FixedStrategyFactory

        // Verify initial and target words are EAST and WEST
        assertEquals("EAST", model.getInitialWord());
        assertEquals("WEST", model.getTargetWord());

        // Try invalid dictionary words
        try {
            model.tick("AAAA");
        } catch (InvalidWordException e) {
            assertEquals(e.getMessage(), "This word is not in the dictionary.");
        }

        // Try CAT (invalid length)
        try {
            model.tick("CAT");
        } catch (InvalidWordException e) {
            assertEquals(e.getMessage(), "This word is not in the dictionary.");
        }

        // Try MODAL (invalid length)
        try {
            model.tick("MODAL");
        } catch (InvalidWordException e) {
            assertEquals(e.getMessage(), "This word is not in the dictionary.");
        }

        // Input lowercase wast (valid)
        model.tick("wast");

        // Try duplicate WAST (should fail due to single-letter rule)
        try {
            model.tick("WAST");
        } catch (InvalidWordException e) {
            assertEquals("Word must differ by exactly one letter from the previous word.", e.getMessage());
        }

        // Try PURE (violates single-letter rule)
        try {
            model.tick("PURE");
        } catch (InvalidWordException e) {
            assertEquals("Word must differ by exactly one letter from the previous word.", e.getMessage());
        }

        // Verify path won't be influenced by invalid input
        assertEquals(2, model.getCurrentPath().size());
    }
}