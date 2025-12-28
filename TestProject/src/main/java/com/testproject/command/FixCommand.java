package com.testproject.command;

import com.testproject.TestProject;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * /fix - Repairs the item in hand to full durability.
 */
public class FixCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public FixCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.fix")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        ItemStack item = player.getInventory().getItemInMainHand();

        // Check if holding an item
        if (item.getType().isAir()) {
            plugin.getMessageManager().send(player, "fix.no-item");
            return true;
        }

        // Check if item is damageable
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable damageable)) {
            plugin.getMessageManager().send(player, "fix.not-repairable");
            return true;
        }

        // Check if already at full durability
        if (damageable.getDamage() == 0) {
            plugin.getMessageManager().send(player, "fix.already-repaired");
            return true;
        }

        // Repair the item
        damageable.setDamage(0);
        item.setItemMeta(meta);

        plugin.getMessageManager().send(player, "fix.repaired");

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
