package be.bagofwords.util;

public class ExecutionResult {

    private int returnCode;
    private String stdOut;
    private String errorOut;

    public ExecutionResult(int returnCode, String stdOut, String errOut) {
        this.returnCode = returnCode;
        this.stdOut = stdOut;
        this.errorOut = errOut;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public String getStdOut() {
        return stdOut;
    }

    public void setStdOut(String stdOut) {
        this.stdOut = stdOut;
    }

    public String getErrorOut() {
        return errorOut;
    }

    public void setErrorOut(String errorOut) {
        this.errorOut = errorOut;
    }
}
