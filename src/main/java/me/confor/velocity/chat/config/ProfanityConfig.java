package me.confor.velocity.chat.config;

import java.util.List;

/**
 * Configuration for profanity word filtering
 */
public class ProfanityConfig extends BaseConfig {
    private final List<String> profanityWords;
    
    public ProfanityConfig(boolean enabled, List<String> profanityWords) {
        super(enabled);
        this.profanityWords = profanityWords;
    }
    
    public List<String> getProfanityWords() {
        return profanityWords;
    }
}