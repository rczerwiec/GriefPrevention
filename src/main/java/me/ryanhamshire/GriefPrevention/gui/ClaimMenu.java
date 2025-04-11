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
import org.bukkit.inventory.meta.SkullMeta;

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

        // Zarządzanie zaufanymi osobami
        ItemStack trustManagement = createGuiItem(Material.PLAYER_HEAD,
            ChatColor.YELLOW + "Zarządzaj Zaufanymi",
            ChatColor.GRAY + "Kliknij, aby dodać lub usunąć",
            ChatColor.GRAY + "zaufane osoby z działki");
        menu.setItem(31, trustManagement);

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

    public void openTrustMenu(Player player) {
        Inventory trustMenu = Bukkit.createInventory(null, 45, ChatColor.YELLOW + "Zarządzanie Zaufanymi");
        Claim currentClaim = plugin.dataStore.getClaimAt(player.getLocation(), false, null);

        if (currentClaim != null && currentClaim.ownerID.equals(player.getUniqueId())) {
            // Przycisk dodawania zaufanej osoby
            ItemStack addTrust = createGuiItem(Material.EMERALD,
                ChatColor.GREEN + "Dodaj Zaufaną Osobę",
                ChatColor.GRAY + "Kliknij, aby dodać nową",
                ChatColor.GRAY + "zaufaną osobę do działki");
            trustMenu.setItem(20, addTrust);

            // Przycisk usuwania zaufanej osoby
            ItemStack removeTrust = createGuiItem(Material.REDSTONE,
                ChatColor.RED + "Usuń Zaufaną Osobę",
                ChatColor.GRAY + "Kliknij, aby usunąć",
                ChatColor.GRAY + "zaufaną osobę z działki");
            trustMenu.setItem(24, removeTrust);

            // Lista zaufanych osób
            ArrayList<String> builders = new ArrayList<>();
            ArrayList<String> containers = new ArrayList<>();
            ArrayList<String> accessors = new ArrayList<>();
            ArrayList<String> managers = new ArrayList<>();
            currentClaim.getPermissions(builders, containers, accessors, managers);

            int slot = 9;
            // Dodaj managerów
            for (String manager : managers) {
                if (slot >= 36) break;
                ItemStack playerHead = createPlayerHead(manager);
                trustMenu.setItem(slot++, playerHead);
            }
            // Dodaj budowniczych
            for (String builder : builders) {
                if (slot >= 36) break;
                ItemStack playerHead = createPlayerHead(builder);
                trustMenu.setItem(slot++, playerHead);
            }
            // Dodaj osoby z dostępem do pojemników
            for (String container : containers) {
                if (slot >= 36) break;
                ItemStack playerHead = createPlayerHead(container);
                trustMenu.setItem(slot++, playerHead);
            }
            // Dodaj osoby z dostępem
            for (String accessor : accessors) {
                if (slot >= 36) break;
                ItemStack playerHead = createPlayerHead(accessor);
                trustMenu.setItem(slot++, playerHead);
            }
        }

        // Przycisk powrotu
        ItemStack backButton = createGuiItem(Material.ARROW,
            ChatColor.YELLOW + "Powrót",
            ChatColor.GRAY + "Kliknij, aby wrócić",
            ChatColor.GRAY + "do głównego menu");
        trustMenu.setItem(40, backButton);

        player.openInventory(trustMenu);
    }

    private ItemStack createPlayerHead(String playerName) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            // Sprawdzamy czy to nie jest uprawnienie
            if (playerName.startsWith("[") && playerName.endsWith("]")) {
                meta.setDisplayName(ChatColor.YELLOW + playerName);
            } else {
                // Próbujemy znaleźć dokładny nick gracza
                Player player = Bukkit.getPlayer(playerName);
                String exactPlayerName = player != null ? player.getName() : 
                    playerName.length() > 16 ? playerName.substring(0, 16) : playerName;
                
                try {
                    meta.setOwningPlayer(Bukkit.getOfflinePlayer(exactPlayerName));
                } catch (Exception e) {
                    // Jeśli nie udało się ustawić właściciela, po prostu pomijamy
                }
                meta.setDisplayName(ChatColor.YELLOW + exactPlayerName);
            }
            
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Kliknij PPM, aby usunąć");
            lore.add(ChatColor.GRAY + "tę osobę z zaufanych");
            meta.setLore(lore);
            head.setItemMeta(meta);
        }
        return head;
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