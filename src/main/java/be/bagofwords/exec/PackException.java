package be.bagofwords.exec;

/**
 * Created by koen on 28/04/17.
 */
public class PackException extends RuntimeException {

    public PackException(String message) {
        super(message);
    }

    public PackException(String message, Throwable cause) {
        super(message, cause);
    }
}
