package com.example.zaltar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class ReloadCommand implements CommandExecutor {

    private final Zaltar plugin;

    public ReloadCommand(Zaltar plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Только игроки могут использовать эту команду.");
            return true;
        }

        if (!player.hasPermission("cc.ccreload")) {
            player.sendMessage("Нет прав.");
            return true;
        }

        plugin.reloadConfig();

        File customConfigFile = new File(plugin.getDataFolder(), "config.yml");
        if (customConfigFile.exists()) {
            FileConfiguration customConfig = YamlConfiguration.loadConfiguration(customConfigFile);
            plugin.getEnderChestManager().setConfig(customConfig);
        }

        player.sendMessage("Конфиг перезагружен: config.yml");
        return true;
    }
}
