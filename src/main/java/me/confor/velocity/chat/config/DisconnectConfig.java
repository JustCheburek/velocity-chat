package me.confor.velocity.chat.config;

/**
 * Configuration for disconnect events
 */
public class DisconnectConfig extends BaseConfig {
    private final String format;

    public DisconnectConfig(boolean enabled, String format) {
        super(enabled);
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}