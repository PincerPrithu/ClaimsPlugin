package com.pincerdevelopment.claimsplugin.commands;

import com.j256.ormlite.dao.Dao;
import com.pincerdevelopment.claimsplugin.ClaimsPlugin;
import com.pincerdevelopment.claimsplugin.object.Region;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UnclaimLandCommand implements CommandExecutor {

    private static final long CONFIRMATION_TIMEOUT = 15 * 1000; // 15 seconds
    private final Map<UUID, Long> confirmationMap = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        UUID playerUUID = player.getUniqueId();

        long currentTime = System.currentTimeMillis();

        if (confirmationMap.containsKey(playerUUID) && (currentTime - confirmationMap.get(playerUUID) <= CONFIRMATION_TIMEOUT)) {
            // Confirm unclaim
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        Dao<Region, UUID> regionDao = ClaimsPlugin.getInstance().getRegionDao();
                        Region region = regionDao.queryForId(playerUUID);

                        if (region == null) {
                            player.sendMessage(ChatColor.RED + "You do not have any claimed land to unclaim.");
                            confirmationMap.remove(playerUUID);
                            return;
                        }

                        regionDao.delete(region);
                        player.sendMessage(ChatColor.GREEN + "Region unclaimed successfully.");
                        confirmationMap.remove(playerUUID);
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(ChatColor.RED + "An error occurred while unclaiming the region.");
                        confirmationMap.remove(playerUUID);
                    }
                }
            }.runTaskAsynchronously(ClaimsPlugin.getInstance());
        } else {
            // Initiate unclaim
            player.sendMessage(ChatColor.RED + "This is a permanent action. Once you unclaim the land, it may be claimed by other players. Run the command again within 15 seconds to confirm you want to unclaim.");
            confirmationMap.put(playerUUID, currentTime);
        }

        return true;
    }
}
