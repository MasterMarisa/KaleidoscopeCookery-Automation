package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IBlockCapabilityHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Set;
import java.util.function.Predicate;

@Mixin({FruitBasketBlockEntity.class})
public class FruitBasketBlockEntityMixin extends BaseBlockEntity implements WorldlyContainer, IBlockCapabilityHandler {
    @Unique
    private final IItemHandler fullHandler = new InvWrapper(this);
    @Unique
    private final int[] fullSlots = new int[]{0,1,2,3,4,5,6,7};
    @Shadow
    private final ItemStackHandler items = new ItemStackHandler(8);

    public FruitBasketBlockEntityMixin(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType) ModBlocks.FRUIT_BASKET_BE.get(), pPos, pBlockState);
    }

    @Shadow
    public void putOn(ItemStack stack) {
    }

    @Shadow
    public void takeOut(Player player) {
    }

    @Shadow
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Shadow
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Shadow
    public ItemStackHandler getItems() {
        return this.items;
    }

    @Shadow
    public void setItems(ItemStackHandler items, RegistryAccess access) {
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return fullSlots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return 8;
    }

    @Override
    public boolean isEmpty() {
        for(int i = 0;i < 8;i++){
            if(!this.items.getStackInSlot(i).isEmpty()){
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = this.items.extractItem(slot,amount,false);
        this.setChanged();
        this.refresh();
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack stack = this.items.extractItem(slot,64,false);
        this.setChanged();
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        if(this.items.getStackInSlot(slot).isEmpty()){
            this.items.setStackInSlot(slot,itemStack);
            this.setChanged();
            this.refresh();
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Math.min(64,stack.getMaxStackSize());
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(Player player) {
        WorldlyContainer.super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        WorldlyContainer.super.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return !stack.is(ModItems.FRUIT_BASKET.get()) && (this.items.getStackInSlot(slot).isEmpty() || ItemStack.isSameItemSameComponents(this.items.getStackInSlot(slot),stack));
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return !this.items.getStackInSlot(slot).isEmpty();
    }

    @Override
    public int countItem(Item item) {
        int count = 0;
        for(int i = 0;i < 8;i++){
            ItemStack stack = this.items.getStackInSlot(i);
            if(stack.is(item)){
                count += stack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        for(int i = 0;i < 8;i++){
            ItemStack stack = this.items.getStackInSlot(i);
            for(Item item : set){
                if(stack.is(item)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        return WorldlyContainer.super.hasAnyMatching(predicate);
    }

    @Override
    public void clearContent() {
        for(int i = 0;i < 8;i++){
            this.items.setStackInSlot(i,ItemStack.EMPTY);
        }
        this.setChanged();
        this.refresh();;
    }

    @Override
    public IItemHandler getHandler(@Nullable Direction side) {
        return this.fullHandler;
    }
}
