package com.shiftytab.lifesystem;

import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class Main extends JavaPlugin implements Listener {
    private Map<UUID, Integer> playerLives;
    private File configFile;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        this.playerLives = new HashMap<>();
        updateConfigFiles();
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("Lifesystem plugin enabled successfully (new version!!).");
    }

    @Override
    public void onDisable() {
        getLogger().info("Saving player life data...");
        for (UUID uuid : playerLives.keySet()) {
            config.set(uuid.toString(), playerLives.get(uuid));
        }

        try {
            config.save(configFile);
            getLogger().info("Player life data saved successfully.");
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to save player life data!", e);
        }

        getLogger().info("Lifesystem plugin disabled.");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        // Determine the used item (main hand or off hand)
        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (event.getAction().toString().contains("RIGHT_CLICK") && item.getType() == Material.TOTEM_OF_UNDYING) {
            int lives = playerLives.getOrDefault(uuid, 0);
            playerLives.put(uuid, lives + 1);
            savePlayerLives(uuid, lives + 1);

            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                if (event.getHand() == EquipmentSlot.HAND) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }

            player.sendMessage("+1 vie");
            getLogger().info("Player " + player.getName() + " gained a life. Total: " + (lives + 1));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        updateConfigFiles();

        if (config.contains(uuid.toString())) {
            int lives = config.getInt(uuid.toString());
            playerLives.put(uuid, lives);
            getLogger().info("Player " + player.getName() + " joined with " + lives + " lives.");

            if (lives <= 0) {
                player.kickPlayer("You have no lives left!");
            }
        } else {
            updatePlayerLives(uuid, 3);
            getLogger().info("New player " + player.getName() + " initialized with 3 lives.");
        }
    }

    @EventHandler
    public void OnUseTotem(EntityResurrectEvent event) {
        event.setCancelled(true);
        getLogger().info("Entity resurrect event was cancelled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (playerLives.containsKey(uuid)) {
            updateConfigFiles();
            int lives = config.getInt(uuid.toString());

            if (lives > 1) {
                int newLives = lives - 1;
                updatePlayerLives(uuid, newLives);

                player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&3You have " + newLives + " lives left."));

                getLogger().info("Player " + player.getName() + " lost a life. Lives left: " + newLives);
            } else {
                updatePlayerLives(uuid, 0);
                playerLives.remove(uuid);
                savePlayerLives(uuid, 0);
                player.kickPlayer("You have no lives left!");
                getLogger().info("Player " + player.getName() + " has been kicked for having no lives left.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        playerLives.remove(uuid);
        getLogger().info("Player " + player.getName() + " left the server. Data removed from memory.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedInventory != null && clickedItem != null) {
            Entity entity = getClickedEntity(event);
            if (entity != null && entity.getType() == EntityType.VILLAGER && clickedItem.getType() == Material.TOTEM_OF_UNDYING) {
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                player.sendMessage("You cannot trade this item with the villager!");
                event.setCancelled(true);
                getLogger().info("Player " + player.getName() + " attempted an invalid trade with a villager.");
            }
        }
    }

    private Entity getClickedEntity(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Entity) {
            return (Entity) event.getClickedInventory().getHolder();
        }
        return null;
    }

    private void updateConfigFiles() {
        this.configFile = new File(getDataFolder(), "data.yml");
        this.config = YamlConfiguration.loadConfiguration(configFile);
    }

    private void updatePlayerLives(UUID uuid, int life) {
        playerLives.put(uuid, life);
        savePlayerLives(uuid, life);
    }

    private void savePlayerLives(UUID uuid, int life) {
        config.set(uuid.toString(), life);

        try {
            config.save(configFile);
            getLogger().info("Saved lives for player UUID: " + uuid);
        } catch (IOException e) {
            getLogger().log(Level.WARNING, "Failed to save lives for player UUID: " + uuid, e);
        }
    }
}
