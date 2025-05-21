package me.confor.velocity.chat.config;

/**
 * Configuration for server switch events
 */
public class SwitchConfig extends BaseConfig {
    private final String format;

    public SwitchConfig(boolean enabled, String format) {
        super(enabled);
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}