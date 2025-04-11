package me.ryanhamshire.GriefPrevention.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.Messages;
import me.ryanhamshire.GriefPrevention.PlayerData;
import me.ryanhamshire.GriefPrevention.TextMode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimMenuListener implements Listener {
    private final GriefPrevention plugin;
    private final ClaimMenu claimMenu;
    private final Map<UUID, MenuAction> pendingActions;

    private enum MenuAction {
        ADD_TRUST,
        REMOVE_TRUST
    }

    public ClaimMenuListener(GriefPrevention plugin) {
        this.plugin = plugin;
        this.claimMenu = new ClaimMenu(plugin);
        this.pendingActions = new HashMap<>();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (event.getCurrentItem() == null) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        String title = event.getView().getTitle();

        if (title.equals(ChatColor.DARK_GREEN + "Panel Działek")) {
            event.setCancelled(true);
            handleMainMenuClick(player, clickedItem);
        } else if (title.equals(ChatColor.YELLOW + "Zarządzanie Zaufanymi")) {
            event.setCancelled(true);
            handleTrustMenuClick(player, clickedItem, event.getClick().isRightClick());
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
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
        else if (itemName.equals(ChatColor.YELLOW + "Zarządzaj Zaufanymi")) {
            player.closeInventory();
            claimMenu.openTrustMenu(player);
        }
        else if (itemName.equals(ChatColor.RED + "Zamknij")) {
            player.closeInventory();
        }
    }

    private void handleTrustMenuClick(Player player, ItemStack clickedItem, boolean isRightClick) {
        if (clickedItem.getItemMeta() == null) return;
        String itemName = clickedItem.getItemMeta().getDisplayName();

        if (itemName.equals(ChatColor.GREEN + "Dodaj Zaufaną Osobę")) {
            player.closeInventory();
            pendingActions.put(player.getUniqueId(), MenuAction.ADD_TRUST);
            player.sendMessage(ChatColor.YELLOW + "Wpisz na czacie nick gracza, którego chcesz dodać jako zaufanego.");
            player.sendMessage(ChatColor.GRAY + "Wpisz 'anuluj' aby anulować.");
        }
        else if (itemName.equals(ChatColor.RED + "Usuń Zaufaną Osobę")) {
            player.closeInventory();
            pendingActions.put(player.getUniqueId(), MenuAction.REMOVE_TRUST);
            player.sendMessage(ChatColor.YELLOW + "Wpisz na czacie nick gracza, którego chcesz usunąć z zaufanych.");
            player.sendMessage(ChatColor.GRAY + "Wpisz 'anuluj' aby anulować.");
        }
        else if (itemName.equals(ChatColor.YELLOW + "Powrót")) {
            player.closeInventory();
            claimMenu.openMenu(player);
        }
        else if (clickedItem.getType() == Material.PLAYER_HEAD && isRightClick) {
            // Usuwanie zaufanej osoby poprzez kliknięcie PPM na głowę
            String trustedPlayer = ChatColor.stripColor(itemName);
            handleTrustRemoval(player, trustedPlayer);
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!pendingActions.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
        String message = event.getMessage();

        if (message.equalsIgnoreCase("anuluj")) {
            pendingActions.remove(player.getUniqueId());
            player.sendMessage(ChatColor.YELLOW + "Operacja anulowana.");
            Bukkit.getScheduler().runTask(plugin, () -> claimMenu.openTrustMenu(player));
            return;
        }

        MenuAction action = pendingActions.get(player.getUniqueId());
        pendingActions.remove(player.getUniqueId());

        switch (action) {
            case ADD_TRUST:
                handleTrustAddition(player, message);
                break;
            case REMOVE_TRUST:
                handleTrustRemoval(player, message);
                break;
        }
    }

    private void handleTrustAddition(Player player, String targetName) {
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), false, null);
        if (claim == null || !claim.ownerID.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Musisz stać na swojej działce aby dodać zaufaną osobę.");
            return;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if (targetPlayer == null) {
            player.sendMessage(ChatColor.RED + "Nie znaleziono gracza o nicku " + targetName);
            return;
        }

        // Używamy dokładnego nicku gracza z zachowaniem wielkości liter
        String exactPlayerName = targetPlayer.getName();
        
        // Najpierw usuwamy wszystkie istniejące uprawnienia
        claim.dropPermission(exactPlayerName);
        
        // Nadajemy uprawnienia bezpośrednio
        claim.setPermission(exactPlayerName, ClaimPermission.Build);
        
        // Dodajemy do managerów
        if (!claim.managers.contains(exactPlayerName)) {
            claim.managers.add(exactPlayerName);
        }
        
        // Zapisujemy zmiany w działce
        plugin.dataStore.saveClaim(claim);
        
        // Używamy systemowych komend do pełnego nadania uprawnień
        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.dispatchCommand(player, "trust " + exactPlayerName);
            Bukkit.dispatchCommand(player, "permissiontrust " + exactPlayerName);
        });
        
        player.sendMessage(ChatColor.GREEN + "Dodano gracza " + exactPlayerName + " do zaufanych osób z pełnymi uprawnieniami.");
        
        // Otwieramy menu ponownie po krótkim opóźnieniu
        Bukkit.getScheduler().runTaskLater(plugin, () -> claimMenu.openTrustMenu(player), 5L);
    }

    private void handleTrustRemoval(Player player, String targetName) {
        Claim claim = plugin.dataStore.getClaimAt(player.getLocation(), false, null);
        if (claim == null || !claim.ownerID.equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "Musisz stać na swojej działce aby usunąć zaufaną osobę.");
            return;
        }

        // Próbujemy znaleźć dokładny nick gracza
        Player targetPlayer = Bukkit.getPlayer(targetName);
        String exactPlayerName = targetPlayer != null ? targetPlayer.getName() : targetName;
        
        // Usuwamy wszystkie uprawnienia
        claim.dropPermission(exactPlayerName);
        
        // Zapisujemy zmiany w działce
        plugin.dataStore.saveClaim(claim);
        
        player.sendMessage(ChatColor.GREEN + "Usunięto gracza " + exactPlayerName + " z zaufanych osób.");
        
        Bukkit.getScheduler().runTask(plugin, () -> claimMenu.openTrustMenu(player));
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