package com.example.zaltar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SaltarCommand implements CommandExecutor {

    private final Zaltar plugin;

    public SaltarCommand(Zaltar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }
        plugin.getEnderChestManager().spawnEnderChest(player);
        return true;
    }
}
