package me.confor.velocity.chat.config;

/**
 * Configuration for first join events
 */
public class FirstJoinConfig extends BaseConfig {
    private final boolean passthrough;
    private final String format;

    public FirstJoinConfig(boolean enabled, boolean passthrough, String format) {
        super(enabled);
        this.passthrough = passthrough;
        this.format = format;
    }

    public boolean isPassthrough() {
        return passthrough;
    }

    public String getFormat() {
        return format;
    }
}