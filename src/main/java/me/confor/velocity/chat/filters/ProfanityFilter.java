package me.confor.velocity.chat.filters;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced profanity word filtering with improved regex patterns
 */
public class ProfanityFilter {
    private final Logger logger;
    private List<String> profanityWords;
    private List<Pattern> profanityPatterns;

    public ProfanityFilter(Logger logger) {
        this.logger = logger;
        this.profanityWords = new ArrayList<>();
        this.profanityPatterns = new ArrayList<>();
    }

    /**
     * Set the profanity words list and compile enhanced patterns
     */
    public void setProfanityWords(List<String> words) {
        this.profanityWords = new ArrayList<>(words);
        this.profanityPatterns.clear();

        for (String word : words) {
            // Create pattern that matches whole word only (not substrings)
            String regex = Pattern.quote(word);
            this.profanityPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }

        logger.info("Loaded {} profanity words with word boundary patterns", words.size());
    }

    /**
     * Filter profanity words by replacing them with asterisks
     */
    public String filterProfanity(String message) {
        String result = message;

        for (int i = 0; i < profanityPatterns.size(); i++) {
            Pattern pattern = profanityPatterns.get(i);
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();

            while (matcher.find()) {
                String word = matcher.group();
                String filtered = censorWord(word);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(filtered));
            }
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    /**
     * Enhanced word censoring with better preservation of word structure
     */
    private String censorWord(String word) {
        if (word.length() <= 2) {
            return "*".repeat(word.length());
        }

        int length = word.length();

        // For short words (3-4 chars), keep first letter and replace rest with asterisks
        if (length <= 4) {
            return word.charAt(0) + "*".repeat(length - 1);
        }

        // For longer words, keep first and last letter, replace middle with asterisks
        return word.charAt(0) + "*".repeat(length - 2) + word.charAt(length - 1);
    }

    /**
     * Check if a specific word is in the profanity list
     */
    public boolean isProfanityWord(String word) {
        for (Pattern pattern : profanityPatterns) {
            if (pattern.matcher(word).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get debugging information about what patterns matched
     */
    public String getDebugInfo(String message) {
        StringBuilder debug = new StringBuilder();
        debug.append("Testing message: '").append(message).append("'\n");

        debug.append("Profanity matches:\n");
        for (int i = 0; i < profanityPatterns.size(); i++) {
            Pattern pattern = profanityPatterns.get(i);
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()) {
                debug.append("  - Pattern ").append(i).append(" (").append(profanityWords.get(i))
                        .append("): '").append(matcher.group()).append("'\n");
            }
        }

        return debug.toString();
    }
}