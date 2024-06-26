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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegionSettingsGUI implements Listener {

    private static final String INVENTORY_TITLE = ChatColor.GOLD + "Region Settings";
    private final Inventory inv;
    private final UUID playerUUID;
    private Region region;

    public RegionSettingsGUI() {
        this.playerUUID = null;
        this.region = null;
        this.inv = null;
    }

    public RegionSettingsGUI(UUID playerUUID) {
        this.playerUUID = playerUUID;
        this.inv = Bukkit.createInventory(new CustomInventoryHolder(playerUUID), 27, INVENTORY_TITLE);

        // Fetch region data asynchronously
        new BukkitRunnable() {
            @Override
            public void run() {
                region = ClaimsPlugin.getInstance().getClaim(playerUUID);

                if (region == null) {
                    Bukkit.getLogger().severe("Region is null for player UUID: " + playerUUID);
                    return;
                }

                // Update the GUI items on the main thread
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        initializeItems();
                        Player player = Bukkit.getPlayer(playerUUID);
                        if (player != null && player.isOnline()) {
                            player.openInventory(inv);
                        }
                    }
                }.runTask(ClaimsPlugin.getInstance());
            }
        }.runTaskAsynchronously(ClaimsPlugin.getInstance());
    }

    public void initializeItems() {
        inv.setItem(10, createGuiItem(Material.PAPER, ChatColor.GOLD + "Info",
                ChatColor.GRAY + "These settings allow you to configure",
                ChatColor.GRAY + "what others can do in your region."));

        inv.setItem(12, createGuiItem(
                region.isAllowBuild() ? Material.GRASS_BLOCK : Material.BARRIER,
                region.isAllowBuild() ? ChatColor.GREEN + "Building is allowed! " + ChatColor.GRAY + "(Click to disallow)" : ChatColor.RED + "Building is not allowed! " + ChatColor.GRAY + "(Click to allow)"
        ));
        inv.setItem(13, createGuiItem(
                region.isAllowBreak() ? Material.IRON_PICKAXE : Material.BARRIER,
                region.isAllowBreak() ? ChatColor.GREEN + "Breaking is allowed! " + ChatColor.GRAY + "(Click to disallow)" : ChatColor.RED + "Breaking is not allowed! " + ChatColor.GRAY + "(Click to allow)"
        ));
        inv.setItem(14, createGuiItem(
                region.isAllowInteract() ? Material.LEVER : Material.BARRIER,
                region.isAllowInteract() ? ChatColor.GREEN + "Interacting is allowed! " + ChatColor.GRAY + "(Click to disallow)" : ChatColor.RED + "Interacting is not allowed! " + ChatColor.GRAY + "(Click to allow)"
        ));
        inv.setItem(25, createGuiItem(Material.RED_DYE, ChatColor.RED + "Delete Claim"));
        inv.setItem(26, createGuiItem(Material.ENDER_EYE, ChatColor.YELLOW + "Resize/Move Region"));
    }

    protected ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        assert meta != null;
        meta.setDisplayName(name);
        List<String> loreList = new ArrayList<>();
        for (String line : lore) {
            loreList.add(line);
        }
        meta.setLore(loreList);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof CustomInventoryHolder)) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = ((CustomInventoryHolder) event.getInventory().getHolder()).getPlayerUUID();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;
        String displayName = clickedItem.getItemMeta().getDisplayName();

        new BukkitRunnable() {
            @Override
            public void run() {
                Region eventRegion = ClaimsPlugin.getInstance().getClaim(playerUUID);

                if (eventRegion == null) {
                    Bukkit.getLogger().severe("Event region is null for player UUID: " + playerUUID);
                    return;
                }

                if (displayName.equals(ChatColor.GREEN + "Building is allowed! " + ChatColor.GRAY + "(Click to disallow)") ||
                        displayName.equals(ChatColor.RED + "Building is not allowed! " + ChatColor.GRAY + "(Click to allow)")) {
                    eventRegion.setAllowBuild(!eventRegion.isAllowBuild());
                    player.sendMessage(ChatColor.GREEN + "Building " + (eventRegion.isAllowBuild() ? "allowed" : "denied") + " in your region.");
                } else if (displayName.equals(ChatColor.GREEN + "Breaking is allowed! " + ChatColor.GRAY + "(Click to disallow)") ||
                        displayName.equals(ChatColor.RED + "Breaking is not allowed! " + ChatColor.GRAY + "(Click to allow)")) {
                    eventRegion.setAllowBreak(!eventRegion.isAllowBreak());
                    player.sendMessage(ChatColor.GREEN + "Breaking " + (eventRegion.isAllowBreak() ? "allowed" : "denied") + " in your region.");
                } else if (displayName.equals(ChatColor.GREEN + "Interacting is allowed! " + ChatColor.GRAY + "(Click to disallow)") ||
                        displayName.equals(ChatColor.RED + "Interacting is not allowed! " + ChatColor.GRAY + "(Click to allow)")) {
                    eventRegion.setAllowInteract(!eventRegion.isAllowInteract());
                    player.sendMessage(ChatColor.GREEN + "Interacting " + (eventRegion.isAllowInteract() ? "allowed" : "denied") + " in your region.");
                } else if (displayName.equals(ChatColor.YELLOW + "Resize/Move Region")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.closeInventory();
                            player.performCommand("claim resize");
                            return;
                        }
                    }.runTask(ClaimsPlugin.getInstance());
                    return;
                } else if (displayName.equals(ChatColor.RED + "Delete Claim")) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.closeInventory();
                            player.sendMessage(ChatColor.RED + "Use /unclaim to delete your claim.");
                        }
                    }.runTask(ClaimsPlugin.getInstance());
                    return;
                }

                // Save changes to the database asynchronously
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        try {
                            ClaimsPlugin.getInstance().getRegionDao().update(eventRegion);
                        } catch (Exception e) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    player.sendMessage(ChatColor.RED + "An error occurred while saving the region settings.");
                                }
                            }.runTask(ClaimsPlugin.getInstance());
                            ClaimsPlugin.getInstance().getLogger().severe("An error occurred while saving the region settings: " + e.getMessage());
                        }

                        // Refresh the GUI on the main thread
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                new RegionSettingsGUI(playerUUID);
                            }
                        }.runTask(ClaimsPlugin.getInstance());
                    }
                }.runTaskAsynchronously(ClaimsPlugin.getInstance());
            }
        }.runTaskAsynchronously(ClaimsPlugin.getInstance());
    }

    private static class CustomInventoryHolder implements InventoryHolder {
        private final UUID playerUUID;

        public CustomInventoryHolder(UUID playerUUID) {
            this.playerUUID = playerUUID;
        }

        @Override
        public Inventory getInventory() {
            return null; // Not used
        }

        public UUID getPlayerUUID() {
            return playerUUID;
        }
    }
}
