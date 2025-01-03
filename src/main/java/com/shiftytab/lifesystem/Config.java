package com.shiftytab.lifesystem;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Config {

    public File file;
    public FileConfiguration config;
    public Logger logger;

    /**
     * Constructor
     */
    Config(File path, Logger logger) {
        this.logger = logger;

        /**
         * Load config files
         */
        this.file = new File(path, "config.yml");

        /**
         * Create default config file
         */
        if (!this.file.exists()) {
            CreateDefaultFile();
        }
    }

    /**
     * Create default file
     */
    public void CreateDefaultFile() {
        config = YamlConfiguration.loadConfiguration(file);

        Map<String, Integer> settings = new HashMap<>();
        settings.put("lives_default", 10);
        settings.put("lives_sync_internal", 6000);

        for (Map.Entry<String, Integer> entry : settings.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
            }
        }

        try {
            config.save(file);
            logger.info("Default config.yml file created successfully.");
        } catch (IOException e) {
            logger.severe("Failed to save the config file: " + e.getMessage());
        }
    }

    /**
     * Get config value from `config.yml` by key
     *
     * @param key The key to look up in the config file
     * @return Value of key config
     */
    public String getConfig(String key) {
        if (config == null) {
            return ChatColor.RED + "Error: config.yml not loaded.";
        }
        return config.getString(key, key);
    }
}
