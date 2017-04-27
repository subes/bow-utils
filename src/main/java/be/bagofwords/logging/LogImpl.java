package be.bagofwords.logging;

/**
 * Created by koen on 27/04/17.
 */
public interface LogImpl {

    void log(LogLevel level, Class logger, String message, Throwable throwable);

}
