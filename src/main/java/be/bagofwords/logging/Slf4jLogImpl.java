package be.bagofwords.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.bagofwords.logging.LogLevel.*;

public class Slf4jLogImpl implements LogAdapter {

    @Override
    public void log(LogLevel level, Class logger, String message, Throwable throwable) {
        Logger slf4jLogger = logger == null ? LoggerFactory.getLogger("ROOT") : LoggerFactory.getLogger(logger);
        doLog(level, message, throwable, slf4jLogger);
    }

    @Override
    public void log(LogLevel level, String logger, String message, Throwable throwable) {
        Logger slf4jLogger = logger == null ? LoggerFactory.getLogger("ROOT") : LoggerFactory.getLogger(logger);
        doLog(level, message, throwable, slf4jLogger);
    }

    public void doLog(LogLevel level, String message, Throwable throwable, Logger slf4jLogger) {
        if (level == INFO) {
            slf4jLogger.info(message, throwable);
        } else if (level == WARN) {
            slf4jLogger.warn(message, throwable);
        } else if (level == ERROR) {
            slf4jLogger.error(message, throwable);
        }
    }
}
