package be.bagofwords.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class ExecuteProcess {

    public static ExecutionResult exec(ProcessBuilder processBuilder) throws IOException, InterruptedException {
        return exec(Integer.MAX_VALUE, processBuilder);
    }

    public static ExecutionResult exec(int timeout, ProcessBuilder processBuilder) throws IOException, InterruptedException {
        Process p = processBuilder.start();
        StreamToStringThread errorThread = new StreamToStringThread(p.getErrorStream());
        StreamToStringThread stdThread = new StreamToStringThread(p.getInputStream());
        errorThread.start();
        stdThread.start();
        WaitForThread waitForThread = new WaitForThread(p);
        waitForThread.start();
        long started = System.currentTimeMillis();
        while (System.currentTimeMillis() - started < timeout && !waitForThread.finished) {
            Thread.sleep(20);
        }
        int exitValue = waitForThread.finished ? p.exitValue() : -1;
        if (!waitForThread.finished) {
            p.destroy();
        }
        errorThread.join();
        stdThread.join();
        if (errorThread.exp != null) {
            throw new IOException("Could not read error output stream", errorThread.exp);
        }
        if (stdThread.exp != null) {
            throw new IOException("Could not read std output stream", stdThread.exp);
        }
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

    private static class WaitForThread extends Thread {
        private final Process process;
        public boolean finished;

        public WaitForThread(Process process) {
            this.process = process;
        }

        public void run() {
            try {
                process.waitFor();
                finished = true;
            } catch (InterruptedException e) {
                finished = false;
            }
        }
    }
}
