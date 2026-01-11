package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IShawarmaSpit;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IShawarmaSpitBlockEntityAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({ShawarmaSpitBlockEntity.class})
public class ShawarmaSpitBlockEntityMixin extends BaseBlockEntity implements IShawarmaSpit, IShawarmaSpitBlockEntityAccess {
    @Shadow
    private final RecipeManager.CachedCheck<SingleRecipeInput, CampfireCookingRecipe> quickCheck;
    @Shadow
    public ItemStack cookingItem;
    @Shadow
    public ItemStack cookedItem;
    @Shadow
    public int cookTime;

    public ShawarmaSpitBlockEntityMixin(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType) ModBlocks.SHAWARMA_SPIT_BE.get(), pPos, pBlockState);
        this.quickCheck = RecipeManager.createCheck(RecipeType.CAMPFIRE_COOKING);
        this.cookingItem = ItemStack.EMPTY;
        this.cookedItem = ItemStack.EMPTY;
    }

    public boolean canPutCookingItem(Level level,ItemStack itemStack) {
        if (this.cookedItem.isEmpty() && this.cookingItem.isEmpty()) {
            SingleRecipeInput singleRecipeInput = new SingleRecipeInput(itemStack);
            var opt = this.quickCheck.getRecipeFor(singleRecipeInput, level);
            if (opt.isPresent()) {
                return true;
            }
        }
        return false;
    }

    public boolean canTakeCookedItem(Level level){
        return this.cookTime <= 0 && !this.cookedItem.isEmpty();
    }

    public void takeCookedItemByArm(Level level){
        this.cookingItem = ItemStack.EMPTY;
        this.cookedItem = ItemStack.EMPTY;
        this.cookTime = 0;
        this.refresh();

        if (level instanceof ServerLevel) {
            level.playSound((Player)null, (double)this.worldPosition.getX() + (double)0.5F, (double)this.worldPosition.getY() + (double)0.5F, (double)this.worldPosition.getZ() + (double)0.5F, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.5F + level.random.nextFloat(), level.random.nextFloat() * 0.7F + 0.6F);
        }
    }

    @Shadow
    public boolean onPutCookingItem(Level level, ItemStack itemStack) {
        return false;
    }

    @Shadow
    public boolean onTakeCookedItem(Level level, LivingEntity entity) {
        return false;
    }

    @Shadow
    private void giveItem(Level level, LivingEntity entity, ItemStack mainHandItem, ItemStack copy) {
    }

    @Shadow
    public void tick() {
    }

    @Shadow
    private void spawnParticles() {
    }

    @Shadow
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Shadow
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }
}
