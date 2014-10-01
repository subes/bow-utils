package be.bagofwords.ui;

import be.bagofwords.util.NumUtils;
import be.bagofwords.util.Utils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public abstract class UI {

    private BufferedWriter wtr;

    private static UI object;

    private SimpleDateFormat printTimeFormat = new SimpleDateFormat("HH:mm:ss:SSS");

    public static final boolean printCaller = false;

    public static final boolean printTime = true;

    public static final boolean printMemory = true;

    public UI() {
        object = this;
    }

    public static UI getInstance() {
        if (object == null)
            setInstance(getDefaultOutputManager());
        return object;
    }

    public static void setInstance(UI ui) {
        object = ui;
    }

    public static void writeError(String msg) {
        getInstance().writeLn(ERROR, msg);
    }

    public static void writeError(String msg, Throwable e) {
        synchronized (getInstance()) {
            getInstance().writeLn(ERROR, msg);
            String[] lines = Utils.getStackTrace(e).split("\n");
            for (String line : lines) {
                getInstance().writeLn(ERROR, line);
            }
        }
    }

    public static void writeWarning(String msg) {
        getInstance().writeLn(WARNING, msg);
    }

    public static void writeHigh(String msg) {
        getInstance().writeLn(HIGH, msg);
    }

    public static void writeNormal(String msg) {
        getInstance().writeLn(NORMAL, msg);
    }

    public static void writeLow(String msg) {
        getInstance().writeLn(LOW, msg);
    }

    public static void writeDebug(String msg) {
        getInstance().writeLn(DEBUG, msg);
    }

    public static void write(String msg) {
        writeNormal(msg);
    }

    public void writeLn(Priority priority, String msg) {
        write(priority, msg + "\n");
    }

    private boolean prevNewLine = true;

    public void write(Priority priority, String msg) {
        if (print(priority)) {
            if (prevNewLine) {
                if (printCaller)
                    msg = callingClass() + msg;
                if (printTime)
                    msg = getTime() + " " + msg;
                if (printMemory)
                    msg = getMemory() + " " + msg;
            }
            prevNewLine = !msg.isEmpty() && msg.charAt(msg.length() - 1) == '\n';
            writeToFile(msg);
            writeOutput(priority, msg);
        }
    }

    private void writeToFile(String msg) {
        if (wtr != null)
            try {
                wtr.write(msg);
                wtr.flush(); // Optimize??
            } catch (IOException exp) {
                UI.write("Could not write to outputFile because of " + exp);
                wtr = null;
            }
    }

    public String getMemory() {
        String mem = NumUtils.fixedLength((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000, 4) + "Mb";
        return mem;
    }

    private String getTime() {
        return printTimeFormat.format(new Date());
    }

    //Keep in alphabetical order!
    private final String[] mainMethodNames = {"main", "run"};
    private final String[] methodNamesToIgnore = {"doAction", "doOccasionalAction", "trackProgress"};

    private String callingClass() {
        StackTraceElement[] els = Thread.currentThread().getStackTrace();
        int ind = 0;
        while (Arrays.binarySearch(mainMethodNames, els[ind].getMethodName()) < 0) {
            ind++;
        }
        String stopAt = UI.class.getCanonicalName();
        String result = "";
        String prevClass = "";
        while (!els[ind].getClassName().equals(stopAt)) {
            if (Arrays.binarySearch(methodNamesToIgnore, els[ind].getMethodName()) < 0) {
                String className = "-";
                if (!els[ind].getClassName().equals(prevClass)) {
                    prevClass = els[ind].getClassName();
                    int start = prevClass.lastIndexOf(".");
                    className = prevClass.substring(start + 1);
                }
                result += className + ":" + els[ind].getMethodName() + ":" + els[ind].getLineNumber() + "  ";
            }
            ind--;
        }
        return result;
    }

    public static String read() {
        return getInstance().readInputLine();
    }

    public static String read(String msg) {
        getInstance().writeLn(NORMAL, msg);
        return getInstance().readInputLine();
    }

    public static boolean readBoolean(String msg) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine();
            if (input.equalsIgnoreCase("yes"))
                return true;
            else if (input.equalsIgnoreCase("1"))
                return true;
            else if (input.equalsIgnoreCase("true"))
                return true;
            else if (input.equalsIgnoreCase("0"))
                return false;
            else if (input.equalsIgnoreCase("no"))
                return false;
            else if (input.equalsIgnoreCase("false"))
                return false;
            getInstance().writeLn(NORMAL, "Please enter yes, no, true, false, 0 or 1.");
        }
    }

    public static boolean readBoolean(String msg, boolean defaultValue) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine();
            if (input.equalsIgnoreCase("yes"))
                return true;
            else if (input.equalsIgnoreCase("1"))
                return true;
            else if (input.equalsIgnoreCase("true"))
                return true;
            else if (input.equalsIgnoreCase("0"))
                return false;
            else if (input.equalsIgnoreCase("no"))
                return false;
            else if (input.equalsIgnoreCase("false"))
                return false;
            else if (input.trim().equals(""))
                return defaultValue;
            getInstance().writeLn(NORMAL, "Please enter yes, no, true, false, 0 or 1.");
        }
    }

    /**
     * Time to wait indicates the number of ms to wait before choosing the default answer.
     */

    public static boolean readBoolean(String msg, boolean defaultVal, long timeToWait) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine(timeToWait);
            if (input == null)
                return defaultVal;
            else if (input.equalsIgnoreCase("yes"))
                return true;
            else if (input.equalsIgnoreCase("1"))
                return true;
            else if (input.equalsIgnoreCase("true"))
                return true;
            else if (input.equalsIgnoreCase("0"))
                return false;
            else if (input.equalsIgnoreCase("no"))
                return false;
            else if (input.equalsIgnoreCase("false"))
                return false;
            getInstance().writeLn(NORMAL, "Please enter yes, no, true, false, 0 or 1.");
        }
    }

    public static int readInt(String msg) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine();
            try {
                int result = Integer.parseInt(input);
                return result;
            } catch (Exception exp) {
                getInstance().writeLn(NORMAL, "Please enter a valid integer");
            }
        }
    }

    public static long readLong(String msg) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine();
            try {
                long result = Long.parseLong(input);
                return result;
            } catch (Exception exp) {
                getInstance().writeLn(NORMAL, "Please enter a valid integer");
            }
        }
    }   

    public static double readDouble(String msg) {
        while (true) {
            getInstance().writeLn(NORMAL, msg);
            String input = getInstance().readInputLine();
            try {
                double result = Double.parseDouble(input);
                return result;
            } catch (Exception exp) {
                getInstance().writeLn(NORMAL, "Please enter a valid double");
            }
        }
    }

    public abstract String readInputLine();

    public abstract String readInputLine(long timeToWait);

    protected abstract void writeOutput(Priority priority, String msg);

    public static void writeError(Exception e) {
        writeError("", e);
    }

    public static void writeStackTrace(String message) {
        write(message);
        write(Utils.getStackTrace(new RuntimeException("Dummy")));
    }

    public enum Priority {
        NONE, ERROR, WARNING, HIGH, NORMAL, LOW, DEBUG
    }

    public static final Priority ERROR = Priority.ERROR;

    public static final Priority WARNING = Priority.WARNING;

    public static final Priority HIGH = Priority.HIGH;

    public static final Priority NORMAL = Priority.NORMAL;

    public static final Priority LOW = Priority.LOW;

    public static final Priority DEBUG = Priority.DEBUG;

    private static UI getDefaultOutputManager() {
        UI ui;
        try {
            ui = new ConsoleInputOutput();
            ui.setOutputLevel(UI.NORMAL);
            return ui;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static boolean print(Priority priority) {
        if (getInstance().getOutputLevel() == Priority.NONE)
            return false;
        return getInstance().getOutputLevel().compareTo(priority) >= 0;
    }

    private Priority outputLevel = UI.Priority.NORMAL;

    //Set the output level, choosing from NONE, ERROR, WARNING, HIGH, NORMAL, LOW, DEBUG

    public void setOutputLevel(Priority p) {
        outputLevel = p;
    }

    public Priority getOutputLevel() {
        return outputLevel;
    }

    public void finalize() throws Throwable {
        super.finalize();
        wtr.flush();
        wtr.close();
    }

    public static String getMemoryUsage() {
        return UI.getInstance().getMemory();
    }

    public static void write(Object obj) {
        if (obj == null) {
            write("null");
        } else {
            write(obj.toString());
        }
    }

    public static void write() {
        write("");
    }
}
