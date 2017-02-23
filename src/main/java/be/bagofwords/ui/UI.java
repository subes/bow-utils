package be.bagofwords.ui;

import be.bagofwords.util.NumUtils;
import be.bagofwords.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class UI {

    private static final Logger logger = LoggerFactory.getLogger(UI.class);

    public static boolean printMemory = true;
    private static BufferedReader in;

    public static void writeError(String msg) {
        writeLn(ERROR, msg);
    }

    public static void writeError(String msg, Throwable e) {
        synchronized (UI.class) {
            writeLn(ERROR, msg);
            String[] lines = Utils.getStackTrace(e).split("\n");
            for (String line : lines) {
                writeLn(ERROR, line);
            }
        }
    }

    public static void writeWarning(String msg) {
        writeLn(WARNING, msg);
    }

    public static void writeHigh(String msg) {
        writeLn(HIGH, msg);
    }

    public static void writeNormal(String msg) {
        writeLn(NORMAL, msg);
    }

    public static void writeLow(String msg) {
        writeLn(LOW, msg);
    }

    public static void writeDebug(String msg) {
        writeLn(DEBUG, msg);
    }

    public static void write(String msg) {
        writeNormal(msg);
    }

    public static void writeLn(Priority priority, String msg) {
        if (printMemory)
            msg = getMemory() + " " + msg;
        switch (priority) {
            case DEBUG:
            case LOW:
                logger.debug(msg);
                break;
            case NORMAL:
            case NONE:
                logger.info(msg);
                break;
            case WARNING:
                logger.warn(msg);
            case ERROR:
            case HIGH:
                logger.error(msg);
                break;
        }
    }

    private boolean prevNewLine = true;


    public static String getMemory() {
        return NumUtils.fixedLength((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000, 4) + "Mb";
    }

    public static String read() {
        return readInputLine();
    }

    public static String read(String msg) {
        writeLn(NORMAL, msg);
        return readInputLine();
    }

    public static boolean readBoolean(String msg) {
        while (true) {
            writeLn(NORMAL, msg);
            String input = readInputLine();
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
            writeLn(NORMAL, "Please enter yes, no, true, false, 0 or 1.");
        }
    }

    public static boolean readBoolean(String msg, boolean defaultValue) {
        while (true) {
            writeLn(NORMAL, msg);
            String input = readInputLine();
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
            writeLn(NORMAL, "Please enter yes, no, true, false, 0 or 1.");
        }
    }


    public static int readInt(String msg) {
        while (true) {
            writeLn(NORMAL, msg);
            String input = readInputLine();
            try {
                int result = Integer.parseInt(input);
                return result;
            } catch (Exception exp) {
                writeLn(NORMAL, "Please enter a valid integer");
            }
        }
    }

    public static long readLong(String msg) {
        while (true) {
            writeLn(NORMAL, msg);
            String input = readInputLine();
            try {
                long result = Long.parseLong(input);
                return result;
            } catch (Exception exp) {
                writeLn(NORMAL, "Please enter a valid integer");
            }
        }
    }

    public static double readDouble(String msg) {
        while (true) {
            writeLn(NORMAL, msg);
            String input = readInputLine();
            try {
                double result = Double.parseDouble(input);
                return result;
            } catch (Exception exp) {
                writeLn(NORMAL, "Please enter a valid double");
            }
        }
    }

    public static synchronized String readInputLine() {
        if (in == null) {
            in = new BufferedReader(new InputStreamReader(System.in));
        }
        try {
            return in.readLine();
        } catch (IOException e) {
            writeError("Error while reading input.", e);
        }
        return "";
    }


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


    public static String getMemoryUsage() {
        return UI.getMemory();
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
