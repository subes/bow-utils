package be.bagofwords;

import be.bagofwords.logging.Log;
import be.bagofwords.logging.Slf4jLogImpl;

import java.io.IOException;

/**
 * Created by koen on 27/04/17.
 */
public class Test2 {

    public static void doTest() throws IOException {
        Log.setInstance(new Slf4jLogImpl());
        long start = System.currentTimeMillis();
        int logs = 5000;
        for (int i = 0; i < logs; i++) {
            String msg = "A number " + i + "/" + logs;
            Log.i(msg);
        }
        long timeTaken = System.currentTimeMillis() - start;
        double timePerLog = timeTaken / (double) logs;
        System.out.println("Time per log " + timePerLog);
    }

}
