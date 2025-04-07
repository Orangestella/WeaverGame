import exceptions.InvalidWordException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Observable;

public class WeaverModel extends Observable {
    private ArrayList<String> dictionary;
    private ArrayList<String> currentPath;
    private ArrayList<ValidationResult> resultsPath;
    private String initialWord;
    private String targetWord;
    private StrategyFactory strategyFactory;
    private WordGenerationStrategy wordGenerationStrategy;
    private boolean isWon;

    private boolean showErrorsFlag;
    private boolean showPathFlag;
    private boolean randomWordFlag;

    private WordValidator validator;


    public WeaverModel() throws IOException {
        loadDictionary();
        updateValidator();
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

    public void initialize() {
        updateStrategy();
        updateValidator();
        String[] words = wordGenerationStrategy.generateWords(dictionary);
        this.initialWord = words[0];
        this.targetWord = words[1];
        this.isWon = false;
        currentPath.clear();
        currentPath.add(initialWord);
        notifyUpdate();
    }

    public void tick(String word) {
        updateStrategy();
        updateValidator();
        if(showPathFlag)
            notifyFullPathUpdate();
        else {
            word = word.toUpperCase();
            try {
                ValidationResult result = validator.validate(word, this.targetWord, this.dictionary);
                currentPath.add(word);
                resultsPath.add(result);
                this.isWon = resultsPath.get(resultsPath.size() - 1).getValid();
                notifyUpdate();
            } catch (InvalidWordException e) {
                System.err.println(e.getMessage());
            }
        }
    }


    private void notifyUpdate() {
        setChanged();
        notifyObservers(new GameState(this.initialWord, this.targetWord, this.currentPath, this.resultsPath));
    }

    private void notifyFullPathUpdate(){
        setChanged();
        ArrayList<String> fullPath = this.wordGenerationStrategy.getPath();
        try {
            if (fullPath == null)
                notifyObservers(PathFinder.completePath(this.initialWord, this.targetWord, this.dictionary, this.validator));
            else
                notifyObservers(new GameState(this.initialWord, this.targetWord, fullPath, PathFinder.getValidations(this.targetWord, fullPath, this.dictionary, this.validator)));
        }catch (RuntimeException e){
            System.err.println(e.getMessage());
        }
    }
//
    // Getters and setters
    public void updateStrategy() {
        StrategyFactory factory = randomWordFlag ?
                new RandomStrategyFactory() :
                new FixedStrategyFactory(this.dictionary.get(1), this.dictionary.get(0));

        this.wordGenerationStrategy = showPathFlag ?
                new WithPath(factory.createStrategy(dictionary)):
                factory.createStrategy(dictionary);

    }

    public void updateValidator() {
        this.validator = showErrorsFlag
                ? new WithWarning(new BasicValidator())
                : new BasicValidator();
    }

}
