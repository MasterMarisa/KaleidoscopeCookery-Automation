package com.mastermarisa.kaleidoscopecookeryautomation.interfaces;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IShawarmaSpitBlockEntityAccess {
    public boolean canPutCookingItem(Level level, ItemStack itemStack);
    public boolean canTakeCookedItem(Level level);
    public void takeCookedItemByArm(Level level);
}
