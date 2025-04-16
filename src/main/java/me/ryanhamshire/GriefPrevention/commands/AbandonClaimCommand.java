package me.ryanhamshire.GriefPrevention.commands;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AbandonClaimCommand extends CommandHandler {

    public AbandonClaimCommand(GriefPrevention plugin) {
        super(plugin, "abandonclaim");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Ta komenda może być użyta tylko przez gracza.");
            return true;
        }

        Player player = (Player) sender;
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), true, playerData.lastClaim);

        if (claim == null) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.AbandonClaimMissing);
            return true;
        }

        if (!claim.getOwnerID().equals(player.getUniqueId()) && !player.hasPermission("griefprevention.deleteclaims")) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotYourClaim);
            return true;
        }

        if (claim.children.size() > 0) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.DeleteTopLevelClaim);
            return true;
        }

        // Oblicz zwrot bloków
        int returnValue = (int) Math.ceil(claim.getArea() * plugin.config_claims_abandonReturnRatio);
        
        // Usuń działkę
        plugin.dataStore.deleteClaim(claim);
        
        // Dodaj bloki do konta gracza
        playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + returnValue);
        
        GriefPrevention.sendMessage(player, TextMode.Success, Messages.SuccessfulAbandon, String.valueOf(playerData.getRemainingClaimBlocks()));
        
        return true;
    }
} 