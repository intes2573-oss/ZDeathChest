package com.example.zaltar;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class CustomInventoryHandler implements Listener {

    private final Zaltar plugin;

    public CustomInventoryHandler(Zaltar plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String title = event.getView().getTitle();
        // Handle inventory close logic if needed
        if (title != null && title.contains("Death Chest")) {
            // Custom logic on close
        }
    }
}
