package my.demo.exception;

public class NoCarAvailableException extends RuntimeException {

    public NoCarAvailableException(String message) {
        super(message);
    }
}
