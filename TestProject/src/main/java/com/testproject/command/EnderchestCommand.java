package com.testproject.command;

import com.testproject.TestProject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /enderchest [player] - Opens a player's ender chest.
 */
public class EnderchestCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public EnderchestCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.enderchest")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        Player target;

        if (args.length >= 1) {
            // Opening another player's ender chest
            if (!sender.hasPermission("testproject.enderchest.others")) {
                plugin.getMessageManager().send(sender, "no-permission");
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "player-not-found");
                return true;
            }

            player.openInventory(target.getEnderChest());
            plugin.getMessageManager().send(player, "enderchest.opened-other", "player", target.getName());
        } else {
            // Opening own ender chest
            player.openInventory(player.getEnderChest());
            plugin.getMessageManager().send(player, "enderchest.opened-self");
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("testproject.enderchest.others")) {
            String input = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}
