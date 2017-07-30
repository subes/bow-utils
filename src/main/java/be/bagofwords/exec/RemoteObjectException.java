package be.bagofwords.exec;

public class RemoteObjectException extends RuntimeException {

    public RemoteObjectException(String message, Exception exp) {
        super(message, exp);
    }

}
