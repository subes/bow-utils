package be.bagofwords.logging;

import be.bagofwords.util.Utils;

import java.io.PrintStream;

/**
 * Created by koen on 27/04/17.
 */
public class SdtOutLogImpl implements LogImpl {

    @Override
    public void log(LogLevel level, Class logger, String message, Throwable throwable) {
        PrintStream stream = level == LogLevel.INFO ? System.out : System.err;
        String loggerStr = logger != null ? logger.getSimpleName() : "ROOT";
        if (throwable != null) {
            stream.println(level + " " + loggerStr + " " + message + " " + throwable.getMessage());
            stream.println(Utils.getStackTrace(throwable));
        } else {
            stream.println(level + " " + loggerStr + " " + message);
        }
    }
}
