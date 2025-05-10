import exceptions.InvalidWordException;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;


public class WeaverModelTest {
    WeaverModel model;

    @Before
    public void setUp() throws Exception {
        model = new WeaverModel();
    }

    @Test
    public void fixedNoError() {
        model.setRandomWordFlag(false);
        model.setShowErrorsFlag(false);
        model.setShowPathFlag(false);
        model.updateStrategy();
        model.initialize();
        ValidationResult validationResult;
        assertEquals("EAST",model.getInitialWord());
        assertEquals("WEST",model.getTargetWord());
        try {
            model.tick("AAAA");
        } catch (InvalidWordException e) {
            assertEquals(e.getMessage(),"This word is not in the dictionary.");
        }
        validationResult = model.tick("WAST");
        assertFalse(validationResult.getValid());
        assertEquals("NULL",validationResult.getMessage());
        assertEquals(validationResult.getLetterStates().size(), 4);
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(0));
        assertEquals(LetterState.NOT_IN_WORD, validationResult.getLetterStates().get(1));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(2));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(3));
        try {
            model.tick("WAST");
        } catch (InvalidWordException e) {
            assertEquals("Word must differ by exactly one letter from the previous word.",e.getMessage());
        }
        validationResult = model.tick("WEST");
        assertTrue(validationResult.getValid());
        assertTrue(model.isWon());
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(0));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(1));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(2));
        assertEquals(LetterState.CORRECT_POSITION, validationResult.getLetterStates().get(3));
        ArrayList<String> path = model.getCurrentPath();
        assertEquals(path.size(), 3);
        assertEquals(model.getInitialWord(), path.get(0));
        assertEquals("WAST", path.get(1));
        assertEquals(model.getTargetWord(), path.get(2));
        model.resetGame();
        assertFalse(model.isWon());
        path = model.getCurrentPath();
        assertEquals(path.size(), 1);
        assertEquals(model.getInitialWord(), path.get(0));
        path = model.getFullSolutionPath();
        assertEquals(path.size(), 3);
        assertFalse(model.isWon());
    }

    @Test
    public void testRandomWithPathAndErrors() {
        // 设置为随机模式并启用错误提示
        model.initialize();
        model.setRandomWordFlag(true);
        model.setShowErrorsFlag(true);
        model.updateStrategy();
        model.updateValidator();

        String initialWord = model.getInitialWord();
        String targetWord = model.getTargetWord();

        assertNotNull(initialWord);
        assertNotNull(targetWord);
        assertNotEquals(initialWord, targetWord);

        ArrayList<String> fullPath = model.getFullSolutionPath();
        assertNotNull(fullPath);
        assertTrue(fullPath.size() > 1);

        ValidationResult result = model.tick(fullPath.get(1));
        assertFalse(result.getValid());
        assertEquals("Continue", result.getMessage());

        for (int i = 2; i < fullPath.size(); i++) {
            result = model.tick(fullPath.get(i));
            if (i < fullPath.size() - 1) {
                assertFalse(result.getValid());
                assertEquals("Continue", result.getMessage());
            } else {
                assertTrue(result.getValid());
                assertEquals("You win the game!", result.getMessage());
                assertTrue(model.isWon());
            }
        }

        ArrayList<String> currentPath = model.getCurrentPath();
        assertEquals(fullPath.size(), currentPath.size());
        for (int i = 0; i < fullPath.size(); i++) {
            assertEquals(fullPath.get(i), currentPath.get(i));
        }

        // 重置游戏
        model.resetGame();
        assertFalse(model.isWon());
        assertEquals(1, model.getCurrentPath().size());
        assertEquals(initialWord, model.getCurrentPath().get(0));
    }
}