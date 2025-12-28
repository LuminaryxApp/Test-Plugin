package com.testproject.command;

import com.testproject.TestProject;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * /tpa <player> - Send teleport request.
 * /tpaccept - Accept incoming teleport request.
 * /tpdeny - Deny incoming teleport request.
 */
public class TpaCommand implements CommandExecutor, TabCompleter {

    private final TestProject plugin;

    // Maps target UUID -> requester UUID
    private final Map<UUID, UUID> pendingRequests = new HashMap<>();
    // Maps target UUID -> expiration task
    private final Map<UUID, BukkitTask> expirationTasks = new HashMap<>();

    private static final int REQUEST_TIMEOUT_SECONDS = 60;

    public TpaCommand(TestProject plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            plugin.getMessageManager().send(sender, "player-only");
            return true;
        }

        String cmd = command.getName().toLowerCase();

        return switch (cmd) {
            case "tpa" -> handleTpa(player, args);
            case "tpaccept" -> handleTpAccept(player);
            case "tpdeny" -> handleTpDeny(player);
            default -> false;
        };
    }

    private boolean handleTpa(Player player, String[] args) {
        if (!player.hasPermission("testproject.tpa")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(plugin.getMessageManager().colorize("&cUsage: /tpa <player>"));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            plugin.getMessageManager().send(player, "player-not-found");
            return true;
        }

        if (target.equals(player)) {
            plugin.getMessageManager().send(player, "tpa.self-not-allowed");
            return true;
        }

        // Check if already has pending request to this player
        if (pendingRequests.containsKey(target.getUniqueId()) &&
            pendingRequests.get(target.getUniqueId()).equals(player.getUniqueId())) {
            plugin.getMessageManager().send(player, "tpa.already-pending", "player", target.getName());
            return true;
        }

        // Cancel any existing request from this player
        cancelExistingRequest(player.getUniqueId());

        // Store the request
        pendingRequests.put(target.getUniqueId(), player.getUniqueId());

        // Set expiration timer
        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (pendingRequests.remove(target.getUniqueId(), player.getUniqueId())) {
                expirationTasks.remove(target.getUniqueId());
                if (player.isOnline()) {
                    plugin.getMessageManager().send(player, "tpa.expired", "player", target.getName());
                }
            }
        }, REQUEST_TIMEOUT_SECONDS * 20L);

        expirationTasks.put(target.getUniqueId(), task);

        // Notify both players
        plugin.getMessageManager().send(player, "tpa.sent", "player", target.getName());
        plugin.getMessageManager().send(target, "tpa.received", "player", player.getName());

        return true;
    }

    private boolean handleTpAccept(Player player) {
        if (!player.hasPermission("testproject.tpaccept")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }

        UUID requesterUuid = pendingRequests.remove(player.getUniqueId());
        if (requesterUuid == null) {
            plugin.getMessageManager().send(player, "tpa.no-pending");
            return true;
        }

        // Cancel expiration task
        BukkitTask task = expirationTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        Player requester = Bukkit.getPlayer(requesterUuid);
        if (requester == null || !requester.isOnline()) {
            plugin.getMessageManager().send(player, "tpa.requester-offline");
            return true;
        }

        // Teleport requester to target
        requester.teleport(player.getLocation());

        plugin.getMessageManager().send(player, "tpa.accepted", "player", requester.getName());
        plugin.getMessageManager().send(requester, "tpa.accepted-notify", "player", player.getName());

        return true;
    }

    private boolean handleTpDeny(Player player) {
        if (!player.hasPermission("testproject.tpdeny")) {
            plugin.getMessageManager().send(player, "no-permission");
            return true;
        }

        UUID requesterUuid = pendingRequests.remove(player.getUniqueId());
        if (requesterUuid == null) {
            plugin.getMessageManager().send(player, "tpa.no-pending");
            return true;
        }

        // Cancel expiration task
        BukkitTask task = expirationTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }

        Player requester = Bukkit.getPlayer(requesterUuid);
        plugin.getMessageManager().send(player, "tpa.denied");

        if (requester != null && requester.isOnline()) {
            plugin.getMessageManager().send(requester, "tpa.denied-notify", "player", player.getName());
        }

        return true;
    }

    private void cancelExistingRequest(UUID requesterUuid) {
        // Find and remove any existing request from this requester
        Iterator<Map.Entry<UUID, UUID>> it = pendingRequests.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, UUID> entry = it.next();
            if (entry.getValue().equals(requesterUuid)) {
                BukkitTask task = expirationTasks.remove(entry.getKey());
                if (task != null) {
                    task.cancel();
                }
                it.remove();
                break;
            }
        }
    }

    public void clearAllRequests() {
        for (BukkitTask task : expirationTasks.values()) {
            task.cancel();
        }
        expirationTasks.clear();
        pendingRequests.clear();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (command.getName().equalsIgnoreCase("tpa") && args.length == 1) {
            String input = args[0].toLowerCase();
            completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList()));
        }

        return completions;
    }
}
