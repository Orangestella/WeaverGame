import java.util.ArrayList;

public class RandomStrategyFactory implements StrategyFactory{

    @Override
    public WordGenerationStrategy createStrategy(ArrayList<String> dictionary) {
        return new RandomWordStrategy(dictionary);
    }
}
