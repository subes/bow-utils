package be.bagofwords.logging;

import static be.bagofwords.logging.LogLevel.*;

/**
 * Created by koen on 27/04/17.
 */
public class Log {

    private static LogAdapter DEFAULT_INSTANCE = new Slf4jLogImpl();
    private static LogAdapter INSTANCE = DEFAULT_INSTANCE;
    public static final Object LOCK = new Object();

    public static void setInstance(LogAdapter logImpl) {
        if (INSTANCE != DEFAULT_INSTANCE) {
            throw new IllegalArgumentException("Instance has already been set to " + INSTANCE);
        }
        INSTANCE = logImpl;
        Log.i("Set logging instance to " + logImpl);
    }

    public static LogAdapter getInstance() {
        return INSTANCE;
    }

    public static void i(String message) {
        log(INFO, message, null);
    }

    public static void i(String message, Throwable throwable) {
        log(INFO, message, throwable);
    }

    public static void i(Class logger, String message) {
        log(INFO, logger, message, null);
    }

    public static void i(Class logger, String message, Throwable throwable) {
        log(INFO, logger, message, throwable);
    }

    public static void i(Object logger, String message) {
        log(INFO, logger.getClass(), message, null);
    }

    public static void i(Object logger, String message, Throwable throwable) {
        log(INFO, logger.getClass(), message, throwable);
    }

    public static void w(String message) {
        log(WARN, message, null);
    }

    public static void w(String message, Throwable throwable) {
        log(WARN, message, throwable);
    }

    public static void w(Class logger, String message) {
        log(WARN, logger, message, null);
    }

    public static void w(Class logger, String message, Throwable throwable) {
        log(WARN, logger, message, throwable);
    }

    public static void w(Object logger, String message) {
        log(WARN, logger.getClass(), message, null);
    }

    public static void w(Object logger, String message, Throwable throwable) {
        log(WARN, logger.getClass(), message, throwable);
    }

    public static void e(String message) {
        log(ERROR, message, null);
    }

    public static void e(Throwable throwable) {
        log(ERROR, "", throwable);
    }

    public static void e(String message, Throwable throwable) {
        log(ERROR, message, throwable);
    }

    public static void e(Class logger, String message) {
        log(ERROR, logger, message, null);
    }

    public static void e(Class logger, String message, Throwable throwable) {
        log(ERROR, logger, message, throwable);
    }

    public static void e(Object logger, String message) {
        log(ERROR, logger.getClass(), message, null);
    }

    public static void e(Object logger, String message, Throwable throwable) {
        log(ERROR, logger.getClass(), message, throwable);
    }

    public static void log(LogLevel level, String message) {
        log(level, message, null);
    }

    public static void log(LogLevel level, String message, Throwable throwable) {
        Class logger = LogUtils.callingClass();
        log(level, logger, message, throwable);
    }

    public static void log(LogLevel level, String logger, String message, Throwable throwable) {
        synchronized (LOCK) {
            INSTANCE.log(level, logger, message, throwable);
        }
    }

    public static void log(LogLevel level, Class logger, String message, Throwable throwable) {
        synchronized (LOCK) {
            INSTANCE.log(level, logger, message, throwable);
        }
    }

}
