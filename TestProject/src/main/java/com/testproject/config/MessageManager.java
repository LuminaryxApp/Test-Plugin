package com.testproject.config;

import com.testproject.TestProject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages all plugin messages from messages.yml
 */
public class MessageManager {

    private final TestProject plugin;
    private FileConfiguration messages;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacyAmpersand();

    public MessageManager(TestProject plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messages = YamlConfiguration.loadConfiguration(messagesFile);
    }

    /**
     * Get a raw message string from config.
     */
    public String getRaw(String path) {
        return messages.getString(path, "&cMissing message: " + path);
    }

    /**
     * Get a message with color codes translated.
     */
    public String get(String path) {
        return colorize(getRaw(path));
    }

    /**
     * Get a message with placeholders replaced.
     */
    public String get(String path, String... placeholders) {
        String message = getRaw(path);

        // Replace placeholders in pairs: key1, value1, key2, value2, ...
        for (int i = 0; i < placeholders.length - 1; i += 2) {
            message = message.replace("%" + placeholders[i] + "%", placeholders[i + 1]);
        }

        return colorize(message);
    }

    /**
     * Get a message with a map of placeholders.
     */
    public String get(String path, Map<String, String> placeholders) {
        String message = getRaw(path);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return colorize(message);
    }

    /**
     * Send a message to a sender with optional prefix.
     */
    public void send(CommandSender sender, String path, boolean prefix, String... placeholders) {
        String message = get(path, placeholders);
        if (prefix) {
            message = get("prefix") + message;
        }
        sender.sendMessage(toComponent(message));
    }

    /**
     * Send a message with prefix.
     */
    public void send(CommandSender sender, String path, String... placeholders) {
        send(sender, path, true, placeholders);
    }

    /**
     * Send a raw message without prefix.
     */
    public void sendRaw(CommandSender sender, String path, String... placeholders) {
        send(sender, path, false, placeholders);
    }

    /**
     * Get an integer value from config.
     */
    public int getInt(String path, int defaultValue) {
        return messages.getInt(path, defaultValue);
    }

    /**
     * Translate color codes.
     */
    public String colorize(String message) {
        if (message == null) return "";
        // Handle hex colors like &#RRGGBB
        message = translateHexColorCodes(message);
        // Handle legacy & color codes
        return message.replace("&", "§");
    }

    /**
     * Convert string to Adventure Component.
     */
    public Component toComponent(String message) {
        return legacySerializer.deserialize(message.replace("§", "&")).asComponent();
    }

    /**
     * Translate hex color codes like &#RRGGBB to Minecraft format.
     */
    private String translateHexColorCodes(String message) {
        StringBuilder result = new StringBuilder();
        char[] chars = message.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '&' && i + 7 < chars.length && chars[i + 1] == '#') {
                // Found potential hex code
                String hex = message.substring(i + 2, i + 8);
                if (hex.matches("[0-9A-Fa-f]{6}")) {
                    result.append("§x");
                    for (char c : hex.toCharArray()) {
                        result.append("§").append(Character.toLowerCase(c));
                    }
                    i += 7; // Skip past the hex code
                    continue;
                }
            }
            result.append(chars[i]);
        }

        return result.toString();
    }

    /**
     * Helper to create placeholder map.
     */
    public static Map<String, String> placeholders(String... pairs) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < pairs.length - 1; i += 2) {
            map.put(pairs[i], pairs[i + 1]);
        }
        return map;
    }
}
