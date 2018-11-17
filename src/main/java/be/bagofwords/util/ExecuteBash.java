package be.bagofwords.util;

import java.io.IOException;

public class ExecuteBash {

    public static String exec(String bashCommand) throws IOException, InterruptedException {
        ExecutionResult res = execWithResult(bashCommand);
        if (!res.isSuccess()) {
            throw new RuntimeException("Failed to execute \"" + bashCommand + "\" " + res.getErrorOut());
        }
        return res.getStdOut();
    }

    public static ExecutionResult execWithResult(String bashCommand) throws IOException, InterruptedException {
        return ExecuteProcess.exec("/bin/bash", "-c", bashCommand);
    }
}
