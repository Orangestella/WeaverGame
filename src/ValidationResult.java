
import java.util.Map;

public class ValidationResult {
    private final boolean valid;
    private String message;
    private Map<Integer, LetterState> letterStates;

    private boolean isValid() {
        if (letterStates == null || letterStates.isEmpty()) {
            return false;
        }
        try {
            // 先将 values() 转换为 ArrayList
            java.util.List<LetterState> statesList = new java.util.ArrayList<>(letterStates.values());
            // 然后迭代 List
            for (LetterState state : statesList) {
                if (state != LetterState.CORRECT_POSITION) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // 如果在转换或迭代过程中发生错误，打印出来以便调试
            System.err.println("Error during ValidationResult.isValid iteration: " + e.getMessage());
            e.printStackTrace(); // 打印堆栈跟踪
            return false; // 或者根据需要处理
        }
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
