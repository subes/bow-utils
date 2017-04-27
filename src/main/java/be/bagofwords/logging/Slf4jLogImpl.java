package be.bagofwords.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static be.bagofwords.logging.LogLevel.*;

/**
 * Created by koen on 27/04/17.
 */
public class Slf4jLogImpl implements LogImpl {

    @Override
    public void log(LogLevel level, Class logger, String message, Throwable throwable) {
        Logger slf4jLogger = logger == null ? LoggerFactory.getLogger("ROOT") : LoggerFactory.getLogger(logger);
        if (level == INFO) {
            slf4jLogger.info(message, throwable);
        } else if (level == WARN) {
            slf4jLogger.warn(message, throwable);
        } else if (level == ERROR) {
            slf4jLogger.error(message, throwable);
        }
    }
}
