package me.confor.velocity.chat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for formatting messages with placeholders
 */
public class MessageFormatter {
    private final MiniMessage miniMessage;

    public MessageFormatter() {
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Format a chat message with player, server, and message placeholders
     *
     * @param format The format string with placeholders
     * @param playerName The name of the player
     * @param serverName The name of the server
     * @param message The chat message
     * @param parseMessage Whether to parse the message content for formatting
     * @return The formatted component
     */
    public Component formatChatMessage(String format, String playerName, String serverName,
                                       String message, boolean parseMessage) {
        List<TagResolver.Single> placeholders = new ArrayList<>();

        // Add player placeholder
        placeholders.add(Placeholder.parsed("player", playerName));

        // Add server placeholder
        if (serverName != null) {
            placeholders.add(Placeholder.parsed("server", serverName));
        }

        // Add message placeholder - either parsed or unparsed
        if (parseMessage) {
            placeholders.add(Placeholder.parsed("message", message));
        } else {
            placeholders.add(Placeholder.unparsed("message", message));
        }

        return miniMessage.deserialize(format, TagResolver.resolver(placeholders));
    }

    /**
     * Format a server-related message (join, leave, switch)
     *
     * @param format The format string with placeholders
     * @param playerName The name of the player
     * @param serverName The name of the server (may be null)
     * @param previousServerName The name of the previous server (may be null)
     * @return The formatted component
     */
    public Component formatServerMessage(String format, String playerName,
                                         String serverName, String previousServerName) {
        List<TagResolver.Single> placeholders = new ArrayList<>();

        // Add player placeholder
        placeholders.add(Placeholder.parsed("player", playerName));

        // Add server placeholder if provided
        if (serverName != null) {
            placeholders.add(Placeholder.parsed("server", serverName));
        }

        // Add previous_server placeholder if provided
        if (previousServerName != null) {
            placeholders.add(Placeholder.parsed("previous_server", previousServerName));
        }

        return miniMessage.deserialize(format, TagResolver.resolver(placeholders));
    }

    /**
     * Format a private message
     *
     * @param format The format string with placeholders
     * @param senderName The name of the sender
     * @param recipientName The name of the recipient
     * @param message The message content
     * @param isSender Whether this is being sent to the sender (true) or recipient (false)
     * @return The formatted component
     */
    public Component formatPrivateMessage(String format, String senderName, String recipientName,
                                          String message, boolean isSender) {
        List<TagResolver.Single> placeholders = new ArrayList<>();

        // Add placeholders
        placeholders.add(Placeholder.parsed("sender", senderName));
        placeholders.add(Placeholder.parsed("recipient", recipientName));
        placeholders.add(Placeholder.unparsed("message", message));
        placeholders.add(Placeholder.parsed("direction", isSender ? "to" : "from"));

        return miniMessage.deserialize(format, TagResolver.resolver(placeholders));
    }

    /**
     * Parse a MiniMessage format string into a Component
     *
     * @param message The message to parse
     * @return The parsed component
     */
    public Component parseMessage(String message) {
        return miniMessage.deserialize(message);
    }
}