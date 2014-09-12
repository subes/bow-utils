package be.bow.ui;

import be.bow.util.ExecuteProcess;

import java.io.File;
import java.io.IOException;

public class PlaySound {

    public static void play(File soundFile) throws IOException, InterruptedException {
        ExecuteProcess.exec("mplayer", soundFile.getAbsolutePath());
    }

}
