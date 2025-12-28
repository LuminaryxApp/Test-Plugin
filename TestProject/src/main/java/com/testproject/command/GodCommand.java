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
 * /god [player] - Toggles god mode (invincibility and no hunger loss).
 */
public class GodCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public GodCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.god")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        Player target;

        if (args.length >= 1) {
            // Targeting another player
            if (!sender.hasPermission("testproject.god.others")) {
                plugin.getMessageManager().send(sender, "no-permission");
                return true;
            }

            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "player-not-found");
                return true;
            }

            plugin.toggleGodMode(target.getUniqueId());
            boolean enabled = plugin.isInGodMode(target.getUniqueId());

            if (enabled) {
                plugin.getMessageManager().send(sender, "god.enabled-other", "player", target.getName());
                plugin.getMessageManager().send(target, "god.enabled-by-other", "sender", sender.getName());
            } else {
                plugin.getMessageManager().send(sender, "god.disabled-other", "player", target.getName());
                plugin.getMessageManager().send(target, "god.disabled-by-other", "sender", sender.getName());
            }
        } else {
            // Self - must be a player
            if (!(sender instanceof Player player)) {
                plugin.getMessageManager().send(sender, "console-not-allowed");
                return true;
            }

            plugin.toggleGodMode(player.getUniqueId());
            boolean enabled = plugin.isInGodMode(player.getUniqueId());

            if (enabled) {
                plugin.getMessageManager().send(player, "god.enabled-self");
            } else {
                plugin.getMessageManager().send(player, "god.disabled-self");
            }
        }

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender.hasPermission("testproject.god.others")) {
            String input = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}
