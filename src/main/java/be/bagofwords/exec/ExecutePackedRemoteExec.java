package be.bagofwords.exec;

public class ExecutePackedRemoteExec extends RuntimeException {

    public ExecutePackedRemoteExec(String message, Exception exp) {
        super(message, exp);
    }

}
