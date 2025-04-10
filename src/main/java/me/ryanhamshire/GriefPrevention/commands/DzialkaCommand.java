package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.TextMode;
import me.ryanhamshire.GriefPrevention.gui.ClaimMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DzialkaCommand extends CommandHandler {
    private final ClaimMenu claimMenu;

    public DzialkaCommand(@NotNull GriefPrevention plugin) {
        super(plugin, "dzialka");
        this.claimMenu = new ClaimMenu(plugin);
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender,
            @NotNull Command command,
            @NotNull String label,
            @NotNull String[] args)
    {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Ta komenda jest dostępna tylko dla graczy.");
            return true;
        }

        if (!plugin.claimsEnabledForWorld(player.getWorld())) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.ClaimsDisabledWorld);
            return true;
        }

        claimMenu.openMenu(player);
        return true;
    }
} 