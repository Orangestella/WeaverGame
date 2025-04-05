package exceptions;

public class InvalidWordException extends IllegalArgumentException{
    public InvalidWordException(String message){
        super(message);
    }
}
