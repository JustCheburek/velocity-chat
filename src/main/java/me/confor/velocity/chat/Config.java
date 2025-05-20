package me.confor.velocity.chat;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class Config {
    static final long CONFIG_VERSION = 7;

    private final Path dataDir;
    private Toml toml;

    // is there a less ugly way of doing this?
    // defaults are set in loadConfigs()

    public boolean GLOBAL_CHAT_ENABLED;
    public boolean GLOBAL_CHAT_TO_CONSOLE;
    public boolean GLOBAL_CHAT_PASSTHROUGH;
    public boolean GLOBAL_CHAT_ALLOW_MSG_FORMATTING;
    public String GLOBAL_CHAT_FORMAT;

    public boolean URLS_CLICKABLE;
    public String URLS_PATTERN;
    public TextReplacementConfig urlReplacement;

    public boolean JOIN_ENABLE;
    public boolean JOIN_PASSTHROUGH;
    public String JOIN_FORMAT;

    public boolean FIRST_JOIN_ENABLE;
    public boolean FIRST_JOIN_PASSTHROUGH;
    public String FIRST_JOIN_FORMAT;

    public boolean LEAVE_ENABLE;
    public boolean LEAVE_PASSTHROUGH;
    public String LEAVE_FORMAT;

    public boolean SWITCH_ENABLE;
    public String SWITCH_FORMAT;

    public boolean DISCONNECT_ENABLE;
    public String DISCONNECT_FORMAT;

    @Inject
    public Config(@DataDirectory Path dataDir) {
        this.dataDir = dataDir;

        loadFile();
        loadConfigs();

        this.urlReplacement = TextReplacementConfig.builder()
                .match(Pattern.compile(this.URLS_PATTERN))
                .replacement(text -> text.clickEvent(ClickEvent.openUrl(text.content())))
                .build();
    }

    private void loadFile() {
        File dir = dataDir.toFile();
        if (!dir.exists()) dir.mkdir();

        File file = new File(dir, "config.toml");
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                Files.copy(in, file.toPath());
            } catch (IOException e) {
                throw new RuntimeException("ERROR: Can't write default configuration file", e);
            }
        }

        this.toml = new Toml().read(file);
        long version = this.toml.getLong("config_version", 0L);
        if (version != CONFIG_VERSION) {
            throw new RuntimeException("ERROR: Config version mismatch.");
        }
    }

    public void loadConfigs() {
        // global chat
        GLOBAL_CHAT_ENABLED = toml.getBoolean("chat.enable", true);
        GLOBAL_CHAT_TO_CONSOLE = toml.getBoolean("chat.log_to_console", false);
        GLOBAL_CHAT_PASSTHROUGH = toml.getBoolean("chat.passthrough", true);
        GLOBAL_CHAT_ALLOW_MSG_FORMATTING = toml.getBoolean("chat.parse_player_messages", false);
        GLOBAL_CHAT_FORMAT = toml.getString("chat.format", "<<player>> <message>");

        // urls
        URLS_CLICKABLE = toml.getBoolean("urls.clickable", true);
        URLS_PATTERN = toml.getString("urls.pattern", "https?:\\/\\/\\S+");

        // join
        JOIN_ENABLE = toml.getBoolean("join.enable", false);
        JOIN_PASSTHROUGH = toml.getBoolean("join.passthrough", false);
        JOIN_FORMAT = toml.getString("join.format", "<yellow><player> joined <server></yellow>");

        // first join
        FIRST_JOIN_ENABLE = toml.getBoolean("first_join.enable", true);
        FIRST_JOIN_PASSTHROUGH = toml.getBoolean("first_join.passthrough", false);
        FIRST_JOIN_FORMAT = toml.getString(
                "first_join.format",
                "<green>Welcome <player> to <server> for the first time!</green>"
        );

        // leave
        LEAVE_ENABLE = toml.getBoolean("leave.enable", false);
        LEAVE_PASSTHROUGH = toml.getBoolean("leave.passthrough", false);
        LEAVE_FORMAT = toml.getString("leave.format", "<yellow><player> left <server></yellow>");

        // disconnect
        DISCONNECT_ENABLE = toml.getBoolean("disconnect.enable", true);
        DISCONNECT_FORMAT = toml.getString("disconnect.format", "<yellow><player> was disconnected</yellow>");

        // switch
        SWITCH_ENABLE = toml.getBoolean("switch.enable", true);
        SWITCH_FORMAT = toml.getString(
                "switch.format",
                "<yellow><player> switched from <previous_server> to <server></yellow>"
        );
    }
}
