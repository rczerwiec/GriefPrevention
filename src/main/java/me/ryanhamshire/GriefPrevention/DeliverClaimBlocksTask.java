/*
    GriefPrevention Server Plugin for Minecraft
    Copyright (C) 2012 Ryan Hamshire

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ryanhamshire.GriefPrevention;

import me.ryanhamshire.GriefPrevention.events.AccrueClaimBlocksEvent;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

//FEATURE: give players claim blocks for playing
//runs every 5 minutes in the main thread, grants blocks per hour / 12 to each online player
class DeliverClaimBlocksTask implements Runnable
{
    private static final int MINUTES_BETWEEN_DELIVERY = 5;
    private Player player;
    private final GriefPrevention instance;

    DeliverClaimBlocksTask(Player player)
    {
        this.player = player;
        this.instance = GriefPrevention.instance;
    }

    @Override
    public void run()
    {
        // Sprawdzamy czy gracz jest online
        if (!player.isOnline()) {
            Bukkit.getLogger().info("[GriefPrevention] Gracz " + player.getName() + " jest offline, pomijam dodawanie bloków.");
            return;
        }

        if (!instance.claimsEnabledForWorld(player.getWorld())) {
            Bukkit.getLogger().info("[GriefPrevention] Świat " + player.getWorld().getName() + " ma wyłączone działki.");
            return;
        }

        PlayerData playerData = instance.dataStore.getPlayerData(player.getUniqueId());

        // Sprawdzamy czy gracz osiągnął limit
        int maxAccruedBlocks = playerData.getAccruedClaimBlocksLimit();
        int currentBlocks = playerData.getAccruedClaimBlocks();
        int bonusBlocks = playerData.getBonusClaimBlocks();
        int groupBonus = instance.dataStore.getGroupBonusBlocks(player.getUniqueId());
        int totalAvailable = currentBlocks + bonusBlocks + groupBonus;
        
        Bukkit.getLogger().info("[GriefPrevention] Statystyki dla " + player.getName() + ":");
        Bukkit.getLogger().info("  - Aktualne bloki: " + currentBlocks);
        Bukkit.getLogger().info("  - Bonusowe bloki: " + bonusBlocks);
        Bukkit.getLogger().info("  - Bloki z grupy: " + groupBonus);
        Bukkit.getLogger().info("  - Łącznie dostępne: " + totalAvailable);
        Bukkit.getLogger().info("  - Limit bloków: " + maxAccruedBlocks);
            
        if (currentBlocks >= maxAccruedBlocks) {
            Bukkit.getLogger().info("[GriefPrevention] Gracz " + player.getName() + " osiągnął limit bloków (" + maxAccruedBlocks + ")");
            return;
        }

        try
        {
            // Obliczamy ile bloków na 5 minut (1/12 wartości godzinowej, zaokrąglamy w górę)
            int blocksPerHour = instance.config_claims_blocksAccruedPerHour_default;
            int blocksToAdd = (int)Math.ceil(blocksPerHour / 12.0);
            
            Bukkit.getLogger().info("[GriefPrevention] Próba dodania " + blocksToAdd + " bloków dla " + player.getName() + 
                " (konfiguracja: " + blocksPerHour + " na godzinę, czyli " + blocksToAdd + " co 5 minut)");

            // Przekazujemy wartość godzinową do eventu (event sam podzieli przez 6)
            AccrueClaimBlocksEvent event = new AccrueClaimBlocksEvent(player, blocksPerHour, false);
            instance.getServer().getPluginManager().callEvent(event);
            
            if (event.isCancelled()) {
                Bukkit.getLogger().info("[GriefPrevention] Gracz " + player.getName() + " nie otrzymał bloków - anulowane przez inny plugin");
                return;
            }

            // Event zwraca nam dokładnie tyle bloków ile chcieliśmy dodać
            blocksToAdd = event.getBlocksToAccrue();
            if (blocksToAdd <= 0) {
                Bukkit.getLogger().info("[GriefPrevention] Nie dodano bloków - ilość <= 0");
                return;
            }

            // Dodajemy bloki
            playerData.accrueBlocks(blocksToAdd);
                
            int newTotal = playerData.getAccruedClaimBlocks();
            Bukkit.getLogger().info("[GriefPrevention] Dodano " + blocksToAdd + " bloków dla " + player.getName() + 
                " (obecnie ma: " + newTotal + "/" + maxAccruedBlocks + ")");
        }
        catch (Exception e)
        {
            Bukkit.getLogger().severe("[GriefPrevention] Błąd podczas dodawania bloków dla " + player.getName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
