package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IPot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.BaseBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.SimpleInput;
import com.github.ysbbbbbb.kaleidoscopecookery.init.*;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagCommon;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModConfig;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IPotBlockEntityAccess;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.HashMap;
import java.util.List;

@Mixin({PotBlockEntity.class})
public class PotBlockEntityMixin extends BaseBlockEntity implements IPot, IPotBlockEntityAccess {
    @Unique
    private ItemStack storedRecipeItem;
    @Unique
    public long lastStirByArmTime;
    @Shadow
    private NonNullList<ItemStack> inputs;
    @Shadow
    private Ingredient carrier;
    @Shadow
    private ItemStack result;
    @Shadow
    private int status;
    @Shadow
    private int currentTick;
    @Shadow
    private int stirFryCount;
    @Shadow
    public long seed;
    @Shadow
    public PotBlockEntity.StirFryAnimationData animationData;

    public PotBlockEntityMixin(BlockPos pPos, BlockState pBlockState) {
        super((BlockEntityType) ModBlocks.POT_BE.get(), pPos, pBlockState);
        this.inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        this.carrier = Ingredient.EMPTY;
        this.result = ItemStack.EMPTY;
        this.status = 0;
        this.currentTick = 0;
        this.stirFryCount = 0;
        this.animationData = new PotBlockEntity.StirFryAnimationData();
        this.seed = System.currentTimeMillis();
        this.storedRecipeItem = ItemStack.EMPTY;
        this.lastStirByArmTime = 0;
    }

