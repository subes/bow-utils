package be.bagofwords.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExecuteProcess {
    public static ExecutionResult exec(String... params) throws IOException, InterruptedException {
        Process p = Runtime.getRuntime().exec(params);
        int result = p.waitFor();
        BufferedReader rdr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
        StringBuilder errOut = new StringBuilder();
        while (rdr.ready()) {
            errOut.append(rdr.readLine());
            errOut.append("\n");
        }
        rdr.close();
        rdr = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder stdOut = new StringBuilder();
        while (rdr.ready()) {
            stdOut.append(rdr.readLine());
        }
        rdr.close();
        return new ExecutionResult(result, stdOut.toString(), errOut.toString());
    }
}
