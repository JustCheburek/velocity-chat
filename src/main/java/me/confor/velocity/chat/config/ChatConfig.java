package me.confor.velocity.chat.config;

/**
 * Configuration for global chat
 */
public class ChatConfig extends BaseConfig {
    private final boolean logToConsole;
    private final boolean passthrough;
    private final boolean parsePlayerMessages;
    private final String format;
    private final String noPermissionMessage;
    private final String configReloadedMessage;

    public ChatConfig(boolean enabled, boolean logToConsole, boolean passthrough,
                      boolean parsePlayerMessages, String format,
                      String noPermissionMessage, String configReloadedMessage) {
        super(enabled);
        this.logToConsole = logToConsole;
        this.passthrough = passthrough;
        this.parsePlayerMessages = parsePlayerMessages;
        this.format = format;
        this.noPermissionMessage = noPermissionMessage;
        this.configReloadedMessage = configReloadedMessage;
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

    public String getNoPermissionMessage() {
        return noPermissionMessage;
    }

    public String getConfigReloadedMessage() {
        return configReloadedMessage;
    }
}