package com.shiftytab.lifesystem;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public class Main extends JavaPlugin implements Listener, TabExecutor {
    /**
     * Prefix for all commands
     */
    private static final String PREFIX =  ChatColor.DARK_AQUA + "LifeSystem" + ChatColor.AQUA + " » " + ChatColor.RESET;
    /**
     * Instance of message class
     */
    private Message Message;
    /**
     * Instance of data class
     */
    private Data Data;

    /**
     * On plugin enable
     */
    @Override
    public void onEnable() {

        /**
         * Create plugin folder to store differents configuration files
         */
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        /**
         * Instance all class needed
         */
        Data = new Data(getDataFolder(), getLogger(), this);
        Message = new Message(getDataFolder(), getLogger());

        /**
         * Register/Setter events
         */
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getCommand("lifesystem").setExecutor(this);
        getCommand("lifesystem").setTabCompleter(this);

        /**
         * Good ! This work !
         */
        getLogger().info("LifeSystem plugin enabled successfully.");
    }

    /**
     * On plugin disable
     */
    @Override
    public void onDisable() {
        Data.save();
        getLogger().info("LifeSystem plugin disabled. All data has been saved.");
    }

    /**
     * Listener on command is typing in chat
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("lifesystem") && !command.getName().equalsIgnoreCase("lifes") && !command.getName().equalsIgnoreCase("life")) {
            return false;
        }

        if (command.getName().equalsIgnoreCase("lifesystem")) {
            if (!(sender.hasPermission("lifesystem.admin"))) {
                sender.sendMessage(PREFIX + Message.getMessage("no_permission"));
                return true;
            }

            if (args.length < 2) {
                sender.sendMessage(PREFIX + Message.getMessage("usage_lifesystem"));
                return true;
            }

            String action = args[0].toLowerCase();
            Player target = Bukkit.getPlayer(args[1]);

            if (target == null) {
                sender.sendMessage(PREFIX + Message.getMessage("invalid_player"));
                return true;
            }

            UUID targetUUID = target.getUniqueId();
            int currentLives = Data.playerLives.getOrDefault(targetUUID, 3);

            try {
                if (action.equals("add")) {
                    if (args.length < 3) {
                        sender.sendMessage(PREFIX + Message.getMessage("usage_lifesystem"));
                        return true;
                    }
                    int amount = Integer.parseInt(args[2]);
                    Data.update(targetUUID, currentLives + amount);
                    sender.sendMessage(PREFIX + Message.getMessage("added_lives")
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%player%", target.getName())
                            .replace("%total%", String.valueOf(currentLives + amount)));
                } else if (action.equals("set")) {
                    if (args.length < 3) {
                        sender.sendMessage(PREFIX + Message.getMessage("usage_lifesystem"));
                        return true;
                    }
                    int amount = Integer.parseInt(args[2]);
                    Data.update(targetUUID, amount);
                    sender.sendMessage(PREFIX + Message.getMessage("set_lives")
                            .replace("%amount%", String.valueOf(amount))
                            .replace("%player%", target.getName()));
                } else if (action.equals("remove")) {
                    if (args.length < 3) {
                        sender.sendMessage(PREFIX + Message.getMessage("usage_lifesystem"));
                        return true;
                    }
                    int amount = Integer.parseInt(args[2]);
                    if (currentLives - amount <= 0) {
                        Data.update(targetUUID, 0);
                        target.kickPlayer(PREFIX + Message.getMessage("no_lives_left"));
                        sender.sendMessage(PREFIX + Message.getMessage("removed_lives")
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%player%", target.getName())
                                .replace("%total%", "0"));
                    } else {
                        Data.update(targetUUID, currentLives - amount);
                        sender.sendMessage(PREFIX + Message.getMessage("removed_lives")
                                .replace("%amount%", String.valueOf(amount))
                                .replace("%player%", target.getName())
                                .replace("%total%", String.valueOf(currentLives - amount)));
                    }
                } else {
                    sender.sendMessage(PREFIX + Message.getMessage("invalid_action"));
                }
            } catch (NumberFormatException e) {
                sender.sendMessage(PREFIX + Message.getMessage("invalid_action"));
            }
        }

        if (command.getName().equalsIgnoreCase("life") || command.getName().equalsIgnoreCase("lifes")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    UUID uuid = player.getUniqueId();

                    int lives = Data.playerLives.getOrDefault(uuid, 3);
                    player.sendMessage(PREFIX + Message.getMessage("your_lives").replace("%lives%", String.valueOf(lives)));
                } else {
                    sender.sendMessage(PREFIX + Message.getMessage("no_permission"));
                }
            } else if (args.length == 1) {
                if (sender.hasPermission("lifesystem.viewothers")) {
                    Player target = Bukkit.getPlayer(args[0]);

                    if (target != null) {
                        UUID targetUUID = target.getUniqueId();
                        int targetLives = Data.playerLives.getOrDefault(targetUUID, 3);

                        sender.sendMessage(PREFIX + Message.getMessage("player_lives")
                                .replace("%player%", target.getName())
                                .replace("%lives%", String.valueOf(targetLives)));
                    } else {
                        sender.sendMessage(PREFIX + Message.getMessage("invalid_player"));
                    }
                } else {
                    sender.sendMessage(PREFIX + Message.getMessage("no_permission"));
                }
            } else {
                sender.sendMessage(PREFIX + Message.getMessage("usage_lifesystem"));
            }
            return true;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("lifesystem")) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            completions.add("add");
            completions.add("set");
            completions.add("remove");
        } else if (args.length == 2) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                completions.add(player.getName());
            }
        } else if (args.length == 3) {
            completions.add("1");
            completions.add("5");
            completions.add("10");
        }
        return completions;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ItemStack item = event.getHand() == EquipmentSlot.HAND
                ? player.getInventory().getItemInMainHand()
                : player.getInventory().getItemInOffHand();

        if (event.getAction().toString().contains("RIGHT_CLICK") && item.getType() == Material.TOTEM_OF_UNDYING) {
            int lives = Data.playerLives.getOrDefault(uuid, 3);
            Data.update(uuid, lives + 1);

            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1.0f, 1.0f);
            player.sendMessage(PREFIX + Message.getMessage("added_lives")
                    .replace("%amount%", "1")
                    .replace("%player%", player.getName())
                    .replace("%total%", String.valueOf(lives + 1)));

            if (item.getAmount() > 1) {
                item.setAmount(item.getAmount() - 1);
            } else {
                if (event.getHand() == EquipmentSlot.HAND) {
                    player.getInventory().setItemInMainHand(null);
                } else {
                    player.getInventory().setItemInOffHand(null);
                }
            }

            getLogger().info("Player " + player.getName() + " gained a life. Total: " + (lives + 1));
            event.setCancelled(true);
        }
    }

    /**
     * Listener: On Player join server
     * @param event
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        int lives = Data.playerLives.getOrDefault(uuid, Data.config.contains(uuid.toString()) ? Data.config.getInt(uuid.toString()) : 3);
        Data.playerLives.put(uuid, lives);

        if (lives <= 0) {
            player.kickPlayer(PREFIX + Message.getMessage("no_lives_left"));
            getLogger().info("Player " + player.getName() + " was kicked for having no lives.");
        } else {
            player.sendMessage(PREFIX + Message.getMessage("your_lives").replace("%lives%", String.valueOf(lives)));
            getLogger().info("Player " + player.getName() + " joined with " + lives + " lives.");
        }
    }

    /**
     * Listener: On player dying
     * @param event
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();

        if (Data.playerLives.containsKey(uuid)) {
            int lives = Data.playerLives.get(uuid);

            if (lives > 1) {
                Data.update(uuid, lives - 1);
                player.sendMessage(PREFIX + Message.getMessage("your_lives").replace("%lives%", String.valueOf(lives - 1)));
                getLogger().info("Player " + player.getName() + " lost a life. Lives left: " + (lives - 1));
            } else {
                Data.update(uuid, 0);
                player.kickPlayer(PREFIX + Message.getMessage("no_lives_left"));
                getLogger().info("Player " + player.getName() + " was kicked for having no lives.");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        Data.playerLives.remove(uuid);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Entity entity = getClickedEntity(event);
        ItemStack clickedItem = event.getCurrentItem();

        if (entity != null && entity.getType() == EntityType.VILLAGER && clickedItem != null && clickedItem.getType() == Material.TOTEM_OF_UNDYING) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            player.sendMessage(PREFIX + Message.getMessage("no_permission"));
            event.setCancelled(true);
        }
    }

    private Entity getClickedEntity(InventoryClickEvent event) {
        if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof Entity) {
            return (Entity) event.getClickedInventory().getHolder();
        }
        return null;
    }
}