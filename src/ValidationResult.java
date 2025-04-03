import java.util.Map;

public class ValidationResult {
    private boolean valid;
    private String message;
    private Map<Integer, LetterState> letterStates;

    public boolean isValid(){
        return valid;
    }
    public String getMessage(){
        return message;
    }
    public Map<Integer, LetterState> getLetterStates(){
        return letterStates;
    }
}
