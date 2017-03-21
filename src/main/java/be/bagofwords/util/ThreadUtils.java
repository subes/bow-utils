package be.bagofwords.util;

/**
 * Created by koen on 20/03/17.
 */
public class ThreadUtils {

    public static boolean terminate(Thread thread, int millis) {
        thread.interrupt();
        try {
            thread.join(millis);
        } catch (InterruptedException ex) {
            return false;
        }
        return !thread.isAlive();
    }

}
