package com.testproject.listener;

import com.testproject.TestProject;
import com.testproject.gui.TrashHolder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

/**
 * Handles trash GUI - clears items when closed.
 */
public class TrashListener implements Listener {

    private final TestProject plugin;

    public TrashListener(TestProject plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder() instanceof TrashHolder)) {
            return;
        }

        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        // Clear all items in the trash inventory (they are disposed)
        event.getInventory().clear();

        plugin.getMessageManager().send(player, "trash.disposed");
    }
}
