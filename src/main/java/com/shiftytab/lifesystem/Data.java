package com.shiftytab.lifesystem;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Data {
    public Map<UUID, Integer> playerLives;
    public File configFile;
    public FileConfiguration config;
    public Logger logger;

    /**
     * Constructor
     */
    public Data(File path, Logger logger, Plugin plugin) {
        this.logger = logger;

        // Initialize the playerLives map
        this.playerLives = new HashMap<>();

        // Create cron job to save every 5 minutes
        Bukkit.getScheduler().runTaskTimer(plugin, this::save, 6000L, 6000L);

        // Start loading data
        this.configFile = new File(path, "data.yml");

        // Ensure the file exists or create a new one
        if (!configFile.exists()) {
            try {
                if (configFile.createNewFile()) {
                    logger.info("data.yml file created successfully.");
                }
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Could not create data.yml file", e);
            }
        }

        this.config = YamlConfiguration.loadConfiguration(configFile);

        // Load data into memory
        for (String key : this.config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int lives = this.config.getInt(key);
                this.playerLives.put(uuid, lives);
            } catch (IllegalArgumentException e) {
                logger.log(Level.WARNING, "Invalid UUID in data.yml: " + key, e);
            }
        }
    }

    /**
     * Get all values from memory and save them to `data.yml`
     */
    public void save() {
        for (Map.Entry<UUID, Integer> entry : playerLives.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(configFile);
            logger.info("All life data has been saved.");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while saving life data!", e);
        }
    }

    /**
     * Save a specific value into memory and config file for a specific UUID
     */
    public void update(UUID uuid, int lives) {
        playerLives.put(uuid, lives);
        config.set(uuid.toString(), lives);

        try {
            config.save(configFile);
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error while saving lives for player: " + uuid, e);
        }
    }
}
