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
    private final String playerClickCommand;
    private final String playerHoverMessage;

    public ChatConfig(boolean enabled, boolean logToConsole, boolean passthrough,
                      boolean parsePlayerMessages, String format,
                      String noPermissionMessage, String configReloadedMessage,
                      String playerClickCommand, String playerHoverMessage) {
        super(enabled);
        this.logToConsole = logToConsole;
        this.passthrough = passthrough;
        this.parsePlayerMessages = parsePlayerMessages;
        this.format = format;
        this.noPermissionMessage = noPermissionMessage;
        this.configReloadedMessage = configReloadedMessage;
        this.playerClickCommand = playerClickCommand;
        this.playerHoverMessage = playerHoverMessage;
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

    public String getPlayerClickCommand() {
        return playerClickCommand;
    }

    public String getPlayerHoverMessage() {
        return playerHoverMessage;
    }
}
