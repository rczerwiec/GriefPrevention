package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class TrustCommand extends CommandHandler {

    public TrustCommand(GriefPrevention plugin) {
        super(plugin, "trust");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Ta komenda może być użyta tylko przez gracza.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Użycie: /trust <gracz>");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);

        if (claim == null) {
            player.sendMessage(ChatColor.RED + "Musisz stać na działce aby dodać do niej zaufaną osobę.");
            return true;
        }

        if (!claim.getOwnerID().equals(player.getUniqueId()) && !player.hasPermission("griefprevention.adminclaims")) {
            player.sendMessage(ChatColor.RED + "Możesz dodawać zaufane osoby tylko do własnej działki.");
            return true;
        }

        String targetName = args[0];
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

        if (targetPlayer == null || !targetPlayer.hasPlayedBefore()) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono gracza o nicku " + targetName);
            return true;
        }

        claim.setPermission(targetPlayer.getUniqueId().toString(), ClaimPermission.Build);
        plugin.dataStore.saveClaim(claim);

        player.sendMessage(ChatColor.GREEN + "Dodano gracza " + targetName + " jako zaufaną osobę do działki.");
        
        if (targetPlayer.isOnline()) {
            Player onlineTarget = targetPlayer.getPlayer();
            onlineTarget.sendMessage(ChatColor.GREEN + "Gracz " + player.getName() + " dodał cię jako zaufaną osobę do swojej działki.");
        }

        return true;
    }
} 