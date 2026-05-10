package com.example.zaltar;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class LootManager {

    private final Zaltar plugin;
    private File lootFile;
    private FileConfiguration lootConfig;
    private List<ItemStack> lootItems = new ArrayList<>();

    public LootManager(Zaltar plugin) {
        this.plugin = plugin;
        lootFile = new File(plugin.getDataFolder(), "loot.yml");
        if (!lootFile.exists()) {
            try {
                lootFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        lootConfig = YamlConfiguration.loadConfiguration(lootFile);
        loadLootItems();
    }

    private void loadLootItems() {
        lootItems.clear();
        Object raw = lootConfig.get("lootItems");
        if (raw instanceof List<?> list) {
            for (Object obj : list) {
                if (obj instanceof ItemStack item) {
                    lootItems.add(item);
                }
            }
        }
    }

    public void loadLoot(Inventory inventory) {
        inventory.clear();
        loadLootItems();
        ItemStack[] contents = inventory.getContents();
        List<ItemStack> items = new ArrayList<>(lootItems);
        Random random = new Random();
        Collections.shuffle(items, random);

        List<Integer> availableSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) availableSlots.add(i);
        Collections.shuffle(availableSlots, random);

        Iterator<Integer> slotIt = availableSlots.iterator();
        for (ItemStack item : items) {
            if (!slotIt.hasNext()) break;
            inventory.setItem(slotIt.next(), item);
        }
    }

    public List<ItemStack> getLootItems() {
        return Collections.unmodifiableList(lootItems);
    }

    public ItemStack getRandomLootItem() {
        if (lootItems.isEmpty()) return null;
        return lootItems.get(new Random().nextInt(lootItems.size()));
    }

    public void saveLoot(Inventory inventory) {
        List<ItemStack> items = new ArrayList<>();
        for (ItemStack item : inventory.getContents()) {
            if (item != null) items.add(item);
        }
        lootConfig.set("lootItems", items);
        try {
            lootConfig.save(lootFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadLootItems();
    }
}
