package com.testproject;

import com.testproject.command.*;
import com.testproject.config.MessageManager;
import com.testproject.listener.GodModeListener;
import com.testproject.listener.TrashListener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Main plugin class for TestProject.
 * A simple essentials plugin with gamemode, god, openinv, enderchest, fix, tpa, and trash commands.
 */
public class TestProject extends JavaPlugin {

    private static TestProject instance;
    private MessageManager messageManager;

    // Track players in god mode
    private final Set<UUID> godModePlayers = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config
        saveDefaultConfig();
        saveResource("messages.yml", false);

        // Initialize message manager
        messageManager = new MessageManager(this);

        // Register commands
        registerCommands();

        // Register listeners
        getServer().getPluginManager().registerEvents(new GodModeListener(this), this);
        getServer().getPluginManager().registerEvents(new TrashListener(this), this);

        getLogger().info("TestProject has been enabled!");
    }

    @Override
    public void onDisable() {
        godModePlayers.clear();
        getLogger().info("TestProject has been disabled!");
    }

    private void registerCommands() {
        GamemodeCommand gamemodeCommand = new GamemodeCommand(this);
        getCommand("gamemode").setExecutor(gamemodeCommand);
        getCommand("gamemode").setTabCompleter(gamemodeCommand);

        GodCommand godCommand = new GodCommand(this);
        getCommand("god").setExecutor(godCommand);
        getCommand("god").setTabCompleter(godCommand);

        OpenInvCommand openInvCommand = new OpenInvCommand(this);
        getCommand("openinv").setExecutor(openInvCommand);
        getCommand("openinv").setTabCompleter(openInvCommand);

        EnderchestCommand enderchestCommand = new EnderchestCommand(this);
        getCommand("enderchest").setExecutor(enderchestCommand);
        getCommand("enderchest").setTabCompleter(enderchestCommand);

        FixCommand fixCommand = new FixCommand(this);
        getCommand("fix").setExecutor(fixCommand);

        TpaCommand tpaCommand = new TpaCommand(this);
        getCommand("tpa").setExecutor(tpaCommand);
        getCommand("tpa").setTabCompleter(tpaCommand);
        getCommand("tpaccept").setExecutor(tpaCommand);
        getCommand("tpdeny").setExecutor(tpaCommand);

        TrashCommand trashCommand = new TrashCommand(this);
        getCommand("trash").setExecutor(trashCommand);
    }

    public static TestProject getInstance() {
        return instance;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    // God mode management
    public boolean isInGodMode(UUID uuid) {
        return godModePlayers.contains(uuid);
    }

    public void setGodMode(UUID uuid, boolean enabled) {
        if (enabled) {
            godModePlayers.add(uuid);
        } else {
            godModePlayers.remove(uuid);
        }
    }

    public void toggleGodMode(UUID uuid) {
        if (godModePlayers.contains(uuid)) {
            godModePlayers.remove(uuid);
        } else {
            godModePlayers.add(uuid);
        }
    }
}
