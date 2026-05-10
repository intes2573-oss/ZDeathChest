package com.example.zaltar;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class AltLootCommand implements CommandExecutor, Listener {

    private final Zaltar plugin;
    private Inventory lootInventory;

    public AltLootCommand(Zaltar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        lootInventory = plugin.getServer().createInventory(null, 54,
                ChatColor.GOLD + "Редактор лута");
        plugin.getLootManager().loadLoot(lootInventory);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        player.openInventory(lootInventory);
        return true;
    }

    public Inventory getLootInventory() {
        return lootInventory;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (lootInventory == null) return;
        if (!event.getInventory().equals(lootInventory)) return;
        plugin.getLootManager().saveLoot(lootInventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (lootInventory == null) return;
        if (!event.getInventory().equals(lootInventory)) return;
        // Allow all edits in loot editor
    }
}
