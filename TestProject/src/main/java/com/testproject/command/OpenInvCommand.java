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
 * /openinv <player> - Opens a player's inventory for viewing/editing.
 */
public class OpenInvCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public OpenInvCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.openinv")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().colorize("&cUsage: /openinv <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().send(sender, "player-not-found");
            return true;
        }

        if (target.equals(player)) {
            plugin.getMessageManager().send(sender, "openinv.self-not-allowed");
            return true;
        }

        // Open the target's inventory
        player.openInventory(target.getInventory());
        plugin.getMessageManager().send(player, "openinv.opened", "player", target.getName());

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}
