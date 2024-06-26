package com.pincerdevelopment.claimsplugin.command;

import com.pincerdevelopment.claimsplugin.ClaimsPlugin;
import com.pincerdevelopment.claimsplugin.menu.RegionSettingsGUI;
import com.pincerdevelopment.claimsplugin.object.Region;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClaimLandCommand implements CommandExecutor {

    private final Map<UUID, String> claimNames = new HashMap<>();
    private final Map<UUID, Location> pos1Map = new HashMap<>();
    private final Map<UUID, Location> pos2Map = new HashMap<>();
    private final Map<UUID, Boolean> isResizing = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        if (args.length == 1) {
            String arg = args[0].toLowerCase();

            // Handle region settings
            if ("settings".equals(arg)) {
                Region existingClaim = ClaimsPlugin.getInstance().getClaim(playerUUID);
                if (existingClaim == null) {
                    player.sendMessage(ChatColor.RED + "You do not have a claimed region you can change settings for. Use /claim <name> to claim a region first.");
                    return true;
                }

                new RegionSettingsGUI(playerUUID);
                return true;
            }

            // Handle resizing the claim
            if ("resize".equals(arg)) {
                Region existingClaim = ClaimsPlugin.getInstance().getClaim(playerUUID);
                if (existingClaim == null) {
                    player.sendMessage(ChatColor.RED + "You do not have a claimed region to resize.");
                    return true;
                }

                claimNames.put(playerUUID, existingClaim.getName());
                isResizing.put(playerUUID, true);
                pos1Map.remove(playerUUID);
                pos2Map.remove(playerUUID);
                player.sendMessage(ChatColor.GREEN + "Starting process for resizing your region. Go to one corner of the area you wish to claim and type /claim 1");
                return true;
            }

            // Handle the initial claim command
            if (!"1".equals(arg) && !"2".equals(arg)) {
                Region existingClaim = ClaimsPlugin.getInstance().getClaim(playerUUID);
                if (existingClaim != null) {
                    player.sendMessage(ChatColor.RED + "You already have a claimed region. You must unclaim it before claiming a new region.");
                    return true;
                }

                String name = args[0];
                claimNames.put(playerUUID, name);
                pos1Map.remove(playerUUID);
                pos2Map.remove(playerUUID);
                player.sendMessage(ChatColor.GREEN + "Starting process for creating " + name + ChatColor.GREEN + " region. Go to one corner of the area you wish to claim and type /claim 1");
                return true;
            }

            // Handle setting the first position
            if ("1".equals(arg)) {
                if (!claimNames.containsKey(playerUUID)) {
                    player.sendMessage(ChatColor.RED + "You need to start the claim process first using /claim <name>.");
                    return true;
                }

                Location loc1 = player.getLocation();
                if (isLocationInAnyRegion(loc1)) {
                    player.sendMessage(ChatColor.RED + "You are standing in someone else's region. Please choose a different location.");
                    return true;
                }

                pos1Map.put(playerUUID, loc1);
                player.sendMessage(ChatColor.GREEN + "Position 1 set. Now go to the opposite corner and type /claim 2");
                return true;
            }

            // Handle setting the second position
            if ("2".equals(arg)) {
                if (!claimNames.containsKey(playerUUID) || !pos1Map.containsKey(playerUUID)) {
                    player.sendMessage(ChatColor.RED + "You need to set the first position first using /claim 1.");
                    return true;
                }

                Location loc2 = player.getLocation();
                if (isLocationInAnyRegion(loc2)) {
                    player.sendMessage(ChatColor.RED + "You are standing in someone else's region. Please choose a different location.");
                    return true;
                }

                pos2Map.put(playerUUID, loc2);

                Location pos1 = pos1Map.get(playerUUID);

                if (Region.isAreaOverlapping(ClaimsPlugin.getInstance().getAllRegions(),
                        pos1.getBlockX(), pos1.getBlockZ(),
                        loc2.getBlockX(), loc2.getBlockZ(),
                        pos1.getWorld().getName())) {
                    player.sendMessage(ChatColor.RED + "This area overlaps with an existing region. Please select a different area.");
                    return true;
                }

                if (isResizing.containsKey(playerUUID) && isResizing.get(playerUUID)) {
                    resizeRegion(claimNames.get(playerUUID), pos1, loc2, player);
                } else {
                    createRegion(claimNames.get(playerUUID), pos1, loc2, player);
                }

                claimNames.remove(playerUUID);
                pos1Map.remove(playerUUID);
                pos2Map.remove(playerUUID);
                isResizing.remove(playerUUID);
                return true;
            }
        }

        player.sendMessage(ChatColor.RED + "Usage: /claim <name> to start the claim process, /claim 1 to set the first position, /claim 2 to set the second position, or /claim settings to manage your region settings.");
        return true;
    }

    private boolean isLocationInAnyRegion(Location location) {
        List<Region> allRegions = ClaimsPlugin.getInstance().getAllRegions();
        if (allRegions == null) {
            return false;
        }

        for (Region region : allRegions) {
            if (region.isLocationInRegion(location)) {
                return true;
            }
        }
        return false;
    }

    private void createRegion(String name, Location pos1, Location pos2, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    UUID owner = player.getUniqueId();
                    String world = pos1.getWorld().getName();

                    int x1 = pos1.getBlockX();
                    int z1 = pos1.getBlockZ();
                    int x2 = pos2.getBlockX();
                    int z2 = pos2.getBlockZ();

                    Region newRegion = new Region(name, world, x1, z1, x2, z2, owner);
                    ClaimsPlugin.getInstance().getRegionDao().create(newRegion);

                    player.sendMessage(ChatColor.GREEN + "Region claimed successfully! Now configure your region's settings using /claim settings");

                    } catch (Exception e) {
                    ClaimsPlugin.getInstance().getLogger().severe("An error occurred while creating region: " + e.getMessage());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "An error occurred while creating the region.");
                        }
                    }.runTask(ClaimsPlugin.getInstance());
                }
            }
        }.runTaskAsynchronously(ClaimsPlugin.getInstance());
    }

    private void resizeRegion(String name, Location pos1, Location pos2, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    Region existingRegion = ClaimsPlugin.getInstance().getClaim(player.getUniqueId());
                    if (existingRegion == null) {
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.sendMessage(ChatColor.RED + "You do not have a claimed region to resize.");
                            }
                        }.runTask(ClaimsPlugin.getInstance());
                        return;
                    }

                    int x1 = pos1.getBlockX();
                    int z1 = pos1.getBlockZ();
                    int x2 = pos2.getBlockX();
                    int z2 = pos2.getBlockZ();

                    existingRegion.setX1(x1);
                    existingRegion.setZ1(z1);
                    existingRegion.setX2(x2);
                    existingRegion.setZ2(z2);

                    ClaimsPlugin.getInstance().getRegionDao().update(existingRegion);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.GREEN + "Region resized successfully! Now configure your region's settings using /claim settings");
                        }
                    }.runTask(ClaimsPlugin.getInstance());
                } catch (Exception e) {
                    ClaimsPlugin.getInstance().getLogger().severe("An error occurred while resizing region: " + e.getMessage());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "An error occurred while resizing the region.");
                        }
                    }.runTask(ClaimsPlugin.getInstance());
                }
            }
        }.runTaskAsynchronously(ClaimsPlugin.getInstance());
    }
}
