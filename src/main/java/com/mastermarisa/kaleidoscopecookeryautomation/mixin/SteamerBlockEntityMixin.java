package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ISteamer;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.SteamerRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IBlockCapabilityHandler;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.ISteamerBlockEntityAccess;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

@Mixin({SteamerBlockEntity.class})
public class SteamerBlockEntityMixin extends BaseBlockEntity implements ISteamer, WorldlyContainer, IBlockCapabilityHandler, ISteamerBlockEntityAccess {
    //region Container
    @Override public int getContainerSize() { return 8; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Unique
    private final IItemHandler fullHandler = new InvWrapper(this);

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()){
            cookingProgress[slot] = 0;
            setChanged();
            this.refresh();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        cookingProgress[slot] = 0;
        this.setChanged();
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        Optional<RecipeHolder<SteamerRecipe>> steamerRecipe = getSteamerRecipe(level, stack);
        cookingTime[slot] = steamerRecipe.get().value().getCookTick();
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
        this.refresh();
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return 1;
    }

    @Override public boolean stillValid(Player player) { return true; }

    @Override
    public void startOpen(Player player) {
        //WorldlyContainer.super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        //WorldlyContainer.super.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if(this.getBlockState().getValue(SteamerBlock.HALF)){
            if(slot > 3){
                return false;
            }
        }else {
            if(slot > 7){
                return false;
            }
        }
        if(items.get(slot).isEmpty() && !getSteamerRecipe(level, stack).isEmpty()){
            return true;
        }
        return false;
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return !this.items.get(slot).isEmpty();
    }

    @Override
    public int countItem(Item item) {
        int count = 0;
        for(ItemStack itemStack : this.items){
            if(itemStack.is(item)){
                count += itemStack.getCount();
            }
        }
        return count;
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        for(ItemStack itemStack : this.items){
            for(Item item : set){
                if(itemStack.is(item)){
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

    @Override public void clearContent() { items.clear(); setChanged(); refresh(); }

    @Unique
    private final int[] fullSlotsForFace = new int[]{0,1,2,3,4,5,6,7};

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return fullSlotsForFace;
    }

    @Override
    public boolean canPlaceItemThroughFace(int i, ItemStack itemStack, @Nullable Direction direction) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int i, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Unique
    public IItemHandler getHandler(@Nullable Direction side) {
        return fullHandler;
    }
    //endregion

    public boolean canAddItemByArm(Level level, ArmBlockEntity arm,ItemStack stack){
        if(getSteamerRecipe(level, stack).isEmpty()){
            return false;
        }
        int size = this.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
        for(int i = 0;i < size;i++){
            if(this.items.get(i).isEmpty()){
                return true;
            }
        }
        return false;
    }

    public int getEmptySlotCount(Level level){
        int ans = 0;
        int size = this.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
        for(int i = 0;i < size;i++){
            if(this.items.get(i).isEmpty()){
                ans += 1;
            }
        }
        return ans;
    }

    public void addItemByArm(Level level, ArmBlockEntity arm,ItemStack stack){
        ItemStack res = stack.copy();
        int size = this.getBlockState().getValue(SteamerBlock.HALF) ? 4 : 8;
        Optional<RecipeHolder<SteamerRecipe>> steamerRecipe = getSteamerRecipe(level, stack);
        for(int i = 0;i < size;i++){
            if(this.items.get(i).isEmpty()){
                this.items.set(i, res.split(1));
                cookingTime[i] = steamerRecipe.get().value().getCookTick();
            }
        }
        setChanged();
        refresh();
    }

    @Shadow
    private final RecipeManager.CachedCheck<SingleRecipeInput, SteamerRecipe> quickCheck;
    @Shadow
    private final NonNullList<ItemStack> items;
    @Shadow
    private final int[] cookingProgress;
    @Shadow
    private final int[] cookingTime;
    @Shadow
    private int litLevel;


    public SteamerBlockEntityMixin(BlockPos pos, BlockState state) {
        super((BlockEntityType) ModBlocks.STEAMER_BE.get(), pos, state);
        this.quickCheck = RecipeManager.createCheck(ModRecipes.STEAMER_RECIPE);
        this.items = NonNullList.withSize(8, ItemStack.EMPTY);
        this.cookingProgress = new int[8];
        this.cookingTime = new int[8];
        this.litLevel = 0;
    }

    @Shadow
    public void tick(Level level) {
    }

    @Shadow
    public void mergeItem(ItemStack stack, Level level) {
    }

    @Shadow
    public List<ItemStack> dropAsItem(Level level) {
        return null;
    }

    @Shadow
    public void updateLitLevel(Level level) {
    }

    @Shadow
    public boolean hasHeatSource(Level level) {
        return false;
    }

    @Shadow
    private void cookingTick(Level level, BlockPos pos, BlockState state, SteamerBlockEntity steamer) {
    }

    @Shadow
    private void cooldownTick(Level level, BlockPos pos, BlockState state, SteamerBlockEntity steamer) {
    }

    @Shadow
    public void makeCookingParticles(Level level, BlockPos pos) {
    }

    @Shadow
    public void makeRipeParticles(Level level, BlockPos pos) {
    }

    @Shadow
    public Optional<RecipeHolder<SteamerRecipe>> getSteamerRecipe(Level level, ItemStack stack) {
        return this.items.stream().noneMatch(ItemStack::isEmpty) ? Optional.empty() : this.quickCheck.getRecipeFor(new SingleRecipeInput(stack), level);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean placeFood(Level level, LivingEntity user, ItemStack food) {
        Optional<RecipeHolder<SteamerRecipe>> steamerRecipe = this.getSteamerRecipe(level, food);
        if (steamerRecipe.isEmpty()) {
            return false;
        } else {
            int cookTime = ((SteamerRecipe)((RecipeHolder)steamerRecipe.get()).value()).getCookTick();

            if (cookTime <= 0) {
                return false;
            } else {
                boolean half = (Boolean)this.getBlockState().getValue(SteamerBlock.HALF);
                int endIndex = half ? 4 : 8;
                for(int i = 0; i < endIndex; ++i) {
                    ItemStack itemstack = (ItemStack)this.items.get(i);
                    if (itemstack.isEmpty()) {
                        this.cookingTime[i] = cookTime;
                        this.cookingProgress[i] = 0;
                        this.items.set(i, food.split(1));
                    }
                }

                this.refresh();
                return true;
            }
        }
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public boolean takeFood(Level level, LivingEntity user) {
        BlockState blockState = this.getBlockState();
        boolean isAllEmpty = true;
        boolean half = (Boolean)blockState.getValue(SteamerBlock.HALF);
        int startIndex = half ? 4 : 8;

        for(int i = startIndex - 1; i >= 0; --i) {
            ItemStack stack = (ItemStack)this.items.get(i);
            if (!stack.isEmpty()) {
                isAllEmpty = false;
                ItemUtils.getItemToLivingEntity(user, stack);
                this.items.set(i, ItemStack.EMPTY);
                this.cookingTime[i] = 0;
                this.cookingProgress[i] = 0;
            }
        }

        if (isAllEmpty && level.isEmptyBlock(this.getBlockPos().above()) && !blockState.getValue(SteamerBlock.HAS_LID)) {
            int var10000;
            if (user instanceof Player) {
                Player player = (Player)user;
                var10000 = player.getInventory().selected;
            } else {
                var10000 = -1;
            }

            int preferredSlot = var10000;
            ItemUtils.getItemToLivingEntity(user, ((Item)ModItems.STEAMER.get()).getDefaultInstance(), preferredSlot);

            for(int i = 4; i < 8; ++i) {
                this.items.set(i, ItemStack.EMPTY);
                this.cookingTime[i] = 0;
                this.cookingProgress[i] = 0;
            }

            level.playSound((Player)null, this.getBlockPos(), blockState.getSoundType().getBreakSound(), SoundSource.BLOCKS);
            if (half) {
                level.setBlockAndUpdate(this.getBlockPos(), Blocks.AIR.defaultBlockState());
            } else {
                this.setChanged();
                level.setBlockAndUpdate(this.getBlockPos(), (BlockState)blockState.setValue(SteamerBlock.HALF, true));
            }
        } else {
            this.refresh();
        }

        return true;
    }

    @Shadow
    public NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Shadow
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Shadow
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
    }

    @Shadow
    public int[] getCookingProgress() {
        return this.cookingProgress;
    }

    @Shadow
    public int[] getCookingTime() {
        return this.cookingTime;
    }
}
