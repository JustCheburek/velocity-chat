package me.confor.velocity.chat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.filters.ProfanityFilter;
import me.confor.velocity.chat.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Command for private messaging between players
 */
public class PrivateMessageCommand implements SimpleCommand {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final MessageFormatter messageFormatter;
    private final ProfanityFilter profanityFilter;
    private final Logger logger;
    private final MiniMessage miniMessage;

    public PrivateMessageCommand(ProxyServer server, ConfigManager configManager,
                                 MessageFormatter messageFormatter, ProfanityFilter profanityFilter, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.messageFormatter = messageFormatter;
        this.profanityFilter = profanityFilter;
        this.logger = logger;
        this.miniMessage = MiniMessage.miniMessage();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            Component message = miniMessage.deserialize(configManager.getPrivateMessageConfig().getPlayersOnlyMessage());
            source.sendMessage(message);
            return;
        }

        if (!configManager.getPrivateMessageConfig().isEnabled()) {
            Component message = miniMessage.deserialize(configManager.getPrivateMessageConfig().getDisabledMessage());
            source.sendMessage(message);
            return;
        }

        if (args.length < 2) {
            Component message = miniMessage.deserialize(configManager.getPrivateMessageConfig().getUsageMessage());
            source.sendMessage(message);
            return;
        }

        Player sender = (Player) source;
        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        // Find target player
        Optional<Player> target = server.getPlayer(targetName);
        if (target.isEmpty()) {
            String notFoundMessage = configManager.getPrivateMessageConfig().getPlayerNotFoundMessage()
                    .replace("<player>", targetName);
            Component notFoundComponent = miniMessage.deserialize(notFoundMessage);
            sender.sendMessage(notFoundComponent);
            return;
        }

        if (target.get().equals(sender)) {
            Component selfMessage = miniMessage.deserialize(configManager.getPrivateMessageConfig().getSelfMessageMessage());
            sender.sendMessage(selfMessage);
            return;
        }

        // Filter profanity
        String filteredMessage = profanityFilter.filterProfanity(message);

        // Format the message for sender using sender format
        String senderFormatString = configManager.getPrivateMessageConfig().getSenderFormat()
                .replace("<sender>", sender.getUsername())
                .replace("<recipient>", target.get().getUsername())
                .replace("<message>", filteredMessage);
        Component senderMessage = miniMessage.deserialize(senderFormatString);

        // Format the message for recipient using recipient format
        String recipientFormatString = configManager.getPrivateMessageConfig().getRecipientFormat()
                .replace("<sender>", sender.getUsername())
                .replace("<recipient>", target.get().getUsername())
                .replace("<message>", filteredMessage);
        Component recipientMessage = miniMessage.deserialize(recipientFormatString);

        // Send messages
        sender.sendMessage(senderMessage);
        target.get().sendMessage(recipientMessage);

        // Log to console if enabled
        if (configManager.getPrivateMessageConfig().shouldLogToConsole()) {
            String logMessage = configManager.getPrivateMessageConfig().getConsoleLogFormat()
                    .replace("<sender>", sender.getUsername())
                    .replace("<recipient>", target.get().getUsername())
                    .replace("<message>", filteredMessage);
            logger.info(logMessage);
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();

        if (args.length <= 1) {
            // Suggest online player names
            String partial = args.length == 1 ? args[0].toLowerCase() : "";
            return server.getAllPlayers().stream()
                    .map(Player::getUsername)
                    .filter(name -> name.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("chat.msg");
    }
}