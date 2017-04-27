package be.bagofwords.logging;

class LogUtils {

    static Class callingClass() {
        StackTraceElement[] els = Thread.currentThread().getStackTrace();
        String stopAt = Log.class.getCanonicalName();
        for (int i = 2; i < els.length; i++) {
            if (!els[i].getClassName().equals(stopAt)) {
                try {
                    return LogUtils.class.getClassLoader().loadClass(els[i].getClassName());
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }

}
