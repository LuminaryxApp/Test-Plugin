package com.testproject.command;

import com.testproject.TestProject;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
 * /gamemode <type> [player] - Sets the player's gamemode.
 */
public class GamemodeCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    public GamemodeCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("testproject.gamemode")) {
            plugin.getMessageManager().send(sender, "no-permission");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.getMessageManager().colorize("&cUsage: /gamemode <type> [player]"));
            return true;
        }

        // Parse gamemode
        GameMode gamemode = parseGameMode(args[0]);
        if (gamemode == null) {
            plugin.getMessageManager().send(sender, "gamemode.invalid-type");
            return true;
        }

        // Check permission for specific gamemode
        String modePerm = "testproject.gamemode." + gamemode.name().toLowerCase();
        if (!sender.hasPermission(modePerm)) {
            plugin.getMessageManager().send(sender, "gamemode.no-permission-mode", "gamemode", gamemode.name().toLowerCase());
            return true;
        }

        Player target;

        if (args.length >= 2) {
            // Targeting another player
            if (!sender.hasPermission("testproject.gamemode.others")) {
                plugin.getMessageManager().send(sender, "no-permission");
                return true;
            }

            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                plugin.getMessageManager().send(sender, "player-not-found");
                return true;
            }

            target.setGameMode(gamemode);
            plugin.getMessageManager().send(sender, "gamemode.changed-other",
                    "player", target.getName(),
                    "gamemode", formatGameMode(gamemode));
            plugin.getMessageManager().send(target, "gamemode.changed-by-other",
                    "gamemode", formatGameMode(gamemode),
                    "sender", sender.getName());
        } else {
            // Self - must be a player
            if (!(sender instanceof Player player)) {
                plugin.getMessageManager().send(sender, "console-not-allowed");
                return true;
            }

            player.setGameMode(gamemode);
            plugin.getMessageManager().send(player, "gamemode.changed-self",
                    "gamemode", formatGameMode(gamemode));
        }

        return true;
    }

    private GameMode parseGameMode(String input) {
        return switch (input.toLowerCase()) {
            case "0", "s", "survival" -> GameMode.SURVIVAL;
            case "1", "c", "creative" -> GameMode.CREATIVE;
            case "2", "a", "adventure" -> GameMode.ADVENTURE;
            case "3", "sp", "spectator" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    private String formatGameMode(GameMode mode) {
        return mode.name().charAt(0) + mode.name().substring(1).toLowerCase();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> modes = List.of("survival", "creative", "adventure", "spectator");
            String input = args[0].toLowerCase();
            completions.addAll(modes.stream().filter(m -> m.startsWith(input)).toList());
        } else if (args.length == 2 && sender.hasPermission("testproject.gamemode.others")) {
            String input = args[1].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}
