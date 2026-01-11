package com.mastermarisa.kaleidoscopecookeryautomation.interfaces;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IPotBlockEntityAccess {
    public void setArmRecipe(Level level, LivingEntity livingEntity, ItemStack itemStack);
    public boolean takeArmRecipe(Level level,LivingEntity livingEntity,ItemStack itemStack);
    public ItemStack getStoredRecipeItem();
    public ItemStack getPlaceOilReturn(ItemStack stack);
    public boolean canPlaceOil(Level level,ItemStack stack);
    public void placeOilByArm(Level level, ArmBlockEntity arm, ItemStack stack);
    public boolean canAddIngredient(Level level, ArmBlockEntity arm, ItemStack itemStack);
    public void addIngredientByArm(Level level, ArmBlockEntity arm, ItemStack itemStack);
    public boolean inputMatchRecipe(Level level);
    public void onShovelHitByArm(Level level, ArmBlockEntity arm, ItemStack shovel);
    public int getStirFryCount();
    public boolean canTakeOutWithoutCarrier(Level level);
    public boolean canTakeOutWithCarrier(Level level, ItemStack mainHandItem);
    public ItemStack getResultFood(Level level);
    public long getLastStirByArmTime();
}
