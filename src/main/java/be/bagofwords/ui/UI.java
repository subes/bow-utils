package be.bagofwords.ui;

import be.bagofwords.logging.Log;
import be.bagofwords.util.NumUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public abstract class UI {

    private static BufferedReader in;

    public static String read() {
        return readInputLine();
    }

    public static String read(String msg) {
        Log.i(msg);
        return readInputLine();
    }

    public static boolean readBoolean(String msg) {
        while (true) {
            Log.i(msg);
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
            Log.i("Please enter yes, no, true, false, 0 or 1.");
        }
    }

    public static boolean readBoolean(String msg, boolean defaultValue) {
        while (true) {
            Log.i(msg);
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
            Log.i("Please enter yes, no, true, false, 0 or 1.");
        }
    }

    public static int readInt(String msg) {
        while (true) {
            Log.i(msg);
            String input = readInputLine();
            try {
                return Integer.parseInt(input);
            } catch (Exception exp) {
                Log.i("Please enter a valid integer");
            }
        }
    }

    public static long readLong(String msg) {
        while (true) {
            Log.i(msg);
            String input = readInputLine();
            try {
                return Long.parseLong(input);
            } catch (Exception exp) {
                Log.i("Please enter a valid integer");
            }
        }
    }

    public static double readDouble(String msg) {
        while (true) {
            Log.i(msg);
            String input = readInputLine();
            try {
                return Double.parseDouble(input);
            } catch (Exception exp) {
                Log.i("Please enter a valid double");
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
            Log.e("Error while reading input.", e);
        }
        return "";
    }

    public static String getMemoryUsage() {
        return NumUtils.fixedLength((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1000000, 4) + "Mb";
    }

}
