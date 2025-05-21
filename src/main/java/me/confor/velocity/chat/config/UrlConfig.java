package me.confor.velocity.chat.config;

/**
 * Configuration for URL handling
 */
public class UrlConfig {
    private final boolean clickable;
    private final String pattern;

    public UrlConfig(boolean clickable, String pattern) {
        this.clickable = clickable;
        this.pattern = pattern;
    }

    public boolean isClickable() {
        return clickable;
    }

    public String getPattern() {
        return pattern;
    }
}