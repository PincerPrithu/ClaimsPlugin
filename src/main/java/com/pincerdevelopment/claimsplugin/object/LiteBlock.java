package com.pincerdevelopment.claimsplugin.object;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

@Getter
public class LiteBlock {
    private final int x;
    private final int y;
    private final int z;
    private final String world;
    private final Region region;

    public LiteBlock(int x, int y, int z, String world, Region region) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.region = region;
    }

    public Block getBlock() {
        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }
}
