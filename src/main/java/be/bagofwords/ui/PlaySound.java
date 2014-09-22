package be.bagofwords.ui;

import be.bagofwords.util.ExecuteProcess;

import java.io.File;
import java.io.IOException;

public class PlaySound {

    public static void play(File soundFile) throws IOException, InterruptedException {
        ExecuteProcess.exec("mplayer", soundFile.getAbsolutePath());
    }

}
