package com.pincerdevelopment.claimsplugin.menu;

import com.pincerdevelopment.claimsplugin.ClaimsPlugin;
import com.pincerdevelopment.claimsplugin.object.Region;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.UUID;

public class RegionSettingsGUI implements Listener {

    private final Inventory inv;
    private final UUID playerUUID;

    public RegionSettingsGUI(UUID playerUUID) {
        this.inv = Bukkit.createInventory(null, 27, ChatColor.GOLD + "Region Settings");
        this.playerUUID = playerUUID;

        initializeItems();
    }

    public void initializeItems() {
        inv.setItem(10, createGuiItem(Material.DIAMOND_PICKAXE, ChatColor.GREEN + "Allow Build"));
        inv.setItem(12, createGuiItem(Material.BARRIER, ChatColor.RED + "Deny Build"));
        inv.setItem(14, createGuiItem(Material.IRON_PICKAXE, ChatColor.GREEN + "Allow Break"));
        inv.setItem(16, createGuiItem(Material.BARRIER, ChatColor.RED + "Deny Break"));
        inv.setItem(22, createGuiItem(Material.LEVER, ChatColor.GREEN + "Allow Interact"));
        inv.setItem(24, createGuiItem(Material.BARRIER, ChatColor.RED + "Deny Interact"));
    }

    protected ItemStack createGuiItem(Material material, String name) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    public void openInventory(Player player) {
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inv)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        Region region = ClaimsPlugin.getInstance().getClaim(playerUUID);
        if (region == null) {
            player.sendMessage(ChatColor.RED + "No region found.");
            return;
        }

        String displayName = clickedItem.getItemMeta().getDisplayName();
        if (displayName.equals(ChatColor.GREEN + "Allow Build")) {
            region.setAllowBuild(true);
            player.sendMessage(ChatColor.GREEN + "Building allowed in your region.");
        } else if (displayName.equals(ChatColor.RED + "Deny Build")) {
            region.setAllowBuild(false);
            player.sendMessage(ChatColor.RED + "Building denied in your region.");
        } else if (displayName.equals(ChatColor.GREEN + "Allow Break")) {
            region.setAllowBreak(true);
            player.sendMessage(ChatColor.GREEN + "Breaking allowed in your region.");
        } else if (displayName.equals(ChatColor.RED + "Deny Break")) {
            region.setAllowBreak(false);
            player.sendMessage(ChatColor.RED + "Breaking denied in your region.");
        } else if (displayName.equals(ChatColor.GREEN + "Allow Interact")) {
            region.setAllowInteract(true);
            player.sendMessage(ChatColor.GREEN + "Interacting allowed in your region.");
        } else if (displayName.equals(ChatColor.RED + "Deny Interact")) {
            region.setAllowInteract(false);
            player.sendMessage(ChatColor.RED + "Interacting denied in your region.");
        }

        // Save changes to the database
        try {
            ClaimsPlugin.getInstance().getRegionDao().update(region);
        } catch (Exception e) {
            player.sendMessage(ChatColor.RED + "An error occurred while saving the region settings.");
            ClaimsPlugin.getInstance().getLogger().severe("An error occurred while saving the region settings: " + e.getMessage());
        }
    }
}
