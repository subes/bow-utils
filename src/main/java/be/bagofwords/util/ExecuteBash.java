package be.bagofwords.util;

import java.io.File;
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

    public static ExecutionResult execWithResult(String bashCommand, File directory) throws IOException, InterruptedException {
        return ExecuteProcess.exec(directory, "/bin/bash", "-c", bashCommand);
    }
}
