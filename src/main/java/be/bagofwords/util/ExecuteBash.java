package be.bagofwords.util;

import java.io.IOException;

public class ExecuteBash {

    public static ExecutionResult exec(String bashCommand) throws IOException, InterruptedException {
        return ExecuteProcess.exec("/bin/bash", "-c", bashCommand);
    }
}
