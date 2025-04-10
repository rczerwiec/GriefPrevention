package me.ryanhamshire.GriefPrevention.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

public class ClaimMenuListener implements Listener {
    private final GriefPrevention plugin;

    public ClaimMenuListener(GriefPrevention plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getCurrentItem() == null) return;
        if (!event.getView().getTitle().equals(ChatColor.DARK_GREEN + "Panel Działek")) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem.getItemMeta() == null) return;
        String itemName = clickedItem.getItemMeta().getDisplayName();

        if (itemName.equals(ChatColor.RED + "Usuń Działkę")) {
            handleAbandonClaim(player);
        }
        else if (itemName.equals(ChatColor.RED + "Usuń Wszystkie Działki")) {
            handleAbandonAllClaims(player);
        }
        else if (itemName.equals(ChatColor.GREEN + "Lista Twoich Działek")) {
            handleClaimsList(player);
        }
        else if (itemName.equals(ChatColor.RED + "Zamknij")) {
            player.closeInventory();
        }
    }

    private void handleAbandonClaim(Player player) {
        player.closeInventory();
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), false, null);
        
        if (claim == null) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.AbandonClaimMissing);
            return;
        }

        if (!claim.ownerID.equals(player.getUniqueId())) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.NotYourClaim);
            return;
        }

        if (claim.children.size() > 0) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.DeleteTopLevelClaim);
            return;
        }

        plugin.dataStore.deleteClaim(claim);
        
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        GriefPrevention.sendMessage(player, TextMode.Success, Messages.AbandonSuccess, String.valueOf(playerData.getRemainingClaimBlocks()));
    }

    private void handleAbandonAllClaims(Player player) {
        player.closeInventory();
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        
        if (playerData.getClaims().size() == 0) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.YouHaveNoClaims);
            return;
        }

        // Usuń wszystkie działki gracza
        for (Claim claim : playerData.getClaims()) {
            plugin.dataStore.deleteClaim(claim);
        }

        GriefPrevention.sendMessage(player, TextMode.Success, Messages.SuccessfulAbandon, 
            String.valueOf(playerData.getRemainingClaimBlocks()));
    }

    private void handleClaimsList(Player player) {
        player.closeInventory();
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());
        
        if (playerData.getClaims().size() == 0) {
            GriefPrevention.sendMessage(player, TextMode.Err, Messages.YouHaveNoClaims);
            return;
        }

        GriefPrevention.sendMessage(player, TextMode.Info, Messages.ClaimsListHeader);
        
        for (Claim claim : playerData.getClaims()) {
            String message = ChatColor.YELLOW + "Działka (ID: " + claim.getID() + ") " +
                           ChatColor.WHITE + "na koordynatach " +
                           ChatColor.GRAY + "X: " + claim.getLesserBoundaryCorner().getBlockX() +
                           " Z: " + claim.getLesserBoundaryCorner().getBlockZ() +
                           ChatColor.WHITE + " o wielkości " +
                           ChatColor.AQUA + claim.getArea() + " bloków";
            player.sendMessage(message);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Możesz dodać tutaj dodatkową logikę przy zamykaniu menu
    }
} 