package com.example.zaltar;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EnderChestManager implements Listener {

    private final Zaltar plugin;
    private final Map<Location, Long> chestSpawnTimes = new HashMap<>();
    private Inventory deathChestInventory;
    // Maps chest location -> TextDisplay entity UUID
    private final Map<Location, UUID> hologramEntities = new HashMap<>();
    private final Map<Location, UUID> hologramOpenEntities = new HashMap<>();
    private final Set<Location> saltarChests = new HashSet<>();
    private final Map<UUID, Long> lastClickTimes = new HashMap<>();
    private File configFile;
    private FileConfiguration config;

    public EnderChestManager(Zaltar plugin) {
        this.plugin = plugin;
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(configFile);
        config.addDefault("Время.время-до-открытия", 250);
        config.addDefault("Время.время-до-удаления-после-открытия", 30);
        config.options().copyDefaults(true);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setConfig(FileConfiguration newConfig) {
        this.config = newConfig;
    }

    public void spawnEnderChest(Player player) {
        String worldName = config.getString("Спавн.world", "spawn");
        int timeopen = config.getInt("Время.время-до-открытия", 250);
        int timedeleteopen = config.getInt("Время.время-до-удаления-после-открытия", 30);

        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage(ChatColor.RED + "Мир '" + worldName + "' не найден!");
            return;
        }

        double x = config.getDouble("Спавн.x", 100);
        double y = config.getDouble("Спавн.y", 64);
        double z = config.getDouble("Спавн.z", 100);

        Location location = new Location(world, x, y, z);

        // Set up inventory
        deathChestInventory = Bukkit.createInventory(null, 54,
                ChatColor.DARK_PURPLE + "Death Chest");
        plugin.getLootManager().loadLoot(deathChestInventory);
        fillDeathChestWithNautilusShells(deathChestInventory);

        // Place ender chest block
        location.getBlock().setType(Material.ENDER_CHEST);
        chestSpawnTimes.put(location, System.currentTimeMillis());
        saltarChests.add(location);

        createTimerDisplay(location, timeopen);

        // Schedule opening after timeopen seconds
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (saltarChests.contains(location)) {
                openSmert(location);
                removeTimerDisplay(location);
                createTimerDisplayOpen(location, timedeleteopen);

                // Schedule removal after timedeleteopen seconds
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (saltarChests.contains(location)) {
                        removeBlock(location);
                        removeTimerDisplay(location);
                        removeTimerDisplayOpen(location);
                        saltarChests.remove(location);
                        chestSpawnTimes.remove(location);
                    }
                }, timedeleteopen * 20L);
            }
        }, timeopen * 20L);
    }

    private void openSmert(Location location) {
        // Mark the chest as open (accessible)
        // The chest will already be placed; players can now click it
    }

    public void notifyBeforeSpawn() {
        // Placeholder for pre-spawn notification logic
    }

    public void tick() {
        // Called every second by the auto-spawn task
    }

    // ---------- Text Display hologram replacements ----------

    private void createTimerDisplay(Location location, int seconds) {
        Location displayLoc = location.clone().add(0.5, 1.5, 0.5);
        World world = displayLoc.getWorld();
        if (world == null) return;

        TextDisplay display = (TextDisplay) world.spawnEntity(displayLoc, EntityType.TEXT_DISPLAY);
        display.setText(formatTime(seconds));
        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 1, 0),
                new Vector3f(1.5f, 1.5f, 1.5f),
                new AxisAngle4f(0, 0, 1, 0)
        ));
        hologramEntities.put(location, display.getUniqueId());

        // Update the countdown every second
        int[] remaining = {seconds};
        int[] taskId = {-1};
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            remaining[0]--;
            Entity ent = Bukkit.getEntity(hologramEntities.getOrDefault(location, display.getUniqueId()));
            if (ent instanceof TextDisplay td && remaining[0] > 0) {
                td.setText(formatTime(remaining[0]));
            } else {
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 20L, 20L);
    }

    private void createTimerDisplayOpen(Location location, int seconds) {
        Location displayLoc = location.clone().add(0.5, 2.2, 0.5);
        World world = displayLoc.getWorld();
        if (world == null) return;

        TextDisplay display = (TextDisplay) world.spawnEntity(displayLoc, EntityType.TEXT_DISPLAY);
        display.setText(ChatColor.GREEN + "ОТКРЫТ\n" + ChatColor.YELLOW + formatTime(seconds));
        display.setBillboard(Display.Billboard.CENTER);
        display.setDefaultBackground(false);
        display.setShadowed(true);
        display.setAlignment(TextDisplay.TextAlignment.CENTER);
        display.setTransformation(new Transformation(
                new Vector3f(0, 0, 0),
                new AxisAngle4f(0, 0, 1, 0),
                new Vector3f(1.5f, 1.5f, 1.5f),
                new AxisAngle4f(0, 0, 1, 0)
        ));
        hologramOpenEntities.put(location, display.getUniqueId());

        int[] remaining = {seconds};
        int[] taskId = {-1};
        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            remaining[0]--;
            Entity ent = Bukkit.getEntity(hologramOpenEntities.getOrDefault(location, display.getUniqueId()));
            if (ent instanceof TextDisplay td && remaining[0] > 0) {
                td.setText(ChatColor.GREEN + "ОТКРЫТ\n" + ChatColor.YELLOW + formatTime(remaining[0]));
            } else {
                Bukkit.getScheduler().cancelTask(taskId[0]);
            }
        }, 20L, 20L);
    }

    private void removeTimerDisplay(Location location) {
        UUID uid = hologramEntities.remove(location);
        if (uid != null) {
            Entity ent = Bukkit.getEntity(uid);
            if (ent != null) ent.remove();
        }
    }

    private void removeTimerDisplayOpen(Location location) {
        UUID uid = hologramOpenEntities.remove(location);
        if (uid != null) {
            Entity ent = Bukkit.getEntity(uid);
            if (ent != null) ent.remove();
        }
    }

    private void removeBlock(Location location) {
        Block block = location.getBlock();
        if (block.getType() == Material.ENDER_CHEST) {
            block.setType(Material.AIR);
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int secs = totalSeconds % 60;
        if (minutes > 0) {
            return ChatColor.AQUA + "" + minutes + "м " + secs + "с";
        } else {
            return ChatColor.AQUA + "" + secs + "с";
        }
    }

    // ---------- Events ----------

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (event.getClickedBlock().getType() != Material.ENDER_CHEST) return;

        Location loc = event.getClickedBlock().getLocation();
        if (!saltarChests.contains(loc)) return;

        event.setCancelled(true);

        // Only allow opening after timeopen has passed (chest no longer in pre-open state)
        if (hologramEntities.containsKey(loc)) {
            event.getPlayer().sendMessage(ChatColor.RED + "Сундук ещё не открылся!");
            return;
        }

        // Anti-spam click
        UUID uid = event.getPlayer().getUniqueId();
        long now = System.currentTimeMillis();
        if (lastClickTimes.containsKey(uid) && now - lastClickTimes.get(uid) < 500) return;
        lastClickTimes.put(uid, now);

        openCustomInventory(event.getPlayer());
    }

    private void openCustomInventory(Player player) {
        if (deathChestInventory != null) {
            player.openInventory(deathChestInventory);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (deathChestInventory == null) return;
        if (!event.getInventory().equals(deathChestInventory)) return;

        // Anti-spam
        UUID uid = event.getWhoClicked().getUniqueId();
        long now = System.currentTimeMillis();
        if (lastClickTimes.containsKey(uid) && now - lastClickTimes.get(uid) < 200) {
            event.setCancelled(true);
            return;
        }
        lastClickTimes.put(uid, now);

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        // Prevent moving items between bottom inventory and chest
        if (event.getClickedInventory() != null &&
                !event.getClickedInventory().equals(deathChestInventory)) {
            event.setCancelled(true);
            return;
        }

        ItemStack randomLootItem = plugin.getLootManager().getRandomLootItem();
        if (randomLootItem != null) {
            ItemMeta meta = randomLootItem.getItemMeta();
            if (meta != null) {
                clickedItem.setItemMeta(meta);
            }
        }
    }

    // ---------- Loot filling ----------

    private void fillDeathChestWithNautilusShells(Inventory inventory) {
        // Fill base slots with nautilus shells as decoration/filler
        ItemStack nautilusShell = new ItemStack(Material.NAUTILUS_SHELL, 1);
        ItemMeta nautilusMeta = nautilusShell.getItemMeta();
        if (nautilusMeta != null) {
            nautilusMeta.setDisplayName(ChatColor.GRAY + "");
            nautilusShell.setItemMeta(nautilusMeta);
        }

        ItemStack grayDye = new ItemStack(Material.GRAY_DYE, 1);
        ItemMeta grayDyeMeta = grayDye.getItemMeta();
        if (grayDyeMeta != null) {
            grayDyeMeta.setDisplayName(ChatColor.GRAY + "");
            grayDye.setItemMeta(grayDyeMeta);
        }

        List<ItemStack> lootItems = plugin.getLootManager().getLootItems();
        Random random = new Random();

        int numberOfSlots = Math.min(lootItems.size(), 27);
        int itemCount = 0;

        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) slots.add(i);
        Collections.shuffle(slots, random);

        for (int slot : slots) {
            if (itemCount >= numberOfSlots) break;
            if (inventory.getItem(slot) == null) {
                ItemStack item = lootItems.get(random.nextInt(lootItems.size()));
                inventory.setItem(slot, item.clone());
                itemCount++;
            }
        }
    }

    public void cleanup() {
        // Remove all active holograms when plugin disables
        for (UUID uid : hologramEntities.values()) {
            Entity ent = Bukkit.getEntity(uid);
            if (ent != null) ent.remove();
        }
        for (UUID uid : hologramOpenEntities.values()) {
            Entity ent = Bukkit.getEntity(uid);
            if (ent != null) ent.remove();
        }
        hologramEntities.clear();
        hologramOpenEntities.clear();
    }
}
