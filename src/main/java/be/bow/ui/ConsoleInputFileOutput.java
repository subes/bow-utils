package be.bow.ui;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class ConsoleInputFileOutput extends ConsoleInputOutput {

    private BufferedWriter writer;
    private final String file;

    public ConsoleInputFileOutput(String file) throws Exception {
        this.file = file;
        writer = new BufferedWriter(new FileWriter(file));
    }

    @Override
    public void writeOutput(Priority priority, String msg) {
        try {
            writer.write(priority.name());
            writer.write(" ");
            writer.write(msg);
        } catch (IOException e) {
            super.writeOutput(Priority.ERROR, "Failed to write to file " + file);
        }
    }
}
