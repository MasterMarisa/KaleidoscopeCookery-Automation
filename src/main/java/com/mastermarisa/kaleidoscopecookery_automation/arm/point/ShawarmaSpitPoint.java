package com.mastermarisa.kaleidoscopecookery_automation.arm.point;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CampfireCookingRecipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ShawarmaSpitPoint extends ArmInteractionPoint {
    public static final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);

    public ShawarmaSpitPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 1;
    }

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if(blockEntity instanceof ShawarmaSpitBlockEntity spit){
            if (spit.cookingItem.isEmpty() && spit.cookedItem.isEmpty()) {
                SingleRecipeInput singleRecipeInput = new SingleRecipeInput(stack);
                if (quickCheck.getRecipeFor(singleRecipeInput,level).isPresent()) {
                    ItemStack remainder = stack.copy();
                    ItemStack toInsert = remainder.split(8);
                    if (!simulate) {
                        spit.onPutCookingItem(level,toInsert);
                    }
                    return remainder;
                }
            }
        }

        return stack;
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if(blockEntity instanceof ShawarmaSpitBlockEntity spit) {
            if (spit.cookTime <= 0 && !spit.cookedItem.isEmpty()) {
                ItemStack remainder = spit.cookedItem.copy();
                if (!simulate) {
                    spit.cookingItem = ItemStack.EMPTY;
                    spit.cookedItem = ItemStack.EMPTY;
                    spit.cookTime = 0;
                    spit.refresh();
                }
                return remainder;
            }
        }
        return ItemStack.EMPTY;
    }
}
