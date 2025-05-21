package me.confor.velocity.chat.config;

import com.google.inject.Inject;
import com.moandjiezana.toml.Toml;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class ConfigManager {
    private static final long CURRENT_CONFIG_VERSION = 8;

    private final Path dataDir;
    private final Logger logger;
    private Toml toml;

    // Config sections
    private ChatConfig chatConfig;
    private UrlConfig urlConfig;
    private JoinConfig joinConfig;
    private FirstJoinConfig firstJoinConfig;
    private LeaveConfig leaveConfig;
    private SwitchConfig switchConfig;
    private DisconnectConfig disconnectConfig;
    private MentionConfig mentionConfig;
    private PrivateMessageConfig privateMessageConfig;
    private ProfanityConfig profanityConfig;
    private GlobalChatConfig globalChatConfig;

    // Map old config keys to their new versions for migration
    private static final Map<String, String> CONFIG_MIGRATIONS = new HashMap<>();

    static {
        // Define migrations here as needed
        // Example: CONFIG_MIGRATIONS.put("old.config.path", "new.config.path");
    }

    @Inject
    public ConfigManager(Path dataDir, Logger logger) {
        this.dataDir = dataDir;
        this.logger = logger;

        loadConfig();
    }

    @Subscribe
    public void onProxyReload(ProxyReloadEvent event) {
        logger.info("Reloading Chat plugin configuration...");
        loadConfig();
        logger.info("Chat plugin configuration reloaded");
    }

    public void loadConfig() {
        loadConfigFile();
        initConfigSections();
    }

    private void loadConfigFile() {
        File dir = dataDir.toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, "config.toml");

        // Create default config if it doesn't exist
        if (!file.exists()) {
            try (InputStream in = getClass().getResourceAsStream("/config.toml")) {
                if (in == null) {
                    throw new IOException("Default config resource not found");
                }
                Files.copy(in, file.toPath());
                logger.info("Created default configuration file");
            } catch (IOException e) {
                logger.error("Failed to create default configuration file", e);
                throw new RuntimeException("ERROR: Can't write default configuration file", e);
            }
        }

        // Load the TOML file
        this.toml = new Toml().read(file);

        // Check version and handle migration if needed
        long version = this.toml.getLong("config_version", 0L);
        if (version < CURRENT_CONFIG_VERSION) {
            handleConfigMigration(file, version);
        }
    }

    private void handleConfigMigration(File configFile, long currentVersion) {
        logger.warn("Config version mismatch: found v{}, expected v{}. Attempting to migrate...",
                currentVersion, CURRENT_CONFIG_VERSION);

        try {
            // Create backup of old config
            Files.copy(configFile.toPath(),
                    configFile.toPath().resolveSibling("config.toml.backup-v" + currentVersion));
            logger.info("Created backup of old config: config.toml.backup-v{}", currentVersion);

            // For a simple approach, we'll just update the version number
            // In a more complete implementation, you'd migrate settings as needed
            String content = new String(Files.readAllBytes(configFile.toPath()));
            content = content.replace("config_version = " + currentVersion,
                    "config_version = " + CURRENT_CONFIG_VERSION);

            // Apply any migrations needed
            for (Map.Entry<String, String> migration : CONFIG_MIGRATIONS.entrySet()) {
                content = content.replace(migration.getKey(), migration.getValue());
            }

            Files.write(configFile.toPath(), content.getBytes());

            // Reload the config after migration
            this.toml = new Toml().read(configFile);
            logger.info("Successfully migrated config from v{} to v{}",
                    currentVersion, CURRENT_CONFIG_VERSION);

        } catch (IOException e) {
            logger.error("Failed to migrate configuration file", e);
            throw new RuntimeException("ERROR: Failed to migrate configuration. Please update manually.", e);
        }
    }

    private void initConfigSections() {
        chatConfig = new ChatConfig(
                toml.getBoolean("chat.enable", true),
                toml.getBoolean("chat.log_to_console", false),
                toml.getBoolean("chat.passthrough", true),
                toml.getBoolean("chat.parse_player_messages", false),
                toml.getString("chat.format", "<<player>> <message>")
        );

        urlConfig = new UrlConfig(
                toml.getBoolean("urls.clickable", true),
                toml.getString("urls.pattern", "https?:\\/\\/\\S+")
        );

        joinConfig = new JoinConfig(
                toml.getBoolean("join.enable", false),
                toml.getBoolean("join.passthrough", false),
                toml.getString("join.format", "<yellow><player> joined <server></yellow>")
        );

        firstJoinConfig = new FirstJoinConfig(
                toml.getBoolean("first_join.enable", true),
                toml.getBoolean("first_join.passthrough", false),
                toml.getString("first_join.format", "<green>Welcome <player> to <server> for the first time!</green>")
        );

        leaveConfig = new LeaveConfig(
                toml.getBoolean("leave.enable", false),
                toml.getBoolean("leave.passthrough", false),
                toml.getString("leave.format", "<yellow><player> left <server></yellow>")
        );

        switchConfig = new SwitchConfig(
                toml.getBoolean("switch.enable", true),
                toml.getString("switch.format", "<yellow><player> switched from <previous_server> to <server></yellow>")
        );

        disconnectConfig = new DisconnectConfig(
                toml.getBoolean("disconnect.enable", true),
                toml.getString("disconnect.format", "<yellow><player> was disconnected</yellow>")
        );

        mentionConfig = new MentionConfig(
                toml.getBoolean("mentions.enable", true),
                toml.getString("mentions.color", "<gold>"),
                toml.getString("mentions.sound", "minecraft:block.note_block.pling"),
                toml.getString("mentions.title", "<gold>Упоминание"),
                toml.getString("mentions.subtitle", "<yellow><player> упомянул вас")
        );

        privateMessageConfig = new PrivateMessageConfig(
                toml.getBoolean("private_messages.enable", true),
                toml.getBoolean("private_messages.log_to_console", true),
                toml.getString("private_messages.format", "<white><sender><reset> <gray>><reset> <white><recipient><reset><gray>: <reset><message>"),
                toml.getString("private_messages.console_log_format", "[PRIVATE] <sender> -> <recipient>: <message>")
        );

        // Initialize profanity config with default word lists
        List<String> defaultProfanityWords = Arrays.asList(
                "ахуе", "бля", "еба", "ёба", "нахуй", "пизд", "хуй", "хую", "сука", "блят",
                "ганд", "даун", "дибил", "долбаёб", "долбаоб", "канцлагер",
                "концлагер", "конча", "конче", "мраз", "нига", "пидор", "пидр",
                "уеб", "уёб", "хентай", "чечен", "чмо", "фашист", "нацист"
        );

        profanityConfig = new ProfanityConfig(
                toml.getBoolean("profanity.enable", true),
                toml.getList("profanity.profanity_words", defaultProfanityWords)
        );

        globalChatConfig = new GlobalChatConfig(
                toml.getString("global_chat.warning_message", "<gray>Символ ! не обязателен")
        );
    }

    public ChatConfig getChatConfig() {
        return chatConfig;
    }

    public UrlConfig getUrlConfig() {
        return urlConfig;
    }

    public JoinConfig getJoinConfig() {
        return joinConfig;
    }

    public FirstJoinConfig getFirstJoinConfig() {
        return firstJoinConfig;
    }

    public LeaveConfig getLeaveConfig() {
        return leaveConfig;
    }

    public SwitchConfig getSwitchConfig() {
        return switchConfig;
    }

    public DisconnectConfig getDisconnectConfig() {
        return disconnectConfig;
    }

    public MentionConfig getMentionConfig() {
        return mentionConfig;
    }

    public PrivateMessageConfig getPrivateMessageConfig() {
        return privateMessageConfig;
    }

    public ProfanityConfig getProfanityConfig() {
        return profanityConfig;
    }

    public GlobalChatConfig getGlobalChatConfig() {
        return globalChatConfig;
    }

    public TextReplacementConfig getUrlReplacementConfig() {
        return TextReplacementConfig.builder()
                .match(Pattern.compile(urlConfig.getPattern()))
                .replacement(text -> text.clickEvent(ClickEvent.openUrl(text.content())))
                .build();
    }
}