package com.pincerdevelopment.claimsplugin.object;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@DatabaseTable(tableName = "regions")
public class Region {

    @DatabaseField(id = true, canBeNull = false, dataType = DataType.UUID)
    private UUID owner;  // Primary key

    @DatabaseField(canBeNull = false)
    private String name;

    @DatabaseField(canBeNull = false)
    private String world;

    @DatabaseField(canBeNull = false)
    private int x1;

    @DatabaseField(canBeNull = false)
    private int z1;

    @DatabaseField(canBeNull = false)
    private int x2;

    @DatabaseField(canBeNull = false)
    private int z2;

    @DatabaseField(canBeNull = false)
    private boolean allowBuild;

    @DatabaseField(canBeNull = false)
    private boolean allowBreak;

    @DatabaseField(canBeNull = false)
    private boolean allowInteract;

    public Region() {
        // ORMLite requires a no-arg constructor
    }

    public Region(String name, String world, int x1, int z1, int x2, int z2, UUID owner) {
        this.name = name;
        this.world = world;
        this.x1 = x1;
        this.z1 = z1;
        this.x2 = x2;
        this.z2 = z2;
        this.owner = owner;
        this.allowBuild = false;
        this.allowBreak = false;
        this.allowInteract = false;
    }

    public boolean isLocationInRegion(Location location) {
        if (!this.world.equals(location.getWorld().getName())) {
            return false;
        }

        int locX = location.getBlockX();
        int locZ = location.getBlockZ();

        return locX >= x1 && locX <= x2 &&
                locZ >= z1 && locZ <= z2;
    }

    public boolean isAreaOverlapping(int x1, int z1, int x2, int z2, String world) {
        return this.world.equals(world) &&
                x1 <= this.x2 && x2 >= this.x1 &&
                z1 <= this.z2 && z2 >= this.z1;
    }

    public static boolean isAreaOverlapping(List<Region> regions, int x1, int z1, int x2, int z2, String world) {
        for (Region region : regions) {
            if (region.isAreaOverlapping(x1, z1, x2, z2, world)) {
                return true;
            }
        }
        return false;
    }
}
