package me.confor.velocity.chat.config;

/**
 * Configuration for server join events
 */
public class JoinConfig extends BaseConfig {
    private final boolean passthrough;
    private final String format;

    public JoinConfig(boolean enabled, boolean passthrough, String format) {
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