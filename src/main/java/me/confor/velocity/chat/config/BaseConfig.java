package me.confor.velocity.chat.config;

/**
 * Base configuration class that all specific configs extend from
 */
public abstract class BaseConfig {
    private final boolean enabled;

    protected BaseConfig(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}