package com.example.zaltar;

import org.bukkit.plugin.java.JavaPlugin;

public class Zaltar extends JavaPlugin {

    private LootManager lootManager;
    private EnderChestManager enderChestManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.lootManager = new LootManager(this);
        this.enderChestManager = new EnderChestManager(this);

        getCommand("cc").setExecutor(new SaltarCommand(this));
        getCommand("ccloot").setExecutor(new AltLootCommand(this));
        getCommand("checkcc").setExecutor(new ChekSpCommand(this));
        getCommand("ccreload").setExecutor(new ReloadCommand(this));

        startAutoSpawnTask();
        getLogger().info("ZAltar enabled!");
    }

    @Override
    public void onDisable() {
        if (enderChestManager != null) {
            enderChestManager.cleanup();
        }
        getLogger().info("ZAltar disabled!");
    }

    public LootManager getLootManager() {
        return lootManager;
    }

    public EnderChestManager getEnderChestManager() {
        return enderChestManager;
    }

    private void startAutoSpawnTask() {
        // Auto-spawn task runs every 20 ticks (1 second)
        getServer().getScheduler().runTaskTimer(this, () -> {
            enderChestManager.tick();
        }, 20L, 20L);
    }
}
