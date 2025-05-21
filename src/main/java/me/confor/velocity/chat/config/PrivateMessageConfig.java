package me.confor.velocity.chat.config;

/**
 * Configuration for private messaging
 */
public class PrivateMessageConfig extends BaseConfig {
    private final boolean logToConsole;
    private final String format;
    private final String consoleLogFormat;
    
    public PrivateMessageConfig(boolean enabled, boolean logToConsole, String format, String consoleLogFormat) {
        super(enabled);
        this.logToConsole = logToConsole;
        this.format = format;
        this.consoleLogFormat = consoleLogFormat;
    }
    
    public boolean shouldLogToConsole() {
        return logToConsole;
    }
    
    public String getFormat() {
        return format;
    }
    
    public String getConsoleLogFormat() {
        return consoleLogFormat;
    }
}