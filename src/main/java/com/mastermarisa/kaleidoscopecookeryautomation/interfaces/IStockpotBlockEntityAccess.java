package com.mastermarisa.kaleidoscopecookeryautomation.interfaces;

import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IStockpotBlockEntityAccess {
    public boolean canAddSoupBase(ItemStack bucket);
    public void addSoupBaseByArm(Level level, ArmBlockEntity arm, ItemStack bucket);
    public boolean canAddInIngredient(ItemStack itemStack);
    public void addIngredientByArm(ArmBlockEntity arm, ItemStack itemStack);
    public void setArmRecipe(Level level, LivingEntity livingEntity, ItemStack itemStack);
    public boolean canMatchRecipe(ItemStack itemStack);
    public ItemStack getStoredRecipeItem();
    public boolean takeArmRecipe(Level level,LivingEntity livingEntity,ItemStack itemStack);
    public boolean inputMatchRecipe(Level level);
    public boolean canMatchContainer(ItemStack stack);
    public ItemStack getCookingResult();
    public ItemStack takeOutProductByArm(Level level, ArmBlockEntity arm, ItemStack stack);
}
