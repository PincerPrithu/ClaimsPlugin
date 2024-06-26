package com.pincerdevelopment.claimsplugin.listener;

import com.pincerdevelopment.claimsplugin.ClaimsPlugin;
import com.pincerdevelopment.claimsplugin.object.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class RegionActionListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Region region = getRegion(location);
        if (region != null && !region.getOwner().equals(player.getUniqueId())) {
            if (!region.isAllowBuild()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have permission to build in this region.");
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlock().getLocation();

        Region region = getRegion(location);
        if (region != null && !region.getOwner().equals(player.getUniqueId())) {
            if (!region.isAllowBreak()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have permission to break blocks in this region.");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Location location = event.getClickedBlock() != null ? event.getClickedBlock().getLocation() : player.getLocation();

        Region region = getRegion(location);
        if (region != null && !region.getOwner().equals(player.getUniqueId())) {
            if (!region.isAllowInteract()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have permission to interact in this region.");
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity entity = event.getEntity();
            Location location = entity.getLocation();

            Region region = getRegion(location);
            if (region != null && !region.getOwner().equals(player.getUniqueId()) && !region.isAllowInteract()) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You do not have permission to harm entities in this region.");
            }
        }
    }

    private Region getRegion(Location location) {
        List<Region> regions = ClaimsPlugin.getInstance().getAllRegions();
        for (Region region : regions) {
            if (region.isLocationInRegion(location)) {
                return region;
            }
        }
        return null;
    }
}
