package me.confor.velocity.chat.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.filters.ProfanityFilter;
import me.confor.velocity.chat.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
    
    public PrivateMessageCommand(ProxyServer server, ConfigManager configManager, 
                                MessageFormatter messageFormatter, ProfanityFilter profanityFilter, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.messageFormatter = messageFormatter;
        this.profanityFilter = profanityFilter;
        this.logger = logger;
    }
    
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        
        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command can only be used by players.")
                .color(NamedTextColor.RED));
            return;
        }
        
        if (!configManager.getPrivateMessageConfig().isEnabled()) {
            source.sendMessage(Component.text("Private messaging is disabled.")
                .color(NamedTextColor.RED));
            return;
        }
        
        if (args.length < 2) {
            source.sendMessage(Component.text("Usage: /msg <player> <message>")
                .color(NamedTextColor.RED));
            return;
        }
        
        Player sender = (Player) source;
        String targetName = args[0];
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        
        // Find target player
        Optional<Player> target = server.getPlayer(targetName);
        if (target.isEmpty()) {
            sender.sendMessage(Component.text("Player '" + targetName + "' not found.")
                .color(NamedTextColor.RED));
            return;
        }
        
        if (target.get().equals(sender)) {
            sender.sendMessage(Component.text("You can't send a message to yourself.")
                .color(NamedTextColor.RED));
            return;
        }
        
        // Filter profanity
        String filteredMessage = profanityFilter.filterProfanity(message);
        
        // Format the message for both sender and recipient
        Component senderMessage = messageFormatter.formatPrivateMessage(
            configManager.getPrivateMessageConfig().getFormat(),
            sender.getUsername(),
            target.get().getUsername(),
            filteredMessage,
            true // sender view
        );
        
        Component recipientMessage = messageFormatter.formatPrivateMessage(
            configManager.getPrivateMessageConfig().getFormat(),
            sender.getUsername(),
            target.get().getUsername(),
            filteredMessage,
            false // recipient view
        );
        
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