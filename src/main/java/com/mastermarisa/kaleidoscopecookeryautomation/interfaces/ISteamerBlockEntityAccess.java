package com.mastermarisa.kaleidoscopecookeryautomation.interfaces;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface ISteamerBlockEntityAccess {
    public boolean canAddItemByArm(Level level, ArmBlockEntity arm, ItemStack stack);
    public int getEmptySlotCount(Level level);
    public void addItemByArm(Level level, ArmBlockEntity arm,ItemStack stack);
}
