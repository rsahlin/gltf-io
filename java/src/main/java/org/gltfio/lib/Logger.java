
package org.gltfio.lib;

/**
 * Very simple platform agnostic logger that prints a message.
 * Call {@link #setLogger(Log)} or {@link #setDefaultLogger()} before logging any messages.
 * 
 */
public class Logger {

    private static Log logger;
    private static final boolean SET_DEFAULT_LOGGER = true;

    private Logger() {
    }

    public enum Level {
        ERROR(1),
        DEBUG(2),
        INFO(3);

        public final int value;

        Level(int v) {
            value = v;
        }
    }

    /**
     * Loglevels
     * error
     * debug
     * info
     * 
     */
    public interface Log {
        /**
         * Logs a debug message for the specified class, the class name will be used as tag.
         * 
         * @param clazz
         * @param message
         */
        void d(Class clazz, String message);

        /**
         * Logs a debug message for the specified tag.
         * 
         * @param tag
         * @param message
         */
        void d(String tag, String message);

        /**
         * Logs an info message for the class
         * 
         * @param clazz
         * @param message
         */
        void i(Class clazz, String message);

        /**
         * Logs en error message for the class
         * 
         * @param clazz
         * @param message
         */
        void e(Class clazz, String message);

        /**
         * Sets the output loglevel
         * 
         * @param level
         */
        void setLoglevel(Level level);

    }

    private static class DefaultLogger implements Log {

        private Level level = Level.DEBUG;
        private long time = 0;
        private String zeros = "00000000";

        private boolean isLevel(Level messageLevel) {
            return level.value >= messageLevel.value;
        }

        @Override
        public void d(Class clazz, String message) {
            if (isLevel(Level.DEBUG)) {
                logMessage(clazz, message);
            }
        }

        @Override
        public void d(String tag, String message) {
            if (isLevel(Level.DEBUG)) {
                logMessage(tag, message);
            }
        }

        @Override
        public void i(Class clazz, String message) {
            if (isLevel(Level.INFO)) {
                logMessage(clazz, message);
            }
        }

        @Override
        public void e(Class clazz, String message) {
            if (isLevel(Level.ERROR)) {
                logMessage(clazz, message);
            }
        }

        private void logMessage(Class clazz, String message) {
            System.out.println(getDeltaString() + " : " + clazz
                    .getCanonicalName() + " " + message);
        }

        private void logMessage(String tag, String message) {
            System.out.println(getDeltaString() + " : " + tag + " " + message);
        }

        private String getDeltaString() {
            int delta = (int) (time > 0 ? System.currentTimeMillis() - time : 0);
            time = System.currentTimeMillis();
            String deltaStr = Integer.toString(delta);
            return zeros.substring(0, zeros.length() - deltaStr.length()) + deltaStr;
        }

        @Override
        public void setLoglevel(Level loglevel) {
            this.level = loglevel;
        }

    }

    /**
     * Sets the logger implementation to use, must be called before any message is logged.
     * 
     * @param l The logger to use
     */
    public static void setLogger(Log l) {
        Logger.logger = l;
    }

    /**
     * Sets the default (j2se) logger - this will log using System.out
     */
    public static void setDefaultLogger() {
        Logger.logger = new DefaultLogger();
    }

    private static void checkLogger() {
        if (logger == null) {
            if (!SET_DEFAULT_LOGGER) {
                System.out.println("No logger set - call #setLogger() or #setDefaultLogger()");
                throw new IllegalArgumentException("No logger is set");
            } else {
                setDefaultLogger();
            }
        }
    }

    /**
     * Logs a debug message for the specified class.
     * 
     * @param clazz
     * @param message
     */
    public static void d(Class clazz, String message) {
        checkLogger();
        logger.d(clazz, message);
    }

    /**
     * Logs an info message for the specified class.
     * 
     * @param clazz
     * @param message
     */
    public static void i(Class clazz, String message) {
        checkLogger();
        logger.i(clazz, message);
    }

    /**
     * Logs an error message for the specified class.
     * 
     * @param clazz
     * @param message
     */
    public static void e(Class clazz, String message) {
        checkLogger();
        logger.e(clazz, message);
    }

    /**
     * Logs a debug message for the tag
     * 
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        checkLogger();
        logger.d(tag, message);
    }

    /**
     * Sets the output loglevel
     * 
     * @param level
     */
    public static void setLoglevel(Level level) {
        checkLogger();
        logger.setLoglevel(level);
    }

}
