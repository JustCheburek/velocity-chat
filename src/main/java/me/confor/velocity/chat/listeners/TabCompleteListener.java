package me.confor.velocity.chat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.TabCompleteEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.util.MentionProcessor;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Handles tab completion for mentions and other chat features
 */
public class TabCompleteListener {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final MentionProcessor mentionProcessor;
    private final Logger logger;

    public TabCompleteListener(ProxyServer server, ConfigManager configManager, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.logger = logger;
        this.mentionProcessor = new MentionProcessor(server, configManager.getMentionConfig());
    }

    @Subscribe
    public void onTabComplete(TabCompleteEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = event.getPlayer();
        String partialText = event.getPartialMessage();

        // Check if mentions are enabled
        if (!configManager.getMentionConfig().isEnabled()) {
            return;
        }

        // Skip if it's a command
        if (partialText.startsWith("/")) {
            return;
        }

        // Find the current word being typed (last word after space)
        String[] words = partialText.split(" ");
        if (words.length == 0) {
            return;
        }

        String currentWord = words[words.length - 1];

        // Check if the current word starts with @ for mention completion
        if (currentWord.startsWith("@") && currentWord.length() > 1) {
            String partialUsername = currentWord.substring(1);
            List<String> suggestions = new ArrayList<>();

            // Get all online players and filter by partial match
            for (Player onlinePlayer : server.getAllPlayers()) {
                if (onlinePlayer.equals(player)) {
                    continue; // Skip self
                }

                String playerName = onlinePlayer.getUsername();
                if (playerName.toLowerCase().startsWith(partialUsername.toLowerCase())) {
                    // Construct the full suggestion by replacing the current word
                    String fullSuggestion = reconstructMessage(words, "@" + playerName);
                    suggestions.add(fullSuggestion);
                }
            }

            // Clear existing suggestions and add our mentions
            event.getSuggestions().clear();
            event.getSuggestions().addAll(suggestions);

            logger.debug("Tab completion for '{}' provided {} suggestions", currentWord, suggestions.size());
        }
    }

    /**
     * Reconstruct the full message with the completed mention
     */
    private String reconstructMessage(String[] words, String replacement) {
        if (words.length == 0) {
            return replacement;
        }

        StringBuilder result = new StringBuilder();

        // Add all words except the last one
        for (int i = 0; i < words.length - 1; i++) {
            if (i > 0) {
                result.append(" ");
            }
            result.append(words[i]);
        }

        // Add space before replacement if there were previous words
        if (words.length > 1) {
            result.append(" ");
        }

        // Add the replacement
        result.append(replacement);

        return result.toString();
    }
}