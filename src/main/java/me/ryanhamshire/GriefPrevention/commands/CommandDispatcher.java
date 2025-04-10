package me.ryanhamshire.GriefPrevention.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CommandDispatcher implements CommandExecutor {
    private final Map<String, CommandHandler> commands = new HashMap<>();

    public void registerCommand(CommandHandler handler) {
        commands.put(handler.getName().toLowerCase(), handler);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandHandler handler = commands.get(command.getName().toLowerCase());
        if (handler != null) {
            return handler.onCommand(sender, command, label, args);
        }
        return false;
    }
} 