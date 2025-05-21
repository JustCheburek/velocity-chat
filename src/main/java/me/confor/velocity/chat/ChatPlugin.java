package me.confor.velocity.chat;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import me.confor.velocity.chat.commands.CommandManager;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.listeners.ChatListener;
import me.confor.velocity.chat.listeners.ConnectionListener;
import me.confor.velocity.chat.listeners.TabCompleteListener;
import me.confor.velocity.chat.storage.PlayerStorage;
import org.slf4j.Logger;

import java.nio.file.Path;

@Plugin(
        id = "chat",
        name = "Chat",
        version = BuildConstants.VERSION,
        description = "A comprehensive chat management plugin for Velocity",
        authors = {"confor"}
)
@Singleton
public class ChatPlugin {

    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private ConfigManager configManager;
    private PlayerStorage playerStorage;
    private ChatListener chatListener;
    private ConnectionListener connectionListener;
    private TabCompleteListener tabCompleteListener;

    @Inject
    public ChatPlugin(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Loading Chat plugin v{}", BuildConstants.VERSION);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Chat plugin...");

        // Initialize configuration
        this.configManager = new ConfigManager(dataDirectory, logger);
        logger.info("Configuration loaded");

        // Initialize storage
        this.playerStorage = new PlayerStorage(dataDirectory, logger);
        logger.info("Player storage initialized");

        // Register event listeners
        registerEventListeners();
        logger.info("Event listeners registered");

        // Register commands
        registerCommands();
        logger.info("Commands registered");

        logger.info("Chat plugin successfully initialized v{}", BuildConstants.VERSION);
        logger.info("Features enabled:");
        logger.info("  - Global Chat: {}", configManager.getChatConfig().isEnabled());
        logger.info("  - Private Messages: {}", configManager.getPrivateMessageConfig().isEnabled());
        logger.info("  - Mentions: {}", configManager.getMentionConfig().isEnabled());
        logger.info("  - Profanity Filter: {}", configManager.getProfanityConfig().isEnabled());
        logger.info("  - Join/Leave Messages: {}/{}",
                configManager.getJoinConfig().isEnabled(),
                configManager.getLeaveConfig().isEnabled());
        logger.info("  - First Join Messages: {}", configManager.getFirstJoinConfig().isEnabled());
        logger.info("  - Server Switch Messages: {}", configManager.getSwitchConfig().isEnabled());
        logger.info("  - URL Clicking: {}", configManager.getUrlConfig().isClickable());
    }

    private void registerEventListeners() {
        // Chat listener handles chat events and filtering
        this.chatListener = new ChatListener(server, configManager, logger);
        server.getEventManager().register(this, chatListener);

        // Connection listener handles join/leave/switch events
        this.connectionListener = new ConnectionListener(server, configManager, playerStorage, logger);
        server.getEventManager().register(this, connectionListener);

        // Tab complete listener handles @ mentions autocomplete
        this.tabCompleteListener = new TabCompleteListener(server, configManager, logger);
        server.getEventManager().register(this, tabCompleteListener);

        // Config manager handles reload events
        server.getEventManager().register(this, configManager);

        logger.debug("All event listeners registered successfully");
    }

    private void registerCommands() {
        CommandManager commandManager = new CommandManager(this, server, configManager, logger);
        commandManager.registerCommands();
        logger.debug("Commands registered successfully");
    }

    /**
     * Reload all plugin components
     */
    public void reloadPlugin() {
        logger.info("Reloading Chat plugin...");

        // Reload configuration
        configManager.loadConfig();

        // Update chat listener with new config
        if (chatListener != null) {
            chatListener.reloadConfig();
        }

        logger.info("Chat plugin reloaded successfully");
    }

    // Getters for accessing plugin components
    public ProxyServer getServer() {
        return server;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public PlayerStorage getPlayerStorage() {
        return playerStorage;
    }

    public ChatListener getChatListener() {
        return chatListener;
    }

    public ConnectionListener getConnectionListener() {
        return connectionListener;
    }

    public TabCompleteListener getTabCompleteListener() {
        return tabCompleteListener;
    }
}