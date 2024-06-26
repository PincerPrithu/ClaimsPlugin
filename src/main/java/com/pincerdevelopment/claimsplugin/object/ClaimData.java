package com.pincerdevelopment.claimsplugin.object;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

@Getter
public class ClaimData {
    @Setter
    private Block pos1;
    @Setter
    private Block pos2;
    private final ItemStack originalItem;
    private final String claimName;

    public ClaimData(Block pos1, ItemStack originalItem, String claimName) {
        this.pos1 = pos1;
        this.originalItem = originalItem;
        this.claimName = claimName;
    }
}
