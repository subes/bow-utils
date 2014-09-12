package be.bow.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ConsoleInputOutput extends UI {

    final BufferedReader in;

    public ConsoleInputOutput() {
        in = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void writeOutput(Priority priority, String msg) {
        if (priority == UI.Priority.WARNING || priority == UI.Priority.ERROR)
            System.out.print(msg);
        else
            System.out.print(msg);
    }

    @Override
    public String readInputLine() {
        try {
            return in.readLine();
        } catch (IOException e) {
            writeOutput(HIGH, "Error while reading input.");
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String readInputLine(long timeToWait) {
        int timeWaited = 0;
        StringBuilder result = new StringBuilder("");
        boolean endOfLineReached = false;
        char[] buff = new char[100];
        synchronized (this) {
            while (timeWaited < timeToWait && !endOfLineReached) {
                try {
                    if (in.ready()) {
                        int num = in.read(buff);
                        for (int i = 0; i < num; i++)
                            if (buff[i] == '\n') {
                                num = i;
                                endOfLineReached = true;
                                break;
                            }
                        result.append(buff, 0, num);
                    } else {
                        this.wait(20);
                        timeWaited += 20;
                    }
                } catch (IOException | InterruptedException e) {
                    writeOutput(HIGH, "Error while reading input.");
                    e.printStackTrace();
                }
            }
        }
        if (result.length() == 0)
            return null;
        else
            return result.toString();
    }

}
