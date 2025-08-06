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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Meta command that handles various chat functionalities including private messages
 * This command can be used as a fallback if the individual aliases are conflicting
 */
public class ChatCommand implements SimpleCommand {
    private final ProxyServer server;
    private final ConfigManager configManager;
    private final MessageFormatter messageFormatter;
    private final ProfanityFilter profanityFilter;
    private final Logger logger;
    private final MiniMessage miniMessage;
    private final PrivateMessageCommand privateMessageCommand;

    public ChatCommand(ProxyServer server, ConfigManager configManager,
                       MessageFormatter messageFormatter, ProfanityFilter profanityFilter, Logger logger) {
        this.server = server;
        this.configManager = configManager;
        this.messageFormatter = messageFormatter;
        this.profanityFilter = profanityFilter;
        this.logger = logger;
        this.miniMessage = MiniMessage.miniMessage();
        this.privateMessageCommand = new PrivateMessageCommand(server, configManager, messageFormatter, profanityFilter, logger);
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            showHelp(source);
            return;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "msg":
            case "tell":
            case "pm":
            case "w":
            case "message":
                // Handle private message functionality
                String[] msgArgs = Arrays.copyOfRange(args, 1, args.length);
                SimpleCommand.Invocation msgInvocation = new SimpleCommand.Invocation() {
                    @Override
                    public CommandSource source() {
                        return source;
                    }

                    @Override
                    public String[] arguments() {
                        return msgArgs;
                    }
                    
                    @Override
                    public String alias() {
                        return subCommand;
                    }
                };
                privateMessageCommand.execute(msgInvocation);
                break;
                
            case "reload":
                if (!source.hasPermission("chat.reload")) {
                    Component message = miniMessage.deserialize(configManager.getChatConfig().getNoPermissionMessage());
                    source.sendMessage(message);
                    return;
                }
                configManager.loadConfig();
                Component message = miniMessage.deserialize(configManager.getChatConfig().getConfigReloadedMessage());
                source.sendMessage(message);
                logger.info("Configuration reloaded by {}", source.toString());
                break;
                
            default:
                showHelp(source);
                break;
        }
    }

    private void showHelp(CommandSource source) {
        source.sendMessage(miniMessage.deserialize("<yellow>Chat Plugin Commands:"));
        source.sendMessage(miniMessage.deserialize("<gray>• <gold>/chat msg <player> <message> <gray>- Send private message"));
        source.sendMessage(miniMessage.deserialize("<gray>• <gold>/chat tell <player> <message> <gray>- Send private message"));
        source.sendMessage(miniMessage.deserialize("<gray>• <gold>/chat pm <player> <message> <gray>- Send private message"));
        if (source.hasPermission("chat.reload")) {
            source.sendMessage(miniMessage.deserialize("<gray>• <gold>/chat reload <gray>- Reload configuration"));
        }
        
        if (configManager.getPrivateMessageConfig().isEnabled()) {
            List<String> aliases = configManager.getPrivateMessageConfig().getAliases();
            source.sendMessage(miniMessage.deserialize(
                "<gray>Available message aliases: <yellow>" + String.join(", ", aliases)
            ));
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        
        if (args.length <= 1) {
            String partial = args.length == 1 ? args[0].toLowerCase() : "";
            List<String> suggestions = new ArrayList<>(Arrays.asList("msg", "tell", "pm", "w", "message"));
            if (invocation.source().hasPermission("chat.reload")) {
                suggestions.add("reload");
            }
            return suggestions.stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
        } else if (args.length >= 2) {
            String subCommand = args[0].toLowerCase();
            if (Arrays.asList("msg", "tell", "pm", "w", "message").contains(subCommand)) {
                // Use private message command's suggestions
                String[] msgArgs = Arrays.copyOfRange(args, 1, args.length);
                SimpleCommand.Invocation msgInvocation = new SimpleCommand.Invocation() {
                    @Override
                    public CommandSource source() {
                        return invocation.source();
                    }

                    @Override
                    public String[] arguments() {
                        return msgArgs;
                    }
                    
                    @Override
                    public String alias() {
                        return subCommand;
                    }
                };
                return privateMessageCommand.suggest(msgInvocation);
            }
        }
        
        return new ArrayList<>();
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // Allow everyone to see help, specific permissions checked per subcommand
    }
}
