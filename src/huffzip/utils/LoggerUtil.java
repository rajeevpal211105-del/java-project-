package huffzip.utils;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * LoggerUtil.java
 * ---------------
 * Central logging configuration for HuffZip, built on java.util.logging
 * (standard library only — no external logging framework).
 *
 * Major operations (tree built, file written, decode complete, errors)
 * are logged through this utility across every module.
 */
public final class LoggerUtil {

    private static final Logger LOGGER = Logger.getLogger("huffzip");

    static {
        LOGGER.setUseParentHandlers(false);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new java.util.logging.Formatter() {
            @Override
            public String format(LogRecord record) {
                String level = record.getLevel() == Level.SEVERE ? "ERROR"
                        : record.getLevel() == Level.WARNING ? "WARN"
                        : record.getLevel() == Level.FINE ? "DEBUG"
                        : "INFO";
                return "[" + level + "] " + record.getLoggerName() + ": " + record.getMessage() + System.lineSeparator();
            }
        });
        LOGGER.addHandler(handler);
        LOGGER.setLevel(Level.INFO);
    }

    private LoggerUtil() {
        // Utility class; not instantiable.
    }

    public static void info(String message) {
        LOGGER.log(Level.INFO, message);
    }

    public static void error(String message) {
        LOGGER.log(Level.SEVERE, message);
    }

    public static void debug(String message) {
        LOGGER.log(Level.FINE, message);
    }

    public static void setDebugEnabled(boolean enabled) {
        LOGGER.setLevel(enabled ? Level.FINE : Level.INFO);
        for (var h : LOGGER.getHandlers()) {
            h.setLevel(enabled ? Level.FINE : Level.INFO);
        }
    }
}
