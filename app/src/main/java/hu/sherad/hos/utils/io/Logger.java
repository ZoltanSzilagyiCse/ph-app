package hu.sherad.hos.utils.io;

import android.util.Log;

public class Logger {

    private static Logger logger;

    private Logger() {
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    private String getTag(LoggerDepth depth) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String className = Thread.currentThread().getStackTrace()[depth.getValue()].getClassName();
            String methodName = Thread.currentThread().getStackTrace()[depth.getValue()].getMethodName();

            stringBuilder.append(className.substring(className.lastIndexOf(".") + 1));
            stringBuilder.append("[").append(methodName).append("]");
        } catch (Exception exception) {
            Log.d(getClass().getSimpleName(), exception.getMessage(), exception);
        }
        return stringBuilder.toString();
    }

    public void e(Throwable throwable) {
        try {
            Log.e(getTag(LoggerDepth.JVM_METHOD), throwable.getMessage(), throwable);
        } catch (Exception exception) {
            Log.e(getTag(LoggerDepth.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
    }

    public void i(String message) {
        try {
            Log.i(getTag(LoggerDepth.ACTUAL_METHOD), message);
        } catch (Exception exception) {
            Log.e(getTag(LoggerDepth.ACTUAL_METHOD), "Logger failed, exception: " + exception.getMessage());
        }
    }

    public enum LoggerDepth {
        ACTUAL_METHOD(4),
        LOGGER_METHOD(3),
        STACK_TRACE_METHOD(1),
        JVM_METHOD(0);

        private final int value;

        LoggerDepth(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

}
