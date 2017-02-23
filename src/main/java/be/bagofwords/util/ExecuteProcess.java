package be.bagofwords.util;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ExecuteProcess {

    private static final Logger logger = LoggerFactory.getLogger(ExecuteProcess.class);

    public static ExecutionResult exec(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        return exec(Integer.MAX_VALUE, processBuilder);
    }

    public static ExecutionResult exec(int timeout, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process p = processBuilder.start();
        String psName = String.join(" ", processBuilder.command());
        logger.info("Started process " + psName);
        StreamToStringThread errorThread = new StreamToStringThread(p.getErrorStream());
        StreamToStringThread stdThread = new StreamToStringThread(p.getInputStream());
        errorThread.start();
        stdThread.start();
        boolean processTerminatedNormally = p.waitFor(timeout, TimeUnit.MILLISECONDS);
        logger.info("Process terminated " + psName);
        int exitValue = processTerminatedNormally ? p.exitValue() : -1;
        errorThread.join();
        stdThread.join();
        if (errorThread.exp != null) {
            throw new IOException("Could not read error output stream", errorThread.exp);
        }
        if (stdThread.exp != null) {
            throw new IOException("Could not read std output stream", stdThread.exp);
        }
        logger.info("Returning result for " + psName);
        return new ExecutionResult(exitValue, stdThread.output, errorThread.output);
    }

    public static ExecutionResult exec(String... params) throws IOException, InterruptedException {
        return exec(Integer.MAX_VALUE, params);
    }

    public static ExecutionResult exec(int timeout, String... params) throws IOException, InterruptedException {
        return exec(timeout, new ProcessBuilder(params));
    }

    private static class StreamToStringThread extends Thread {
        private final InputStream inputStream;
        public String output;
        public IOException exp;

        private StreamToStringThread(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try {
                output = IOUtils.toString(inputStream, "UTF-8");
            } catch (IOException exp) {
                this.exp = exp;
            }
        }
    }
}
