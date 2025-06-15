```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.slf4j.event.LoggingEvent;
import org.slf4j.event.SubstituteLoggingEvent;
import org.slf4j.helpers.MessageFormatter;
import org.slf4j.spi.LoggingEventBuilder;

public class StructuredLogger {

    private static final Logger logger = LoggerFactory.getLogger(StructuredLogger.class);

    public static void log(Level level, String message, Object... args) {
        String formattedMessage = MessageFormatter.arrayFormat(message, args).getMessage();
        LoggingEvent event = new SubstituteLoggingEvent();
        event.setLevel(level);
        event.setMessage(formattedMessage);
        event.setLoggerName(logger.getName());

        switch (level) {
            case INFO -> logger.info(formatLog(event));
            case WARN -> logger.warn(formatLog(event));
            case ERROR -> logger.error(formatLog(event));
            default -> throw new IllegalArgumentException("Unsupported log level: " + level);
        }
    }

    private static String formatLog(LoggingEvent event) {
        return switch (event.getLevel()) {
            case INFO -> String.format("INFO: %s - %s", event.getLoggerName(), event.getMessage());
            case WARN -> String.format("WARNING: %s - %s", event.getLoggerName(), event.getMessage());
            case ERROR -> String.format("ERROR: %s - %s", event.getLoggerName(), event.getMessage());
            default -> throw new IllegalArgumentException("Unsupported log level: " + event.getLevel());
        };
    }

    public static void main(String[] args) {
        log(Level.INFO, "Application started with args: {}", (Object) args);
        log(Level.WARN, "Potential issue detected: {}", "Disk space low");
        log(Level.ERROR, "An error occurred: {}", new RuntimeException("Connection failed"));
    }
}
```