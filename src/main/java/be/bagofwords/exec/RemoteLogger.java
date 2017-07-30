/*******************************************************************************
 * Created by Koen Deschacht (koendeschacht@gmail.com) 2017-7-29. For license
 * information see the LICENSE file in the root folder of this repository.
 ******************************************************************************/

package be.bagofwords.exec;

import be.bagofwords.logging.LogLevel;
import be.bagofwords.util.SocketConnection;
import be.bagofwords.util.Utils;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import java.text.SimpleDateFormat;
import java.util.Date;

import static be.bagofwords.util.Utils.noException;

public class RemoteLogger extends MarkerIgnoringBase {

    private boolean closed = false;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    private final SocketConnection socketConnection;

    public RemoteLogger(SocketConnection socketConnection) {
        this.socketConnection = socketConnection;
    }

    private void log(int level, String message, Throwable t) {
        if (this.isLevelEnabled(level) && !closed) {
            writeLine(message);
            String[] lines;
            if (t != null) {
                lines = Utils.getStackTrace(t).split("\n");
                for (String line : lines) {
                    writeLine(line);
                }
            } else {
                lines = null;
            }
            final RemoteLogStatement remoteLogStatement = new RemoteLogStatement(toLevel(level), message, lines);
            synchronized (socketConnection) {
                noException(new Utils.Action() {
                    @Override
                    public void run() throws Exception {
                        socketConnection.writeValue(RemoteExecAction.SEND_LOG);
                        socketConnection.writeValue(remoteLogStatement);
                        socketConnection.flush();
                    }
                });
            }
        }
    }

    private LogLevel toLevel(int level) {
        if (level >= 40) {
            return LogLevel.ERROR;
        } else if (level >= 30) {
            return LogLevel.WARN;
        } else {
            return LogLevel.INFO;
        }
    }

    private void writeLine(String line) {
        System.out.println(timeFormat.format(new Date()) + " " + line);
    }

    public void close() {
        this.closed = true;
    }

    private void log(int level, String format, Object arg1, Object arg2) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    private void log(int level, String format, Object... arguments) {
        if (this.isLevelEnabled(level)) {
            FormattingTuple tp = MessageFormatter.arrayFormat(format, arguments);
            this.log(level, tp.getMessage(), tp.getThrowable());
        }
    }

    protected boolean isLevelEnabled(int logLevel) {
        return true;
    }

    public boolean isTraceEnabled() {
        return this.isLevelEnabled(0);
    }

    public void trace(String msg) {
        this.log(0, msg, (Throwable) null);
    }

    public void trace(String format, Object param1) {
        this.log(0, format, param1, null);
    }

    public void trace(String format, Object param1, Object param2) {
        this.log(0, format, param1, param2);
    }

    public void trace(String format, Object... argArray) {
        this.log(0, format, argArray);
    }

    public void trace(String msg, Throwable t) {
        this.log(0, msg, t);
    }

    public boolean isDebugEnabled() {
        return this.isLevelEnabled(10);
    }

    public void debug(String msg) {
        this.log(10, msg, (Throwable) null);
    }

    public void debug(String format, Object param1) {
        this.log(10, format, param1, null);
    }

    public void debug(String format, Object param1, Object param2) {
        this.log(10, format, param1, param2);
    }

    public void debug(String format, Object... argArray) {
        this.log(10, format, argArray);
    }

    public void debug(String msg, Throwable t) {
        this.log(10, msg, t);
    }

    public boolean isInfoEnabled() {
        return this.isLevelEnabled(20);
    }

    public void info(String msg) {
        this.log(20, msg, (Throwable) null);
    }

    public void info(String format, Object arg) {
        this.log(20, format, arg, null);
    }

    public void info(String format, Object arg1, Object arg2) {
        this.log(20, format, arg1, arg2);
    }

    public void info(String format, Object... argArray) {
        this.log(20, format, argArray);
    }

    public void info(String msg, Throwable t) {
        this.log(20, msg, t);
    }

    public boolean isWarnEnabled() {
        return this.isLevelEnabled(30);
    }

    public void warn(String msg) {
        this.log(30, msg, (Throwable) null);
    }

    public void warn(String format, Object arg) {
        this.log(30, format, arg, null);
    }

    public void warn(String format, Object arg1, Object arg2) {
        this.log(30, format, arg1, arg2);
    }

    public void warn(String format, Object... argArray) {
        this.log(30, format, argArray);
    }

    public void warn(String msg, Throwable t) {
        this.log(30, msg, t);
    }

    public boolean isErrorEnabled() {
        return this.isLevelEnabled(40);
    }

    public void error(String msg) {
        this.log(40, msg, (Throwable) null);
    }

    public void error(String format, Object arg) {
        this.log(40, format, arg, null);
    }

    public void error(String format, Object arg1, Object arg2) {
        this.log(40, format, arg1, arg2);
    }

    public void error(String format, Object... argArray) {
        this.log(40, format, argArray);
    }

    public void error(String msg, Throwable t) {
        this.log(40, msg, t);
    }

}