    public void setArmRecipe(Level level,LivingEntity livingEntity,ItemStack itemStack){
        if(itemStack.is(ModItems.RECIPE_ITEM.get())){
            RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemStack);
            if(record != null && record.type().equals(RecipeItem.POT)){
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

    public boolean canPlaceOil(Level level, ItemStack stack){
        if(!hasHeatSource(level)){
            return false;
        }
        if (stack.is(TagMod.OIL)) {
            return true;
        } else if (stack.is((Item)ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
            return true;
        } else if (stack.is((Item)ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            return true;
        }

        return false;
    }

    public ItemStack getPlaceOilReturn(ItemStack stack){
        ItemStack ans = ItemStack.EMPTY;
        if (stack.is(TagMod.OIL)) {
            ans = stack.copy();
            ans.shrink(1);
        } else if (stack.is((Item)ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) {
            ans = stack.copy();
            KitchenShovelItem.setHasOil(ans, false);
        } else if (stack.is((Item)ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            ans = stack.copy();
            OilPotItem.shrinkOilCount(ans);
        }

        return ans;
    }

    public void placeOilByArm(Level level, ArmBlockEntity arm,ItemStack stack){
        this.currentTick = 1200;
        BlockState state = level.getBlockState(this.worldPosition);
        level.setBlockAndUpdate(this.worldPosition, (BlockState)((BlockState)state.setValue(PotBlock.HAS_OIL, true)).setValue(PotBlock.SHOW_OIL, true));
        level.playSound((Player)null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        for(int i = 0; i < 10; ++i) {
            level.addParticle(ParticleTypes.SMOKE, (double)this.worldPosition.getX() + (double)0.5F + level.random.nextDouble() / (double)3.0F * (double)(level.random.nextBoolean() ? 1 : -1), (double)this.worldPosition.getY() + (double)0.25F + level.random.nextDouble() / (double)3.0F, (double)this.worldPosition.getZ() + (double)0.5F + level.random.nextDouble() / (double)3.0F * (double)(level.random.nextBoolean() ? 1 : -1), (double)0.0F, 0.05, (double)0.0F);
        }
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

    public boolean canAddIngredient(Level level, ArmBlockEntity arm, ItemStack itemStack) {
        if (this.status != 0) {
            return false;
        } else if (!itemStack.has(DataComponents.FOOD) && !itemStack.is(TagMod.POT_INGREDIENT)) {
            return false;
        } else if (!canMatchRecipe(itemStack)) {
            return false;
        } else {
            for(int i = 0; i < this.inputs.size(); ++i) {
                ItemStack item = (ItemStack)this.inputs.get(i);
                if (item.isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    public void addIngredientByArm(Level level, ArmBlockEntity arm, ItemStack itemStack) {
        for(int i = 0; i < this.inputs.size(); ++i) {
            ItemStack item = (ItemStack)this.inputs.get(i);
            if (item.isEmpty()) {
//                Item containerItem = ItemUtils.getContainerItem(itemStack);
//                if (containerItem != Items.AIR) {
//                    ItemUtils.getItemToLivingEntity(user, containerItem.getDefaultInstance());
//                }

                this.inputs.set(i, itemStack.split(1));
                level.playSound((Player)null, this.worldPosition, SoundEvents.LANTERN_PLACE, SoundSource.BLOCKS, 1.0F, 0.5F);
            }
        }
    }

    public void onShovelHitByArm(Level level, ArmBlockEntity arm, ItemStack shovel) {
        if (!level.isClientSide) {
            this.seed = System.currentTimeMillis();
            this.refresh();
        }

        Level var5 = this.level;
        if (var5 instanceof ServerLevel serverLevel) {
            RandomSource random = serverLevel.random;
            serverLevel.sendParticles((SimpleParticleType) ModParticles.COOKING.get(), (double)this.worldPosition.getX() + (double)0.5F + random.nextDouble() / (double)3.0F * (double)(random.nextBoolean() ? 1 : -1), (double)this.worldPosition.getY() + 0.1 + random.nextDouble() / (double)3.0F, (double)this.worldPosition.getZ() + (double)0.5F + random.nextDouble() / (double)3.0F * (double)(random.nextBoolean() ? 1 : -1), 1, (double)0.0F, (double)0.0F, (double)0.0F, 0.05);
        }

        if (this.status == 0 && !this.isEmpty()) {
            this.startCooking(level);
            ((ModEventTrigger) ModTrigger.EVENT.get()).trigger((Player)null, "stir_fry_in_pot");
            this.lastStirByArmTime = level.getGameTime();
            level.playSound((Player)null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        }

        if (this.status == 1) {
            --this.stirFryCount;
            ((ModEventTrigger) ModTrigger.EVENT.get()).trigger((Player)null, "stir_fry_in_pot");
            this.lastStirByArmTime = level.getGameTime();
            level.playSound((Player)null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        }
    }

    public boolean canTakeOutWithoutCarrier(Level level) {
        if (this.status != 2 && this.status != 3) {
            return false;
        }
        return this.carrier.isEmpty();
    }

    public boolean canTakeOutWithCarrier(Level level, ItemStack mainHandItem) {
        if (this.status != 2 && this.status != 3) {
            return false;
        }
        ItemStack finallyResult = this.status == 2 ? this.result : FoodBiteRegistry.getItem(FoodBiteRegistry.DARK_CUISINE).getDefaultInstance();
        if (finallyResult.is(FoodBiteRegistry.getItem(FoodBiteRegistry.SUSPICIOUS_STIR_FRY)) && mainHandItem.is(TagCommon.COOKED_RICE)) {
            return true;
        }
        Component carrierName = this.carrier.getItems()[0].getHoverName();
        if (this.carrier.test(mainHandItem)) {
            if (mainHandItem.getCount() == finallyResult.getCount()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getResultFood(Level level){
        ItemStack finallyResult = this.status == 2 ? this.result : FoodBiteRegistry.getItem(FoodBiteRegistry.DARK_CUISINE).getDefaultInstance();
        if (finallyResult.is(FoodBiteRegistry.getItem(FoodBiteRegistry.SUSPICIOUS_STIR_FRY))){
            finallyResult = ((Item)ModItems.SUSPICIOUS_STIR_FRY_RICE_BOWL.get()).getDefaultInstance();
        }
        return finallyResult;
    }

    @Shadow
    public boolean hasHeatSource(Level level) {
        return false;
    }

    @Shadow
    public void tick(Level level) {
    }

    @Shadow
    private void tickBurnt(Level level, RandomSource random) {
    }

    @Shadow
    private void tickFinished(RandomSource random) {
    }

    @Shadow
    private void tickCooking(Level level, RandomSource random) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private void tickPutIngredient(Level level, RandomSource random) {
        if (this.currentTick % 10 == 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles((SimpleParticleType)ModParticles.COOKING.get(), (double)this.worldPosition.getX() + (double)0.5F + random.nextDouble() / (double)5.0F * (double)(random.nextBoolean() ? 1 : -1), (double)this.worldPosition.getY() + 0.1 + random.nextDouble() / (double)3.0F, (double)this.worldPosition.getZ() + (double)0.5F + random.nextDouble() / (double)5.0F * (double)(random.nextBoolean() ? 1 : -1), 1, (double)0.0F, (double)0.0F, (double)0.0F, (double)0.0F);
        }

        if (this.currentTick == 0) {
            if (this.isEmpty()) {
                this.currentTick = 1200;
            } else {
                if(!getStoredRecipeItem().isEmpty() && inputMatchRecipe(level)){
                    this.startCooking(level);
                } else if(getStoredRecipeItem().isEmpty()){
                    this.startCooking(level);
                } else {
                    this.currentTick = 1200;
                }
            }
        }
    }

    @Shadow
    public boolean onPlaceOil(Level level, LivingEntity user, ItemStack stack) {
        return false;
    }

    @Shadow
    private void placeOil(Level level, LivingEntity user, RandomSource random) {
    }

    @Shadow
    public void onShovelHit(Level level, LivingEntity user, ItemStack shovel) {
    }

    @Shadow
    private void startCooking(Level level) {
    }

    @Shadow
    public boolean takeOutProduct(Level level, LivingEntity user, ItemStack stack) {
        return false;
    }

    @Shadow
    private boolean takeOutWithoutCarrier(Level level, LivingEntity user, ItemStack stack, ItemStack finallyResult) {
        return false;
    }

    @Shadow
    private boolean takeOutWithCarrier(Level level, LivingEntity user, ItemStack mainHandItem, ItemStack finallyResult) {
        return false;
    }

    @Shadow
    private void sendActionBarMessage(LivingEntity user, String type, Object... args) {
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
    public void reset() {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inputs", ContainerHelper.saveAllItems(new CompoundTag(), this.inputs, registries));
        tag.put("Carrier", (Tag)Ingredient.CODEC.encodeStart(NbtOps.INSTANCE, this.carrier).getOrThrow());
        tag.put("Result", this.result.saveOptional(registries));
        tag.putInt("Status", this.status);
        tag.putInt("CurrentTick", this.currentTick);
        tag.putInt("StirFryCount", this.stirFryCount);
        tag.putLong("Seed", this.seed);
        tag.put("RecipeRecord",this.storedRecipeItem == null ? ItemStack.EMPTY.saveOptional(registries) : this.storedRecipeItem.saveOptional(registries));
        if(level != null){
            tag.putLong("LastStirByArmTime",level.getGameTime());
        }
    }


    /**
     * @author
     * @reason
     */
    @Overwrite
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.inputs = NonNullList.withSize(9, ItemStack.EMPTY);
        if (tag.contains("Inputs", 10)) {
            ContainerHelper.loadAllItems(tag.getCompound("Inputs"), this.inputs, registries);
        }

        if (tag.contains("Carrier", 10)) {
            CompoundTag compound = tag.getCompound("Carrier");
            this.carrier = (Ingredient)((Pair)Ingredient.CODEC.decode(NbtOps.INSTANCE, compound).getOrThrow()).getFirst();
        }

        if (tag.contains("Result", 10)) {
            this.result = ItemStack.parseOptional(registries, tag.getCompound("Result"));
        }

        this.status = tag.getInt("Status");
        this.currentTick = tag.getInt("CurrentTick");
        this.stirFryCount = tag.getInt("StirFryCount");
        this.seed = tag.getLong("Seed");
        if(tag.contains("RecipeRecord")){
            this.storedRecipeItem = ItemStack.parseOptional(registries,tag.getCompound("RecipeRecord"));
        }
        if(tag.contains("LastStirByArmTime")){
            this.lastStirByArmTime = tag.getLong("LastStirByArmTime");
        }
    }

    @Shadow
    public List<ItemStack> getInputs() {
        return this.inputs;
    }

    @Shadow
    public SimpleInput getContainer() {
        return new SimpleInput(this.inputs);
    }

    @Shadow
    public boolean isEmpty() {
        for(ItemStack stack : this.inputs) {
            if (!stack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Shadow
    public int getStatus() {
        return this.status;
    }

    @Shadow
    public boolean hasCarrier() {
        return !this.carrier.isEmpty();
    }

    @Shadow
    public ItemStack getResult() {
        return this.result;
    }

    @Shadow
    public long getSeed() {
        return this.seed;
    }

    @Shadow
    public int getCurrentTick() {
        return this.currentTick;
    }

    public ItemStack getStoredRecipeItem(){
        return this.storedRecipeItem != null ? this.storedRecipeItem : ItemStack.EMPTY;
    }

    public int getStirFryCount(){
        return this.stirFryCount;
    }

    public long getLastStirByArmTime(){
        return this.lastStirByArmTime;
    }
}
