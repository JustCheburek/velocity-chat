package me.confor.velocity.chat.filters;

import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enhanced profanity word filtering with wildcard support and smart censoring
 */
public class ProfanityFilter {
    private final Logger logger;
    private List<Pattern> profanityPatterns;

    public ProfanityFilter(Logger logger) {
        this.logger = logger;
        this.profanityPatterns = new ArrayList<>();
    }

    /**
     * Set the profanity words list and compile patterns with wildcard support
     */
    public void setProfanityWords(List<String> words) {
        this.profanityPatterns = new ArrayList<>();

        for (String word : words) {
            // Поддержка wildcard шаблонов (например, "уёб*" или "бля*")
            boolean hasWildcard = word.endsWith("*");
            String baseWord = hasWildcard ? word.substring(0, word.length() - 1) : word;
            
            // Создаем гибкий паттерн для каждого слова
            String pattern = createFlexiblePattern(baseWord, hasWildcard);
            this.profanityPatterns.add(Pattern.compile(pattern, 
                Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE));
        }

        logger.info("Loaded {} profanity patterns with wildcard support", words.size());
    }

    /**
     * Создает гибкий паттерн для поиска слова с учетом различных вариаций и wildcard
     */
    private String createFlexiblePattern(String word, boolean hasWildcard) {
        StringBuilder pattern = new StringBuilder();
        
        // Граница слова в начале
        pattern.append("(?<!\\p{L})");

        // Обрабатываем каждую букву в слове
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            
            // Добавляем возможные вариации для каждой буквы
            pattern.append(getCharacterVariations(c));
            
            // Добавляем возможные разделители между буквами
            if (i < word.length() - 1) {
                pattern.append("[\\s\\-_.*]*?");
            }
        }

        // Если это wildcard шаблон, добавляем продолжение
        if (hasWildcard) {
            pattern.append("\\p{L}*");
        }

        // Граница слова в конце
        pattern.append("(?!\\p{L})");

        return pattern.toString();
    }

    /**
     * Возвращает паттерн для всех возможных вариаций буквы
     */
    private String getCharacterVariations(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'а' -> "[аa@4]";
            case 'б' -> "[бb6]";
            case 'в' -> "[вv]";
            case 'г' -> "[гg]";
            case 'д' -> "[дd]";
            case 'е', 'ё' -> "[еёe3]";
            case 'з' -> "[зz3]";
            case 'и' -> "[иi1!]";
            case 'й' -> "[йи]";
            case 'к' -> "[кk]";
            case 'л' -> "[лl]";
            case 'м' -> "[мm]";
            case 'н' -> "[нn]";
            case 'о' -> "[оo0]";
            case 'п' -> "[пp]";
            case 'р' -> "[рp]";
            case 'с' -> "[сc$]";
            case 'т' -> "[тt]";
            case 'у' -> "[уy]";
            case 'ф' -> "[фf]";
            case 'х' -> "[хx]";
            case 'ц' -> "[ц]";
            case 'ч' -> "[ч]";
            case 'ш' -> "[ш]";
            case 'щ' -> "[щ]";
            case 'ъ' -> "[ъ]";
            case 'ы' -> "[ы]";
            case 'ь' -> "[ь]";
            case 'э' -> "[э]";
            case 'ю' -> "[ю]";
            case 'я' -> "[я]";
            default -> Pattern.quote(String.valueOf(c));
        };
    }

    /**
     * Фильтрует нецензурные слова, заменяя их звездочками с сохранением длины
     */
    public String filterProfanity(String message) {
        if (message == null || message.trim().isEmpty()) {
            return message;
        }

        String result = message;
        
        for (Pattern pattern : profanityPatterns) {
            Matcher matcher = pattern.matcher(result);
            StringBuffer sb = new StringBuffer();
            
            while (matcher.find()) {
                String foundWord = matcher.group();
                String censored = "*".repeat(foundWord.length());
                matcher.appendReplacement(sb, Matcher.quoteReplacement(censored));
                
                logger.debug("Filtered profanity: '{}' -> '{}'", foundWord, censored);
            }
            
            matcher.appendTail(sb);
            result = sb.toString();
        }

        return result;
    }

    /**
     * Проверяет, содержит ли текст нецензурные слова
     */
    public boolean containsProfanity(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        for (Pattern pattern : profanityPatterns) {
            if (pattern.matcher(text).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if a specific word is in the profanity list
     */
    public boolean isProfanityWord(String word) {
        return containsProfanity(word);
    }

    /**
     * Get all profanity matches in the text for debugging
     */
    public List<String> findProfanityMatches(String text) {
        List<String> matches = new ArrayList<>();

        for (Pattern pattern : profanityPatterns) {
            Matcher matcher = pattern.matcher(text);
            while (matcher.find()) {
                matches.add(matcher.group());
            }
        }

        return matches;
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
            while (matcher.find()) {
                debug.append("  - Word '").append(i)
                        .append("' matched: '").append(matcher.group())
                        .append("' at position ").append(matcher.start())
                        .append("-").append(matcher.end()).append("\n");
            }
        }

        if (debug.toString().endsWith("Profanity matches:\n")) {
            debug.append("  - No matches found\n");
        }

        return debug.toString();
    }
}