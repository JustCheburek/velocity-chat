package me.confor.velocity.chat.listeners;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.confor.velocity.chat.config.ChatConfig;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.filters.ProfanityFilter;
import me.confor.velocity.chat.util.MessageFormatter;
import me.confor.velocity.chat.util.MentionProcessor;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Handles chat-related events
 */
public class ChatListener {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final Logger logger;
    private final MessageFormatter messageFormatter;
    private final ProfanityFilter profanityFilter;
    private final MentionProcessor mentionProcessor;

    public ChatListener(ProxyServer server, ConfigManager configManager, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.logger = logger;
        this.messageFormatter = new MessageFormatter();
        this.profanityFilter = new ProfanityFilter(logger);
        this.mentionProcessor = new MentionProcessor(server, configManager.getMentionConfig());

        // Load word lists from config
        updateWordLists();

        logger.info("Enhanced chat listener initialized with {} profanity words",
                configManager.getProfanityConfig().getProfanityWords().size());
    }

    /**
     * Update profanity word lists from config
     */
    public void updateWordLists() {
        if (configManager.getProfanityConfig().isEnabled()) {
            profanityFilter.setProfanityWords(configManager.getProfanityConfig().getProfanityWords());
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        ChatConfig chatConfig = configManager.getChatConfig();

        // Skip if chat is disabled
        if (!chatConfig.isEnabled()) {
            logger.debug("Chat is disabled, skipping message processing");
            return;
        }

        Optional<ServerConnection> currentServer = event.getPlayer().getCurrentServer();
        if (currentServer.isEmpty()) {
            logger.debug("Player not connected to any server, skipping");
            return;
        }

        // Get player and message info
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        String serverName = currentServer.get().getServerInfo().getName();
        String originalMessage = event.getMessage();
        String message = originalMessage;

        logger.debug("Processing chat message from {}: '{}'", playerName, originalMessage);

        // Handle ! prefix warning
        boolean hadExclamationPrefix = false;
        if (message.startsWith("!")) {
            hadExclamationPrefix = true;
            message = message.substring(1).trim();

            // Send warning message about ! prefix
            Component warningMsg = messageFormatter.parseMessage(
                    configManager.getGlobalChatConfig().getWarningMessage()
            );
            player.sendMessage(warningMsg);
        }

        // Skip processing if message is empty after removing !
        if (message.trim().isEmpty()) {
            event.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        // Filter profanity (if enabled)
        String filteredMessage = message;
        if (configManager.getProfanityConfig().isEnabled()) {
            String beforeFiltering = filteredMessage;
            filteredMessage = profanityFilter.filterProfanity(message);

            if (!beforeFiltering.equals(filteredMessage)) {
                logger.debug("Profanity filtered for {}: '{}' -> '{}'",
                        playerName, beforeFiltering, filteredMessage);
            }
        }

        // Process mentions (if enabled)
        MentionProcessor.MentionResult mentionResult = mentionProcessor.processMentions(filteredMessage, player);
        String processedMessage = mentionResult.getProcessedMessage();


        if (mentionResult.hasMentions()) {
            logger.debug("Processed {} mentions for {}: {}",
                    mentionResult.getMentionCount(),
                    playerName,
                    mentionResult.getMentionedPlayers().stream()
                            .map(Player::getUsername)
                            .toList()
            );
        }

        // Format the message
        Component formattedMessage = messageFormatter.formatChatMessage(
                chatConfig.getFormat(),
                playerName,
                serverName,
                processedMessage,
                chatConfig.shouldParsePlayerMessages()
        );

        // Apply URL formatting if enabled
        if (configManager.getUrlConfig().isClickable()) {
            formattedMessage = formattedMessage.replaceText(configManager.getUrlReplacementConfig());
            logger.debug("Applied URL formatting for message from {}", playerName);
        }

        // Send the message based on passthrough setting
        if (chatConfig.isPassthrough()) {
            // Cancel original event and send custom message to all players
            event.setResult(PlayerChatEvent.ChatResult.denied());
            sendMessageToAll(formattedMessage);
            logger.debug("Sent formatted message to all players (passthrough mode)");
        } else {
            // Allow original message to go to current server, send formatted to others
            sendMessageExcept(formattedMessage, currentServer.get().getServer());
            logger.debug("Sent formatted message to other servers (non-passthrough mode)");
        }

        // Log to console if enabled
        if (chatConfig.shouldLogToConsole()) {
            StringBuilder logMessage = new StringBuilder();
            logMessage.append(String.format("GLOBAL: <%s@%s> %s", playerName, serverName, filteredMessage));

            // Add mention info to log if there were mentions
            if (mentionResult.hasMentions()) {
                logMessage.append(String.format(" [Mentions: %d players: %s]",
                        mentionResult.getMentionCount(),
                        mentionResult.getMentionedPlayers().stream()
                                .map(Player::getUsername)
                                .toList()));
            }

            // Add prefix info if there was an exclamation mark
            if (hadExclamationPrefix) {
                logMessage.append(" [Had ! prefix]");
            }

            // Add filtering info if profanity was filtered
            if (!message.equals(filteredMessage)) {
                logMessage.append(" [Profanity filtered]");
            }

            logger.info(logMessage.toString());
        }
    }

    /**
     * Send a message to all online players
     */
    private void sendMessageToAll(Component message) {
        int playerCount = server.getAllPlayers().size();
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(message);
        }
        logger.debug("Sent message to {} players", playerCount);
    }

    /**
     * Send a message to all servers except the specified one
     */
    private void sendMessageExcept(Component message, RegisteredServer excludedServer) {
        for (RegisteredServer targetServer : server.getAllServers()) {
            if (!targetServer.equals(excludedServer)) {
                targetServer.sendMessage(message);
            }
        }
    }

    /**
     * Public method to update configuration (called when config is reloaded)
     */
    public void reloadConfig() {
        updateWordLists();
    }
}