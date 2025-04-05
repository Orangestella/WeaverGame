import java.util.HashMap;
import java.util.Map;

public class ValidationResult {
    private final boolean valid;
    private String message;
    private Map<Integer, LetterState> letterStates;

    private boolean isValid() {
        if (letterStates == null || letterStates.isEmpty()) {
            return false;
        }
        for (LetterState state : letterStates.values()) {
            if (state != LetterState.CORRECT_POSITION) {
                return false;
            }
        }
        return true;
    }
    public String getMessage(){
        return message;
    }
    public void setMessage(String message){
        this.message = message;
    }
    public Map<Integer, LetterState> getLetterStates(){
        return letterStates;
    }
    public boolean getValid(){
        return valid;
    }

    public ValidationResult(Map<Integer, LetterState> letterStates){
        this.message = "NULL";
        this.letterStates = letterStates;
        this.valid = isValid();
    }
}
