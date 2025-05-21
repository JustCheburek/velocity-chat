package me.confor.velocity.chat.config;

/**
 * Configuration for player mentions
 */
public class MentionConfig extends BaseConfig {
    private final String mentionColor;
    private final String notificationSound;
    private final String titleText;
    private final String subtitleText;
    
    public MentionConfig(boolean enabled, String mentionColor, String notificationSound, 
                        String titleText, String subtitleText) {
        super(enabled);
        this.mentionColor = mentionColor;
        this.notificationSound = notificationSound;
        this.titleText = titleText;
        this.subtitleText = subtitleText;
    }
    
    public String getColor() {
        return mentionColor;
    }
    
    public String getNotificationSound() {
        return notificationSound;
    }
    
    public String getTitleText() {
        return titleText;
    }
    
    public String getSubtitleText() {
        return subtitleText;
    }
}