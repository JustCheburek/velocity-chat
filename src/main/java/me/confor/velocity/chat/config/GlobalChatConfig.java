package me.confor.velocity.chat.config;

/**
 * Configuration for global chat features
 */
public class GlobalChatConfig {
    private final String warningMessage;
    
    public GlobalChatConfig(String warningMessage) {
        this.warningMessage = warningMessage;
    }
    
    public String getWarningMessage() {
        return warningMessage;
    }
}