package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class CommandHandler implements CommandExecutor {
    protected final GriefPrevention plugin;
    private final String name;

    protected CommandHandler(@NotNull GriefPrevention plugin, @NotNull String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public String getName() {
        return name;
    }
} 