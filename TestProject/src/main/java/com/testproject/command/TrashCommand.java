package com.testproject.command;

import com.testproject.TestProject;
import com.testproject.gui.TrashHolder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * /trash - Opens a disposal menu that clears items on close.
 */
public class TrashCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public TrashCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.trash")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        // Create trash inventory
        String title = plugin.getMessageManager().get("trash.title");
        Inventory trash = Bukkit.createInventory(new TrashHolder(), 54, plugin.getMessageManager().toComponent(title));

        player.openInventory(trash);
        plugin.getMessageManager().send(player, "trash.opened");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
