import exceptions.InvalidWordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class WeaverModel {
    private ArrayList<String> dictionary;
    private ArrayList<String> currentPath;
    private ArrayList<ValidationResult> resultsPath;
    private String initialWord;
    private String targetWord;
    private StrategyFactory strategyFactory;
    private WordGenerationStrategy wordGenerationStrategy;

    private boolean showErrorsFlag;
    private boolean showPathFlag;
    private boolean randomWordFlag;

    private WordValidator validator;


    public WeaverModel() throws IOException {
        loadDictionary();
        this.validator = showErrorsFlag
                ? new WithWarning(new BasicValidator())
                : new BasicValidator();
        updateStrategy();
    }

    private void loadDictionary() throws IOException {
        dictionary = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/dictionary.txt"))))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.length() == 4) {
                    dictionary.add(line.toUpperCase());
                }
            }
        }
    }

    public void newGame() {
        String[] words = wordGenerationStrategy.generateWords(dictionary);
        this.initialWord = words[0];
        this.targetWord = words[1];
        currentPath.clear();
        currentPath.add(initialWord);
        //notifyUpdate();
    }

    public void submitWord(String word) {
        word = word.toUpperCase();
        try {
            ValidationResult result = validator.validate(word, this.targetWord, this.dictionary);
            currentPath.add(word);
            resultsPath.add(result);
        } catch (InvalidWordException e){
            System.err.println(e.getMessage());
        }
    }

    private void checkWinCondition() {
        if (currentPath.get(currentPath.size()-1).equals(targetWord)) {
//            setChanged();
//            notifyObservers(new GameEvent(GameEvent.Type.WIN));
        }
    }
//
//    private void notifyUpdate() {
//        setChanged();
//        notifyObservers(new GameEvent(GameEvent.Type.UPDATE));
//    }
//
    // Getters and setters
    public void updateStrategy() {
        StrategyFactory factory = randomWordFlag ?
                new RandomStrategyFactory() :
                new FixedStrategyFactory("MILE", "PARK");

        this.wordGenerationStrategy = factory.createStrategy(dictionary);
    }
}
