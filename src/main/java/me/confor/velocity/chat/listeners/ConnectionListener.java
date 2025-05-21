package me.confor.velocity.chat.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.confor.velocity.chat.config.ConfigManager;
import me.confor.velocity.chat.storage.PlayerStorage;
import me.confor.velocity.chat.util.MessageFormatter;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Optional;

/**
 * Handles player connection-related events (join, leave, switch, disconnect)
 */
public class ConnectionListener {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final PlayerStorage playerStorage;
    private final Logger logger;
    private final MessageFormatter messageFormatter;

    public ConnectionListener(ProxyServer server, ConfigManager configManager,
                              PlayerStorage playerStorage, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.playerStorage = playerStorage;
        this.logger = logger;
        this.messageFormatter = new MessageFormatter();
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        RegisteredServer currentServer = event.getServer();
        Optional<RegisteredServer> previousServer = event.getPreviousServer();

        String playerName = player.getUsername();
        String uuid = player.getUniqueId().toString();
        String serverName = currentServer.getServerInfo().getName();

        // Handle first join
        if (previousServer.isEmpty() && configManager.getFirstJoinConfig().isEnabled()
                && !playerStorage.hasPlayerBeenSeen(uuid)) {

            Component firstJoinMessage = messageFormatter.formatServerMessage(
                    configManager.getFirstJoinConfig().getFormat(),
                    playerName,
                    serverName,
                    null
            );

            if (configManager.getFirstJoinConfig().isPassthrough()) {
                sendMessageToAll(firstJoinMessage);
            } else {
                sendMessageToServer(firstJoinMessage, currentServer);
            }

            playerStorage.markPlayerAsSeen(uuid);
            return;
        }

        // Handle regular join (no previous server)
        if (previousServer.isEmpty()) {
            if (!configManager.getJoinConfig().isEnabled()) {
                return;
            }

            Component joinMessage = messageFormatter.formatServerMessage(
                    configManager.getJoinConfig().getFormat(),
                    playerName,
                    serverName,
                    null
            );

            if (configManager.getJoinConfig().isPassthrough()) {
                sendMessageToAll(joinMessage);
            } else {
                sendMessageToServer(joinMessage, currentServer);
            }
        }
        // Handle server switch
        else {
            if (!configManager.getSwitchConfig().isEnabled()) {
                return;
            }

            String previousServerName = previousServer.get().getServerInfo().getName();

            Component switchMessage = messageFormatter.formatServerMessage(
                    configManager.getSwitchConfig().getFormat(),
                    playerName,
                    serverName,
                    previousServerName
            );

            if (configManager.getJoinConfig().isPassthrough()) {
                sendMessageToAll(switchMessage);
            } else {
                sendMessageToServer(switchMessage, currentServer);
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        Player player = event.getPlayer();
        String playerName = player.getUsername();
        Optional<ServerConnection> currentServer = player.getCurrentServer();

        // Player disconnected without being connected to a server
        if (currentServer.isEmpty()) {
            if (configManager.getDisconnectConfig().isEnabled()) {
                Component disconnectMessage = messageFormatter.formatServerMessage(
                        configManager.getDisconnectConfig().getFormat(),
                        playerName,
                        null,
                        null
                );

                sendMessageToAll(disconnectMessage);
            }
            return;
        }

        // Player left from a specific server
        if (configManager.getLeaveConfig().isEnabled()) {
            String serverName = currentServer.get().getServerInfo().getName();

            Component leaveMessage = messageFormatter.formatServerMessage(
                    configManager.getLeaveConfig().getFormat(),
                    playerName,
                    serverName,
                    null
            );

            if (configManager.getLeaveConfig().isPassthrough()) {
                sendMessageToAll(leaveMessage);
            } else {
                sendMessageToServer(leaveMessage, currentServer.get().getServer());
            }
        }
    }

    /**
     * Send a message to all online players
     */
    private void sendMessageToAll(Component message) {
        for (Player player : server.getAllPlayers()) {
            player.sendMessage(message);
        }
    }

    /**
     * Send a message to a specific server
     */
    private void sendMessageToServer(Component message, RegisteredServer targetServer) {
        targetServer.sendMessage(message);
    }
}