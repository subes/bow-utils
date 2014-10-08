package be.bagofwords.application.file;

import be.bagofwords.application.CloseableComponent;
import be.bagofwords.application.annotations.BowComponent;
import be.bagofwords.ui.UI;
import be.bagofwords.util.SafeThread;
import be.bagofwords.util.Utils;

import java.util.ArrayList;
import java.util.List;

@BowComponent
public class OpenFilesManager implements CloseableComponent {

    private static final int MAX_NUMBER_OF_OPEN_FILES = 7000;

    private final CheckOpenFilesThread checkOpenFilesThread;
    private final List<FilesCollection> filesCollections;

    private int currentNumberOfOpenFiles = 0;

    public OpenFilesManager() {
        this.filesCollections = new ArrayList<>();
        this.checkOpenFilesThread = new CheckOpenFilesThread();
        this.checkOpenFilesThread.start();
    }

    public synchronized void registerFilesCollection(FilesCollection filesCollection) {
        filesCollections.add(filesCollection);
    }

    @Override
    public void terminate() {
        checkOpenFilesThread.terminate();
    }

    public synchronized void registerOpenFile() {
        currentNumberOfOpenFiles++;
        long lastStatusMessage = System.currentTimeMillis();
        while (currentNumberOfOpenFiles > MAX_NUMBER_OF_OPEN_FILES) {
            Utils.threadSleep(100);
            if (System.currentTimeMillis() - lastStatusMessage > 10 * 1000) {
                UI.write("Method is waiting to open a new file...");
                lastStatusMessage = System.currentTimeMillis();
            }
        }
    }

    public synchronized void registerClosedFile() {
        currentNumberOfOpenFiles--;
    }

    public class CheckOpenFilesThread extends SafeThread {

        public CheckOpenFilesThread() {
            super("CheckOpenFilesThread", true);
        }

        @Override
        protected void runInt() throws Exception {
            while (!isTerminateRequested()) {
                synchronized (this) {
                    if (currentNumberOfOpenFiles / (double) MAX_NUMBER_OF_OPEN_FILES > 0.8) {
                        for (int i = 0; i < filesCollections.size() && !isTerminateRequested(); i++) {
                            FilesCollection filesCollection = filesCollections.get(i);
                            filesCollection.closeOpenFiles(1.0);
                        }
                    }
                }
                Utils.threadSleep(100);
            }
        }
    }
}
