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
            // Create more flexible pattern that catches variations
            String regex = createFlexiblePattern(word);
            this.profanityPatterns.add(Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }

        logger.info("Loaded {} profanity words with enhanced patterns", words.size());
    }

    /**
     * Create flexible pattern for profanity words that catches common substitutions
     */
    private String createFlexiblePattern(String word) {
        StringBuilder pattern = new StringBuilder();
        pattern.append("\\b");

        for (char c : word.toLowerCase().toCharArray()) {
            switch (c) {
                case 'а':
                    pattern.append("[аa@4]");
                    break;
                case 'е':
                    pattern.append("[еe3ё]");
                    break;
                case 'о':
                    pattern.append("[оo0]");
                    break;
                case 'у':
                    pattern.append("[уy]");
                    break;
                case 'и':
                    pattern.append("[иi1]");
                    break;
                case 'с':
                    pattern.append("[сc$5]");
                    break;
                case 'р':
                    pattern.append("[рp]");
                    break;
                case 'х':
                    pattern.append("[хx]");
                    break;
                case 'в':
                    pattern.append("[вv]");
                    break;
                case 'н':
                    pattern.append("[нn]");
                    break;
                case 'т':
                    pattern.append("[тt7]");
                    break;
                case 'м':
                    pattern.append("[мm]");
                    break;
                case 'д':
                    pattern.append("[дd]");
                    break;
                case 'л':
                    pattern.append("[лl]");
                    break;
                case 'б':
                    pattern.append("[бb6]");
                    break;
                case 'г':
                    pattern.append("[гg]");
                    break;
                case 'з':
                    pattern.append("[з3z]");
                    break;
                case 'к':
                    pattern.append("[кk]");
                    break;
                case 'п':
                    pattern.append("[пp]");
                    break;
                case 'ф':
                    pattern.append("[фf]");
                    break;
                case 'ч':
                    pattern.append("[ч4]");
                    break;
                case 'ш':
                    pattern.append("[ш]");
                    break;
                case 'щ':
                    pattern.append("[щ]");
                    break;
                case 'ц':
                    pattern.append("[ц]");
                    break;
                case 'ж':
                    pattern.append("[ж]");
                    break;
                case 'ъ':
                    pattern.append("[ъ]");
                    break;
                case 'ь':
                    pattern.append("[ь]");
                    break;
                case 'ы':
                    pattern.append("[ы]");
                    break;
                case 'э':
                    pattern.append("[э]");
                    break;
                case 'ю':
                    pattern.append("[ю]");
                    break;
                case 'я':
                    pattern.append("[я]");
                    break;
                default:
                    pattern.append(Pattern.quote(String.valueOf(c)));
            }
            // Allow for potential separators between characters
            pattern.append("[\\s\\-_\\.\\*]*");
        }

        pattern.append("\\b");
        return pattern.toString();
    }

    /**
     * Filter profanity words by replacing them with asterisks
     */
    public String filterProfanity(String message) {
        String result = message;

        for (Pattern pattern : profanityPatterns) {
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
     * Normalize text for better matching (remove excessive separators)
     */
    private String normalizeText(String text) {
        return text.replaceAll("[\\s\\-_.*]+", " ").trim();
    }

    /**
     * Enhanced word censoring with better preservation of word structure
     */
    private String censorWord(String word) {
        if (word.length() <= 2) {
            return "*".repeat(word.length());
        }

        int length = word.length();

        // For short words, keep first letter and replace rest with asterisks
        if (length <= 4) {
            return word.charAt(0) + "*".repeat(length - 1);
        }

        // For longer words, keep first letter, some middle characters, and last letter
        int keepStart = 1;
        int keepEnd = 1;
        int censorLength = length - keepStart - keepEnd;

        StringBuilder censored = new StringBuilder();
        censored.append(word.substring(0, keepStart));
        censored.append("*".repeat(Math.max(1, censorLength)));
        if (keepEnd > 0 && length > keepStart) {
            censored.append(word.substring(length - keepEnd));
        }

        return censored.toString();
    }

    /**
     * Check if a specific word is in the profanity list
     */
    public boolean isProfanityWord(String word) {
        String normalizedWord = normalizeText(word.toLowerCase());

        for (Pattern pattern : profanityPatterns) {
            if (pattern.matcher(normalizedWord).find()) {
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
        debug.append("Normalized: '").append(normalizeText(message)).append("'\n");

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