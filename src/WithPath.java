import exceptions.WordGenerationException;

import java.util.ArrayList;

public class WithPath extends  WordGenerationStrategyDecorator{
    private ArrayList<String> path;
    public WithPath(WordGenerationStrategy baseStrategy) {
        super(baseStrategy);
    }

    @Override
    public String[] generateWords(ArrayList<String> dictionary) {
        int maxAttempt = 20;
        for (int i = 0; i < maxAttempt; i++) {
            String[] wordsPair = this.getBaseStrategy().generateWords(dictionary);
            this.path = PathFinder.findPathByBFS(wordsPair[0], wordsPair[1], dictionary);
            if (!path.isEmpty()){
                return wordsPair;
            }
        }
        throw new WordGenerationException("No path found");
    }

    public ArrayList<String> getPath() {
        return (this.path != null) ? this.path : new ArrayList<>();
    }


}
