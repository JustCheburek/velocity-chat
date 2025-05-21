package me.confor.velocity.chat.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.ChatPlugin;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.filters.ProfanityFilter;
import me.confor.velocity.chat.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;

/**
 * Manages and registers all commands for the plugin
 */
public class CommandManager {
    private final ChatPlugin plugin;
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final Logger logger;

    public CommandManager(ChatPlugin plugin, ProxyServer server, ConfigManager configManager, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.configManager = configManager;
        this.logger = logger;
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

        server.getCommandManager().register("msg", privateMessageCommand, "tell", "pm", "w");
    }

    /**
     * Command to reload the plugin's configuration
     */
    private class ReloadCommand implements SimpleCommand {
        @Override
        public void execute(Invocation invocation) {
            if (!invocation.source().hasPermission("chat.reload")) {
                invocation.source().sendMessage(Component.text("You don't have permission to use this command.")
                        .color(NamedTextColor.RED));
                return;
            }

            configManager.loadConfig();
            invocation.source().sendMessage(Component.text("Chat plugin configuration reloaded successfully.")
                    .color(NamedTextColor.GREEN));
            logger.info("Configuration reloaded by {}", invocation.source().toString());
        }

        @Override
        public boolean hasPermission(Invocation invocation) {
            return invocation.source().hasPermission("chat.reload");
        }
    }
}