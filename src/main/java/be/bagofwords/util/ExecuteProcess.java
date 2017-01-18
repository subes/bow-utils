package be.bagofwords.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public class ExecuteProcess {

    public static ExecutionResult exec(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        return exec(Integer.MAX_VALUE, processBuilder);
    }

    public static ExecutionResult exec(int timeout, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process p = processBuilder.start();
        boolean processTerminatedNormally = p.waitFor(timeout, TimeUnit.MILLISECONDS);
        int exitValue = processTerminatedNormally ? p.exitValue() : -1;
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
        return new ExecutionResult(exitValue, stdOut.toString(), errOut.toString());
    }

    public static ExecutionResult exec(String... params) throws IOException, InterruptedException {
        return exec(Integer.MAX_VALUE, params);
    }

    public static ExecutionResult exec(int timeout, String... params) throws IOException, InterruptedException {
        return exec(timeout, new ProcessBuilder(params));
    }
}
