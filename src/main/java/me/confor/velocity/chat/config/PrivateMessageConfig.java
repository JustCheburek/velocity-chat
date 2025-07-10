package me.confor.velocity.chat.config;

import java.util.List;

/**
 * Configuration for private messaging
 */
public class PrivateMessageConfig extends BaseConfig {
    private final List<String> aliases;
    private final boolean logToConsole;
    private final String senderFormat;
    private final String recipientFormat;
    private final String consoleLogFormat;
    private final String playersOnlyMessage;
    private final String disabledMessage;
    private final String usageMessage;
    private final String playerNotFoundMessage;
    private final String selfMessageMessage;
    private final String notificationSound;

    public PrivateMessageConfig(boolean enabled, List<String> aliases, boolean logToConsole,
                                String senderFormat, String recipientFormat, String consoleLogFormat,
                                String playersOnlyMessage, String disabledMessage, String usageMessage,
                                String playerNotFoundMessage, String selfMessageMessage,
                                String notificationSound) {
        super(enabled);
        this.aliases = aliases;
        this.logToConsole = logToConsole;
        this.senderFormat = senderFormat;
        this.recipientFormat = recipientFormat;
        this.consoleLogFormat = consoleLogFormat;
        this.playersOnlyMessage = playersOnlyMessage;
        this.disabledMessage = disabledMessage;
        this.usageMessage = usageMessage;
        this.playerNotFoundMessage = playerNotFoundMessage;
        this.selfMessageMessage = selfMessageMessage;
        this.notificationSound = notificationSound;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean shouldLogToConsole() {
        return logToConsole;
    }

    public String getSenderFormat() {
        return senderFormat;
    }

    public String getRecipientFormat() {
        return recipientFormat;
    }

    public String getConsoleLogFormat() {
        return consoleLogFormat;
    }

    public String getPlayersOnlyMessage() {
        return playersOnlyMessage;
    }

    public String getDisabledMessage() {
        return disabledMessage;
    }

    public String getUsageMessage() {
        return usageMessage;
    }

    public String getPlayerNotFoundMessage() {
        return playerNotFoundMessage;
    }

    public String getSelfMessageMessage() {
        return selfMessageMessage;
    }
    
    public String getNotificationSound() {
        return notificationSound;
    }

    // Backward compatibility methods
    @Deprecated
    public String getFormat() {
        return senderFormat;
    }
}