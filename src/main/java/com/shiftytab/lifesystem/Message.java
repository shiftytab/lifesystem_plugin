package com.shiftytab.lifesystem;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Message {
    private File messagesFile;
    protected FileConfiguration messagesConfig;
    protected Logger logger;

    /**
     * Constructor
     *
     * @param path   The plugin data folder path
     * @param logger The plugin logger for error reporting
     */
    public Message(File path, Logger logger) {
        this.logger = logger;
        this.messagesFile = new File(path, "messages.yml");

        if (!messagesFile.exists()) {
            try {
                if (messagesFile.createNewFile()) {
                    messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);

                    Map<String, String> defaultMessages = new HashMap<>();
                    defaultMessages.put("no_permission", "&cYou do not have permission to use this command.");
                    defaultMessages.put("invalid_player", "&cThe specified player could not be found or is offline.");
                    defaultMessages.put("invalid_action", "&cInvalid action. Use add, set, or remove.");
                    defaultMessages.put("usage_lifesystem", "&cUsage: /lifesystem <add|set|remove> <player> [amount]");
                    defaultMessages.put("added_lives", "&aYou have added %amount% lives to &l%player%&r. Total: %total%.");
                    defaultMessages.put("removed_lives", "&aYou have removed %amount% lives from &l%player%&r. Total: %total%.");
                    defaultMessages.put("set_lives", "&aYou have set &l%player%&r''s lives to %amount%.");
                    defaultMessages.put("no_lives_left", "&cYou have no lives left and have been removed by an administrator.");
                    defaultMessages.put("player_lives", "&a&l%player%&r has %lives% lives.");
                    defaultMessages.put("your_lives", "&aYou have %lives% lives.");

                    for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
                        messagesConfig.set(entry.getKey(), entry.getValue());
                    }

                    messagesConfig.save(messagesFile);
                    logger.info("Default messages.yml file created successfully.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create messages.yml", e);
            }
        } else {
            messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        }
    }

    /**
     * Get message from `messages.yml` by key
     *
     * @param key The key to look up in the messages file
     * @return The formatted message or the key itself if not found
     */
    public String getMessage(String key) {
        if (messagesConfig == null) {
            return ChatColor.RED + "Error: messages.yml not loaded.";
        }
        return ChatColor.translateAlternateColorCodes('&', messagesConfig.getString(key, key));
    }
}
