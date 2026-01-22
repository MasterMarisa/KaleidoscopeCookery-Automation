package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IStockpot;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.serializer.StockpotRecipeSerializer;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.*;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IStockpotBlockEntityAccess;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.*;

@Mixin({StockpotBlockEntity.class})
public class StockpotBlockEntityMixin extends BaseBlockEntity implements IStockpot, IStockpotBlockEntityAccess {
    @Unique
    private ItemStack storedRecipeItem;
    @Shadow
    private final RecipeManager.CachedCheck<StockpotInput, StockpotRecipe> quickCheck;
    @Shadow
    private NonNullList<ItemStack> inputs;
    @Shadow
    private ResourceLocation recipeId;
    @Shadow
    private ResourceLocation soupBaseId;
    @Shadow
    private ItemStack result;
    @Shadow
    private int status;
    @Shadow
    private int currentTick;
    @Shadow
    private int takeoutCount;
    @Shadow
    private ItemStack lidItem;
    @Shadow
    public RecipeHolder<StockpotRecipe> recipe;
    @Shadow
    @Nullable
    public Entity renderEntity;

    public StockpotBlockEntityMixin(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType) ModBlocks.STOCKPOT_BE.get(), pPos, pBlockState);
        this.quickCheck = RecipeManager.createCheck(ModRecipes.STOCKPOT_RECIPE);
        this.inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        this.recipeId = StockpotRecipeSerializer.EMPTY_ID;
        this.soupBaseId = ModSoupBases.WATER;
        this.result = ItemStack.EMPTY;
        this.status = 0;
        this.currentTick = -1;
        this.takeoutCount = 0;
        this.lidItem = ItemStack.EMPTY;
        this.recipe = StockpotRecipeSerializer.getEmptyRecipe();
        this.renderEntity = null;
        this.storedRecipeItem = ItemStack.EMPTY;
    }

    public void setArmRecipe(Level level,LivingEntity livingEntity,ItemStack itemStack){
        if(itemStack.is(ModItems.RECIPE_ITEM.get())){
            RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemStack);
            if(record != null && record.type().equals(RecipeItem.STOCKPOT)){
                storedRecipeItem = itemStack.copy();
                if(livingEntity instanceof Player player){
                    player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
                }
                setChanged();
            }
        }
    }

    public boolean takeArmRecipe(Level level,LivingEntity livingEntity,ItemStack itemStack){
        if(!itemStack.isEmpty()){
            return false;
        }
        if(livingEntity instanceof Player player){
            if(player.isSecondaryUseActive()){
                ItemUtils.getItemToLivingEntity(player,getStoredRecipeItem().copy());
                storedRecipeItem = ItemStack.EMPTY;
                setChanged();
                return true;
            }
        }
        return false;
    }

    public boolean inputMatchRecipe(Level level){
        RecipeItem.RecipeRecord storedRecipe;
        if(getStoredRecipeItem().isEmpty()){
            return false;
        }else {
            storedRecipe = RecipeItem.getRecipe(getStoredRecipeItem());
        }
        if(storedRecipe == null){
            return false;
        }
        HashMap<String, Integer> hashmap = new HashMap<>();
        for(ItemStack temp : inputs){
            if(temp.isEmpty()){
                continue;
            }
            String key = temp.getItem().getDescriptionId().toString();
            if(!hashmap.containsKey(key)){
                hashmap.put(key,temp.getCount());
            }else {
                hashmap.put(key,hashmap.get(key)+temp.getCount());
            }
        }

        for (ItemStack temp : storedRecipe.input()){
            if(temp.isEmpty()){
                continue;
            }
            String key = temp.getItem().getDescriptionId().toString();
            if(hashmap.containsKey(key)){
                hashmap.put(key,hashmap.get(key)-temp.getCount());
            }else {
                return false;
            }
        }

        for(var key : hashmap.keySet()){
            if(hashmap.get(key) < 0){
                return false;
            }
        }

        return true;
    }

    public boolean canMatchRecipe(ItemStack itemStack){
        RecipeItem.RecipeRecord storedRecipe;
        if(getStoredRecipeItem().isEmpty()){
           return false;
        }else {
            storedRecipe = RecipeItem.getRecipe(getStoredRecipeItem());
        }
        if(storedRecipe == null){
            return false;
        }else{
            boolean flag = false;
            int neededCount = 0;
            for (ItemStack temp : storedRecipe.input()){
                if(temp.is(itemStack.getItem())){
                    flag = true;
                    neededCount += temp.getCount();
                }
            }
            if(flag){
                for(ItemStack temp : inputs){
                    if(temp.is(itemStack.getItem())){
                        neededCount -= temp.getCount();
                    }
                }
            }
            if(neededCount > 0){
                return true;
            }
        }
        return false;
    }

    public boolean canAddSoupBase(ItemStack bucket){
        for(Map.Entry<ResourceLocation, ISoupBase> entry : SoupBaseManager.getAllSoupBases().entrySet()) {
            ResourceLocation key = (ResourceLocation)entry.getKey();
            ISoupBase soupBase = (ISoupBase)entry.getValue();
            if (soupBase.isSoupBase(bucket)) {
                return true;
            }
        }
        return false;
    }

    public void addSoupBaseByArm(Level level, ArmBlockEntity arm, ItemStack bucket) {
        if (this.status == 0) {
            for(Map.Entry<ResourceLocation, ISoupBase> entry : SoupBaseManager.getAllSoupBases().entrySet()) {
                ResourceLocation key = (ResourceLocation)entry.getKey();
                ISoupBase soupBase = (ISoupBase)entry.getValue();
                if (soupBase.isSoupBase(bucket)) {
                    this.soupBaseId = key;
                    this.status = 1;
                    this.refresh();
                }
            }
        }
    }

    public boolean canAddInIngredient(ItemStack itemStack){
        if (this.status != 1) {
            return false;
        } else if (!itemStack.has(DataComponents.FOOD) && !itemStack.is(TagMod.POT_INGREDIENT)) {
            return false;
        } else if(!canMatchRecipe(itemStack)) {
            return false;
        } else {
            for(int i = 0; i < this.inputs.size(); ++i) {
                if (((ItemStack)this.inputs.get(i)).isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addIngredientByArm(ArmBlockEntity arm, ItemStack itemStack) {
        for(int i = 0; i < this.inputs.size(); ++i) {
            if (((ItemStack)this.inputs.get(i)).isEmpty()) {
//                    Item containerItem = ItemUtils.getContainerItem(itemStack);
//                    if (containerItem != Items.AIR) {
//                        ItemUtils.getItemToLivingEntity(user, containerItem.getDefaultInstance());
//                    }

                this.inputs.set(i, itemStack.split(1));
                this.refresh();
            }
        }
    }

    public boolean canMatchContainer(ItemStack stack){
        Ingredient carrier = ((StockpotRecipe)this.recipe.value()).carrier();
        if(!carrier.isEmpty()){
            if(carrier.test(stack)){
                return true;
            }
        }
        return false;
    }

    public ItemStack getCookingResult(){
        if(this.status == 3 && !this.result.isEmpty() && this.takeoutCount > 0){
            return this.result.copyWithCount(1);
        }
        return ItemStack.EMPTY;
    }

    public ItemStack takeOutProductByArm(Level level, ArmBlockEntity arm, ItemStack stack) {
        if (this.hasLid()) {
            return ItemStack.EMPTY;
        } else if (this.status == 3 && !this.result.isEmpty() && this.takeoutCount > 0) {
            Ingredient carrier = ((StockpotRecipe)this.recipe.value()).carrier();
            if (!carrier.isEmpty() && !carrier.test(stack)) {
                return ItemStack.EMPTY;
            } else {
                ItemStack resultCopy = ItemStack.EMPTY;
                if(stack.getCount() == 1){
                    this.takeoutCount--;
                    resultCopy = this.result.copyWithCount(1);
                } else {
                    resultCopy = this.result.copyWithCount(this.takeoutCount);
                    this.takeoutCount = 0;
                }

                if(this.takeoutCount <= 0){
                    this.status = 0;
                    this.inputs.clear();
                    this.recipeId = StockpotRecipeSerializer.EMPTY_ID;
                    this.soupBaseId = ModSoupBases.WATER;
                    this.result = ItemStack.EMPTY;
                    this.currentTick = -1;
                    this.renderEntity = null;
                }

                this.refresh();
                return resultCopy;
            }
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Shadow
    public void clientTick() {
    }

    @Shadow
    public boolean hasHeatSource(Level level) {
        return false;
    }

    @Shadow
    public boolean hasLid() {
        return false;
    }

    @Shadow
    public void tick(Level level) {
    }

    @Shadow
    private void spawnParticleWithLid(Level level) {
    }

    @Shadow
    private void spawnParticleWithoutLid(Level level) {
    }

    @Shadow
    private int getBubbleColor() {
        return 0;
    }

    @Shadow
    public boolean onLitClick(Level level, LivingEntity user, ItemStack stack) {
        return false;
    }

    @Shadow
    public StockpotInput getContainer() {
        return null;
    }

    @Shadow
    private void setRecipe(Level levelIn) {
    }

    @Shadow
    private void applyRecipe(Level level, StockpotInput container, RecipeHolder<StockpotRecipe> recipe) {
    }

    @Shadow
    public boolean addSoupBase(Level level, LivingEntity user, ItemStack bucket) {
        return false;
    }

    @Shadow
    public boolean removeSoupBase(Level level, LivingEntity user, ItemStack bucket) {
        return false;
    }

    @Shadow
    public void addAllIngredients(List<ItemStack> ingredients, LivingEntity user) {
    }

    @Shadow
    public boolean addIngredient(Level level, LivingEntity user, ItemStack itemStack) {
        return false;
    }

    @Shadow
    public boolean removeIngredient(Level level, LivingEntity user) {
        return false;
    }

    @Shadow
    private boolean containerIsMatch(LivingEntity user, ItemStack stack) {
        return false;
    }

    @Shadow
    public boolean takeOutProduct(Level level, LivingEntity user, ItemStack stack) {
        return false;
    }

    @Shadow
    private void sendActionBarMessage(LivingEntity user, String key, Object... args) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inputs", ContainerHelper.saveAllItems(new CompoundTag(), this.inputs, registries));
        tag.putString("RecipeId", this.recipeId.toString());
        tag.putString("SoupBaseId", this.soupBaseId.toString());
        tag.put("Result", this.result.saveOptional(registries));
        tag.putInt("Status", this.status);
        tag.putInt("CurrentTick", this.currentTick);
        tag.putInt("TakeoutCount", this.takeoutCount);
        tag.put("LidItem", this.lidItem.saveOptional(registries));
        tag.put("RecipeRecord",this.storedRecipeItem == null ? ItemStack.EMPTY.saveOptional(registries) : this.storedRecipeItem.saveOptional(registries));
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inputs")) {
            this.inputs = NonNullList.withSize(9, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag.getCompound("Inputs"), this.inputs, registries);
        }

        if (tag.contains("RecipeId")) {
            this.recipeId = ResourceLocation.tryParse(tag.getString("RecipeId"));
            if (this.level != null) {
                RecipeManagerAccessor accessor = (RecipeManagerAccessor) this.level.getRecipeManager();
                RecipeHolder<StockpotRecipe> stockpotRecipe = accessor.invokeByKeyTyped(ModRecipes.STOCKPOT_RECIPE,recipeId);
                this.recipe = (RecipeHolder)Objects.requireNonNullElseGet(stockpotRecipe, StockpotRecipeSerializer::getEmptyRecipe);
            }
        }

        if (tag.contains("SoupBaseId")) {
            this.soupBaseId = ResourceLocation.tryParse(tag.getString("SoupBaseId"));
        }

        if (tag.contains("Result")) {
            this.result = ItemStack.parseOptional(registries, tag.getCompound("Result"));
        }

        this.status = tag.getInt("Status");
        this.currentTick = tag.getInt("CurrentTick");
        this.takeoutCount = tag.getInt("TakeoutCount");
        if (tag.contains("LidItem")) {
            this.lidItem = ItemStack.parseOptional(registries, tag.getCompound("LidItem"));
        }
        if(tag.contains("RecipeRecord")){
            this.storedRecipeItem = ItemStack.parseOptional(registries,tag.getCompound("RecipeRecord"));
        }
    }

    @Shadow
    public boolean isEmpty() {
        return false;
    }

    @Shadow
    public NonNullList<ItemStack> getInputs() {
        return this.inputs;
    }
    @Shadow
    public int getStatus() {
        return this.status;
    }
    @Shadow
    public int getTakeoutCount() {
        return this.takeoutCount;
    }
    @Shadow
    public ItemStack getResult() {
        return this.result;
    }
    @Shadow
    public ResourceLocation getSoupBaseId() {
        return this.soupBaseId;
    }
    @Shadow
    @Nullable
    public ISoupBase getSoupBase() {
        return SoupBaseManager.getSoupBase(this.soupBaseId);
    }
    @Shadow
    public ItemStack getLidItem() {
        return this.lidItem;
    }
    @Shadow
    public void setLidItem(ItemStack lidItem) {
    }

    public ItemStack getStoredRecipeItem(){
        return this.storedRecipeItem != null ? this.storedRecipeItem : ItemStack.EMPTY;
    }
}
