package com.pincerdevelopment.claimsplugin;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.pincerdevelopment.claimsplugin.command.ClaimLandCommand;
import com.pincerdevelopment.claimsplugin.commands.UnclaimLandCommand;
import com.pincerdevelopment.claimsplugin.listener.RegionActionListener;
import com.pincerdevelopment.claimsplugin.menu.RegionSettingsGUI;
import com.pincerdevelopment.claimsplugin.object.Region;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.UUID;

public class ClaimsPlugin extends JavaPlugin {

    private ConnectionSource connectionSource;
    @Getter
    private Dao<Region, UUID> regionDao;
    @Getter
    private static ClaimsPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        // Register commands and other initialization logic here
        getCommand("claim").setExecutor(new ClaimLandCommand());
        getCommand("unclaim").setExecutor(new UnclaimLandCommand());

        getServer().getPluginManager().registerEvents(new RegionSettingsGUI(), this);
        getServer().getPluginManager().registerEvents(new RegionActionListener(), this);

        saveDefaultConfig();

        try {
            String databaseType = getConfig().getString("database.type");
            String databaseUrl;

            if ("mysql".equalsIgnoreCase(databaseType)) {
                String host = getConfig().getString("database.host");
                int port = getConfig().getInt("database.port");
                String database = getConfig().getString("database.name");
                String username = getConfig().getString("database.username");
                String password = getConfig().getString("database.password");

                databaseUrl = "jdbc:mysql://" + host + ":" + port + "/" + database;
                connectionSource = new JdbcConnectionSource(databaseUrl, username, password);
            } else {
                // Default to SQLite
                databaseUrl = "jdbc:sqlite:" + getDataFolder() + "/claims.db";
                connectionSource = new JdbcConnectionSource(databaseUrl);
            }

            // Create table if it does not exist
            TableUtils.createTableIfNotExists(connectionSource, Region.class);

            // Initialize DAO
            regionDao = DaoManager.createDao(connectionSource, Region.class);

        } catch (Exception e) {
            getLogger().severe("Failed to enable plugin: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }

    }

    @Override
    public void onDisable() {
        try {
            if (connectionSource != null) {
                connectionSource.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to close database connection.", e);
        }
    }

    public List<Region> getAllRegions() {
        try {
            return regionDao.queryForAll();
        } catch (Exception e) {
            getLogger().severe("Failed to retrieve regions: " + e.getMessage());
            return null;
        }
    }

    public Region getClaim(UUID owner) {
        try {
            return regionDao.queryForId(owner);
        } catch (Exception e) {
            return null;
        }
    }
}
