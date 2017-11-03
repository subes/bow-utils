package be.bagofwords.logging;

public interface LogAdapter {

    void log(LogLevel level, Class logger, String message, Throwable throwable);

    void log(LogLevel level, String logger, String message, Throwable throwable);
}
