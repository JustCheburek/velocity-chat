package me.confor.velocity.chat.modules;

import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import me.confor.velocity.chat.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class GlobalChat {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private Config config;

    // For tracking first-join
    private final Set<String> seenPlayers;
    private final Path seenFile;

    public GlobalChat(ProxyServer server, Logger logger, Path dataDirectory) {
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        this.config = new Config(dataDirectory);

        this.seenFile = dataDirectory.resolve("seenPlayers.txt");
        this.seenPlayers = loadSeen();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        this.config = new Config(this.dataDirectory);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        if (!config.GLOBAL_CHAT_ENABLED)
            return;

        Optional<ServerConnection> currentServer = event.getPlayer().getCurrentServer();
        if (currentServer.isEmpty())
            return;

        String player = event.getPlayer().getUsername();
        String server = currentServer.get().getServerInfo().getName();
        String message = event.getMessage();

        Component msg = parseMessage(config.GLOBAL_CHAT_FORMAT, List.of(
                new ChatTemplate("player", player, false),
                new ChatTemplate("server", server, false),
                new ChatTemplate("message", message, config.GLOBAL_CHAT_ALLOW_MSG_FORMATTING)
        ));

        if (config.URLS_CLICKABLE)
            msg = msg.replaceText(config.urlReplacement);

        if (!config.GLOBAL_CHAT_PASSTHROUGH) {
            sendMessage(msg, currentServer.get().getServer());
        } else {
            sendMessage(msg);
            event.setResult(PlayerChatEvent.ChatResult.denied());
        }

        if (config.GLOBAL_CHAT_TO_CONSOLE)
            this.logger.info("GLOBAL: <{}> {}", player, message);
    }

    @Subscribe
    public void onConnect(ServerConnectedEvent event) {
        RegisteredServer currentServer = event.getServer();
        Optional<RegisteredServer> previousServer = event.getPreviousServer();

        String player = event.getPlayer().getUsername();
        String uuid = event.getPlayer().getUniqueId().toString();
        String server = currentServer.getServerInfo().getName();

        // First-time join
        if (previousServer.isEmpty() && config.FIRST_JOIN_ENABLE && !seenPlayers.contains(uuid)) {
            Component msg = parseMessage(config.FIRST_JOIN_FORMAT, List.of(
                    new ChatTemplate("player", player, false),
                    new ChatTemplate("server", server, false)
            ));
            if (config.FIRST_JOIN_PASSTHROUGH) sendMessage(msg);
            else sendMessage(msg, currentServer);

            markSeen(uuid);
            return;
        }

        // Regular join
        if (previousServer.isEmpty()) {
            if (!config.JOIN_ENABLE) return;
            Component msg = parseMessage(config.JOIN_FORMAT, List.of(
                    new ChatTemplate("player", player, false),
                    new ChatTemplate("server", server, false)
            ));
            if (config.JOIN_PASSTHROUGH) sendMessage(msg);
            else sendMessage(msg, currentServer);
        } else {
            // Server switch
            if (!config.SWITCH_ENABLE) return;
            Component msg = parseMessage(config.SWITCH_FORMAT, List.of(
                    new ChatTemplate("player", player, false),
                    new ChatTemplate("server", server, false),
                    new ChatTemplate("previous_server", previousServer.get().getServerInfo().getName(), false)
            ));

            if (!config.JOIN_PASSTHROUGH)
                sendMessage(msg, currentServer);
            else
                sendMessage(msg);
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        if (!config.LEAVE_ENABLE)
            return;

        String player = event.getPlayer().getUsername();
        Optional<ServerConnection> currentServer = event.getPlayer().getCurrentServer();

        if (currentServer.isEmpty() && config.DISCONNECT_ENABLE) {
            Component msg = parseMessage(config.DISCONNECT_FORMAT, List.of(
                    new ChatTemplate("player", player, false)
            ));

            sendMessage(msg);

            return;
        }

        String server = currentServer.get().getServerInfo().getName();

        Component msg = parseMessage(config.LEAVE_FORMAT, List.of(
                new ChatTemplate("player", player, false),
                new ChatTemplate("server", server, false)
        ));

        if (!config.LEAVE_PASSTHROUGH)
            sendMessage(msg, currentServer.get().getServer());
        else
            sendMessage(msg);
    }

    private Set<String> loadSeen() {
        try {
            if (!Files.exists(seenFile)) Files.createFile(seenFile);
            return new HashSet<>(Files.readAllLines(seenFile));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load seen players file", e);
        }
    }

    private void markSeen(String uuid) {
        if (seenPlayers.add(uuid)) {
            try {
                Files.write(seenFile, (uuid + System.lineSeparator()).getBytes(),
                        java.nio.file.StandardOpenOption.APPEND);
            } catch (IOException e) {
                logger.error("Failed to save seen player {}", uuid, e);
            }
        }
    }

    private Component parseMessage(String input, List<ChatTemplate> templates) {
        List<TagResolver.Single> list = new ArrayList<>();

        for (ChatTemplate tmpl : templates) {
            if (tmpl.parse) {
                list.add(Placeholder.parsed(tmpl.name, tmpl.value));
            } else {
                list.add(Placeholder.parsed(tmpl.name,
                        Component.text(tmpl.value).content()));
            }
        }
        return MiniMessage.miniMessage().deserialize(input,
                list.toArray(new TagResolver[0]));
    }

    private void sendMessage(Component msg) {
        for (Player p : server.getAllPlayers()) p.sendMessage(msg);
    }

    private void sendMessage(Component msg, RegisteredServer excluded) {
        for (RegisteredServer srv : server.getAllServers()) {
            if (!srv.equals(excluded)) srv.sendMessage(msg);
        }
    }

    static final class ChatTemplate {
        final String name;
        final String value;
        final Boolean parse; // should we run through minimessage's parsing?

        public ChatTemplate(String name, String value, Boolean shouldParse) {
            this.name = name;
            this.value = value;
            this.parse = shouldParse;
        }

        // <zml#2468> you'd want to use Component templates, not String templates
        // > the template system, allows you to choose between string and full components as replacements.
        // > These are executed in the main parse loop, so the string replacements can not contain MiniMessage Tags!
    }
}
