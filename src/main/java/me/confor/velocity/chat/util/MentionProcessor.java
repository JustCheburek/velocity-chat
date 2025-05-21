package me.confor.velocity.chat.util;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.config.MentionConfig;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

import java.time.Duration;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles player mentions in chat messages
 */
public class MentionProcessor {
    private final ProxyServer server;
    private final MentionConfig config;
    private final MiniMessage miniMessage;
    private final Pattern mentionPattern;

    public MentionProcessor(ProxyServer server, MentionConfig config) {
        this.server = server;
        this.config = config;
        this.miniMessage = MiniMessage.miniMessage();
        // Pattern for exact username matching, case insensitive
        this.mentionPattern = Pattern.compile("@([a-zA-Z0-9_]{3,16})\\b", Pattern.CASE_INSENSITIVE);
    }

    /**
     * Process mentions in the message and notify mentioned players
     */
    public MentionResult processMentions(String message, Player sender) {
        if (!config.isEnabled()) {
            return new MentionResult(message, new HashSet<>());
        }

        Matcher matcher = mentionPattern.matcher(message);
        StringBuffer processedMessage = new StringBuffer();
        Set<Player> mentionedPlayers = new HashSet<>();

        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);

            // Exact match for username (case insensitive)
            Optional<Player> mentioned = server.getAllPlayers().stream()
                    .filter(player -> player.getUsername().equalsIgnoreCase(mentionedUsername))
                    .findFirst();

            if (mentioned.isPresent() && !mentioned.get().equals(sender)) {
                // Replace the mention with colored version using the actual player's username
                String actualUsername = mentioned.get().getUsername();
                String replacement = config.getColor() + "@" + actualUsername;
                matcher.appendReplacement(processedMessage, Matcher.quoteReplacement(replacement));
                mentionedPlayers.add(mentioned.get());
            } else {
                // Keep original mention if player not found or is sender
                matcher.appendReplacement(processedMessage, Matcher.quoteReplacement(matcher.group(0)));
            }
        }
        matcher.appendTail(processedMessage);

        // Send notifications to mentioned players
        for (Player mentioned : mentionedPlayers) {
            sendMentionNotification(mentioned, sender);
        }

        return new MentionResult(processedMessage.toString(), mentionedPlayers);
    }

    /**
     * Send notification to a mentioned player
     */
    private void sendMentionNotification(Player mentioned, Player sender) {
        try {
            // Send title
            Component title = miniMessage.deserialize(
                    config.getTitleText().replace("<player>", sender.getUsername())
            );
            Component subtitle = miniMessage.deserialize(
                    config.getSubtitleText().replace("<player>", sender.getUsername())
            );

            Title titleMessage = Title.title(
                    title,
                    subtitle,
                    Title.Times.times(
                            Duration.ofMillis(500),  // fade in
                            Duration.ofSeconds(3),   // stay
                            Duration.ofMillis(1000)  // fade out
                    )
            );
            mentioned.showTitle(titleMessage);

            // Play sound
            playMentionSound(mentioned);

        } catch (Exception e) {
            // Log error but don't break the chat flow
            server.getConsoleCommandSource().sendMessage(
                    Component.text("Error sending mention notification: " + e.getMessage())
            );
        }
    }

    /**
     * Play mention sound to the player
     */
    private void playMentionSound(Player player) {
        try {
            String soundName = config.getNotificationSound();

            // Ensure sound name is properly formatted
            if (!soundName.contains(":")) {
                soundName = "minecraft:" + soundName;
            }

            Key soundKey = Key.key(soundName.toLowerCase());
            Sound sound = Sound.sound(soundKey, Sound.Source.MASTER, 1.0f, 1.0f);
            player.playSound(sound);

        } catch (Exception e) {
            try {
                // Fallback to default sound if the configured sound is invalid
                Key soundKey = Key.key("minecraft:block.note_block.pling");
                Sound sound = Sound.sound(soundKey, Sound.Source.MASTER, 1.0f, 1.0f);
                player.playSound(sound);
            } catch (Exception fallbackError) {
                // If even fallback fails, just log and continue
                server.getConsoleCommandSource().sendMessage(
                        Component.text("Could not play mention sound: " + fallbackError.getMessage())
                );
            }
        }
    }

    /**
     * Get suggestions for tab completion when typing mentions
     * Returns only exact matches for active players
     */
    public Set<String> getMentionSuggestions(String input) {
        Set<String> suggestions = new HashSet<>();

        if (!input.startsWith("@") || input.length() <= 1) {
            return suggestions;
        }

        String partial = input.substring(1).toLowerCase();

        for (Player player : server.getAllPlayers()) {
            String playerName = player.getUsername();
            // Suggest players whose names start with the partial input
            if (playerName.toLowerCase().startsWith(partial)) {
                suggestions.add("@" + playerName);
            }
        }

        return suggestions;
    }

    /**
     * Extract mentioned usernames from a message (for logging purposes)
     */
    public Set<String> extractMentionedUsernames(String message) {
        Set<String> usernames = new HashSet<>();
        Matcher matcher = mentionPattern.matcher(message);

        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);
            // Only add if the player actually exists online
            boolean playerExists = server.getAllPlayers().stream()
                    .anyMatch(player -> player.getUsername().equalsIgnoreCase(mentionedUsername));

            if (playerExists) {
                usernames.add(mentionedUsername);
            }
        }

        return usernames;
    }

    /**
     * Check if a message contains any valid mentions
     */
    public boolean containsMentions(String message) {
        if (!mentionPattern.matcher(message).find()) {
            return false;
        }

        Matcher matcher = mentionPattern.matcher(message);
        while (matcher.find()) {
            String mentionedUsername = matcher.group(1);
            // Check if mentioned player is actually online
            boolean playerExists = server.getAllPlayers().stream()
                    .anyMatch(player -> player.getUsername().equalsIgnoreCase(mentionedUsername));

            if (playerExists) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate if a mention is for an existing online player
     */
    public boolean isValidMention(String username) {
        return server.getAllPlayers().stream()
                .anyMatch(player -> player.getUsername().equalsIgnoreCase(username));
    }

    /**
     * Result of mention processing
     */
    public static class MentionResult {
        private final String processedMessage;
        private final Set<Player> mentionedPlayers;

        public MentionResult(String processedMessage, Set<Player> mentionedPlayers) {
            this.processedMessage = processedMessage;
            this.mentionedPlayers = mentionedPlayers;
        }

        public String getProcessedMessage() {
            return processedMessage;
        }

        public Set<Player> getMentionedPlayers() {
            return mentionedPlayers;
        }

        public boolean hasMentions() {
            return !mentionedPlayers.isEmpty();
        }

        public int getMentionCount() {
            return mentionedPlayers.size();
        }
    }
}