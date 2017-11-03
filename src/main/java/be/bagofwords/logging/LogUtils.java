package be.bagofwords.logging;

class LogUtils {

    static Class callingClass() {
        StackTraceElement[] els = Thread.currentThread().getStackTrace();
        for (int i = 1; i < els.length; i++) {
            String className = els[i].getClassName();
            if (className.startsWith("sun.reflect")) {
                return null;
            }
            if (!className.startsWith("be.bagofwords.logging") && !className.startsWith("org.slf4j")) {
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
