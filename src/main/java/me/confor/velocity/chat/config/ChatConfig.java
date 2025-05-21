package me.confor.velocity.chat.config;

/**
 * Configuration for main chat functionality
 */
public class ChatConfig extends BaseConfig {
    private final boolean logToConsole;
    private final boolean passthrough;
    private final boolean parsePlayerMessages;
    private final String format;

    public ChatConfig(boolean enabled, boolean logToConsole, boolean passthrough,
                      boolean parsePlayerMessages, String format) {
        super(enabled);
        this.logToConsole = logToConsole;
        this.passthrough = passthrough;
        this.parsePlayerMessages = parsePlayerMessages;
        this.format = format;
    }

    public boolean shouldLogToConsole() {
        return logToConsole;
    }

    public boolean isPassthrough() {
        return passthrough;
    }

    public boolean shouldParsePlayerMessages() {
        return parsePlayerMessages;
    }

    public String getFormat() {
        return format;
    }
}