package me.confor.velocity.chat.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.ChatPlugin;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.filters.ProfanityFilter;
import me.confor.velocity.chat.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.slf4j.Logger;

import java.util.List;

/**
 * Manages and registers all commands for the plugin
 */
public class CommandManager {
    private final ChatPlugin plugin;
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final Logger logger;
    private final MiniMessage miniMessage;

    public CommandManager(ChatPlugin plugin, ProxyServer server, ConfigManager configManager, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.configManager = configManager;
        this.logger = logger;
        this.miniMessage = MiniMessage.miniMessage();
    }

    /**
     * Register all commands
     */
    public void registerCommands() {
        // Reload command
        server.getCommandManager().register(
                "chatreload",
                new ReloadCommand(),
                "creload"
        );

        // Private message commands
        MessageFormatter messageFormatter = new MessageFormatter();
        ProfanityFilter profanityFilter = new ProfanityFilter(logger);

        PrivateMessageCommand privateMessageCommand = new PrivateMessageCommand(
                server, configManager, messageFormatter, profanityFilter, logger
        );

        // Register private message command with aliases from config
        List<String> aliases = configManager.getPrivateMessageConfig().getAliases();
        if (!aliases.isEmpty()) {
            String primaryCommand = aliases.get(0);
            String[] aliasArray = aliases.subList(1, aliases.size()).toArray(new String[0]);
            server.getCommandManager().register(primaryCommand, privateMessageCommand, aliasArray);
        }
    }

    /**
     * Command to reload the plugin's configuration
     */
    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            if (!invocation.source().hasPermission("chat.reload")) {
                Component message = miniMessage.deserialize(configManager.getChatConfig().getNoPermissionMessage());
                invocation.source().sendMessage(message);
                return;
            }

            configManager.loadConfig();
            Component message = miniMessage.deserialize(configManager.getChatConfig().getConfigReloadedMessage());
            invocation.source().sendMessage(message);
            logger.info("Configuration reloaded by {}", invocation.source().toString());
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("chat.reload");
        }
    }
}