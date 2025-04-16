package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ClaimFlyCommand extends CommandHandler {
    
    public ClaimFlyCommand(GriefPrevention plugin) {
        super(plugin, "claimfly");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Ta komenda może być użyta tylko przez gracza.");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("griefprevention.claimfly")) {
            player.sendMessage(ChatColor.RED + "Nie masz uprawnień do używania tej komendy.");
            return true;
        }

        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);

        if (claim == null) {
            player.sendMessage(ChatColor.RED + "Musisz znajdować się na działce aby użyć tej komendy.");
            return true;
        }

        if (claim.checkPermission(player, ClaimPermission.Build, null) != null && !claim.isAdminClaim()) {
            player.sendMessage(ChatColor.RED + "Możesz latać tylko na własnej działce.");
            return true;
        }

        if (player.getAllowFlight()) {
            player.setAllowFlight(false);
            player.setFlying(false);
            player.sendMessage(ChatColor.GREEN + "Latanie zostało wyłączone.");
        } else {
            player.setAllowFlight(true);
            player.sendMessage(ChatColor.GREEN + "Latanie zostało włączone.");
        }

        return true;
    }
} 