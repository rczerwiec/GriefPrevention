package me.ryanhamshire.GriefPrevention.gui;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.ryanhamshire.GriefPrevention.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ClaimMenu {
    private final GriefPrevention plugin;
    private static final String MENU_TITLE = ChatColor.DARK_GREEN + "Panel Działek";
    private static final int INVENTORY_SIZE = 45; // Wielokrotność 9, max 54

    public ClaimMenu(GriefPrevention plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, INVENTORY_SIZE, MENU_TITLE);
        PlayerData playerData = plugin.dataStore.getPlayerData(player.getUniqueId());

        // Informacje o blokach
        ItemStack blocksInfo = createInfoItem(Material.GRASS_BLOCK, 
            ChatColor.GOLD + "Twoje Bloki",
            playerData.getRemainingClaimBlocks(),
            playerData.getAccruedClaimBlocks(),
            playerData.getBonusClaimBlocks());
        menu.setItem(4, blocksInfo);

        // Przycisk usunięcia działki
        ItemStack abandonClaim = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Usuń Działkę",
            ChatColor.GRAY + "Kliknij, aby usunąć działkę,",
            ChatColor.GRAY + "na której obecnie stoisz");
        menu.setItem(19, abandonClaim);

        // Przycisk usunięcia wszystkich działek
        ItemStack abandonAllClaims = createGuiItem(Material.TNT,
            ChatColor.RED + "Usuń Wszystkie Działki",
            ChatColor.GRAY + "Kliknij, aby usunąć wszystkie",
            ChatColor.GRAY + "swoje działki",
            ChatColor.RED + "UWAGA: Tej operacji nie można cofnąć!");
        menu.setItem(21, abandonAllClaims);

        // Lista działek
        ItemStack claimsList = createGuiItem(Material.MAP,
            ChatColor.GREEN + "Lista Twoich Działek",
            ChatColor.GRAY + "Kliknij, aby zobaczyć listę",
            ChatColor.GRAY + "wszystkich swoich działek");
        menu.setItem(23, claimsList);

        // Informacja o aktualnej działce
        Claim currentClaim = plugin.dataStore.getClaimAt(player.getLocation(), false, null);
        if (currentClaim != null && currentClaim.ownerID != null && currentClaim.ownerID.equals(player.getUniqueId())) {
            ItemStack currentClaimInfo = createGuiItem(Material.DIAMOND,
                ChatColor.AQUA + "Aktualna Działka",
                ChatColor.GRAY + "Wymiary: " + currentClaim.getWidth() + "x" + currentClaim.getHeight(),
                ChatColor.GRAY + "Powierzchnia: " + currentClaim.getArea() + " bloków");
            menu.setItem(25, currentClaimInfo);
        }

        // Przycisk zamknięcia
        ItemStack closeButton = createGuiItem(Material.BARRIER,
            ChatColor.RED + "Zamknij",
            ChatColor.GRAY + "Kliknij, aby zamknąć menu");
        menu.setItem(40, closeButton);

        player.openInventory(menu);
    }

    private ItemStack createInfoItem(Material material, String name, int remaining, int accrued, int bonus) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Pozostałe bloki: " + ChatColor.WHITE + remaining);
            lore.add(ChatColor.GRAY + "Zebrane bloki: " + ChatColor.WHITE + accrued);
            lore.add(ChatColor.GRAY + "Bonusowe bloki: " + ChatColor.WHITE + bonus);
            lore.add(ChatColor.GRAY + "Łącznie: " + ChatColor.WHITE + (accrued + bonus));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> loreList = new ArrayList<>();
            for (String line : lore) {
                loreList.add(line);
            }
            meta.setLore(loreList);
            item.setItemMeta(meta);
        }
        return item;
    }
} 