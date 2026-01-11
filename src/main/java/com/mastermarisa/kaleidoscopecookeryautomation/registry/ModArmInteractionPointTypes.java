package com.mastermarisa.kaleidoscopecookeryautomation.registry;

import com.github.ysbbbbbb.kaleidoscopecookery.block.decoration.FruitBasketBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.*;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.OilPotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.ShawarmaSpitBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IPotBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IShawarmaSpitBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.ISteamerBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IStockpotBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.KaleidoscopeCookeryAutomation;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModArmInteractionPointTypes {
    private static final DeferredRegister<ArmInteractionPointType> REGISTER =
            DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, KaleidoscopeCookeryAutomation.MOD_ID);

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> STOCKPOT_ARM_INTERACT =
            REGISTER.register("stockpot",()-> new StockPotType());

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> POT_ARM_INTERACT =
            REGISTER.register("pot",()-> new PotType());

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> OILPOT_ARM_INTERACT =
            REGISTER.register("oilpot",()-> new OilPotType());

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> STEAMER_ARM_INTERACT =
            REGISTER.register("steamer",()-> new SteamerType());

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> FRUITBASKET_ARM_INTERACT =
            REGISTER.register("fruit_basket",()-> new FruitBasketType());

    public static final DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> SHAWARMASPIT_ARM_INTERACT =
            REGISTER.register("shawarma_spit",()-> new ShawarmaSpitType());

    public static void register(IEventBus modBus) {
        REGISTER.register(modBus);
    }


    public static class StockPotType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof StockpotBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new StockPotPoint(this, level, pos, state);
        }
    }

    public static class StockPotPoint extends AllArmInteractionPointTypes.TopFaceArmInteractionPoint{
        public StockPotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 9;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof StockpotBlockEntity entity){
                if(entity.hasLid()){
                    return stack;
                }
                IStockpotBlockEntityAccess stockpot = (IStockpotBlockEntityAccess)entity;
                if(entity.getStatus() == 0){
                    if(!stockpot.getStoredRecipeItem().isEmpty() && stockpot.canAddSoupBase(stack)){
                        ItemStack remainder = stack.copy();
                        ItemStack toInsert = remainder.split(1);
                        if(!simulate){
                            ((IStockpotBlockEntityAccess) entity).addSoupBaseByArm(level,armBlockEntity,toInsert);
                        }
                        remainder = new ItemStack(ItemUtils.getContainerItem(stack));
                        return remainder;
                    }else {
                        return stack;
                    }
                }else if(entity.getStatus() == 1){
                    if(stack.is(ModItems.STOCKPOT_LID.get())){
                        if(stockpot.inputMatchRecipe(level)){
                            ItemStack remainder = stack.copy();
                            ItemStack toInsert = remainder.split(1);
                            if(!simulate){
                                entity.setLidItem(toInsert);
                                entity.setChanged();
                                level.setBlockAndUpdate(entity.getBlockPos(), (BlockState)entity.getBlockState().setValue(StockpotBlock.HAS_LID, true));
                            }
                            return remainder;
                        }
                    } else if(stockpot.canAddInIngredient(stack)){
                        ItemStack remainder = stack.copy();
                        ItemStack toInsert = remainder.split(1);

                        if(!simulate){
                            stockpot.addIngredientByArm(armBlockEntity,toInsert);
                        }

                        return remainder;
                    }
                }else if(entity.getStatus() == 3){
                    if(!stack.isEmpty() && (stack.getCount() != entity.getTakeoutCount() || stack.getCount() != 1)){
                        return stack;
                    }else {
                        if(stockpot.canMatchContainer(stack)){
                            ItemStack remainder = stockpot.getCookingResult();

                            if(!simulate){
                                remainder = stockpot.takeOutProductByArm(level,armBlockEntity,stack.copy());
                            }
                            return remainder;
                        }
                    }
                }
            }
            return stack;
        }

        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof StockpotBlockEntity entity){
                if(entity.getStatus() == 3 && entity.hasLid()){
                    ItemStack lid = entity.getLidItem().isEmpty() ? ((Item)ModItems.STOCKPOT_LID.get()).getDefaultInstance() : entity.getLidItem().copy();
                    if(!simulate){
                        entity.setLidItem(ItemStack.EMPTY);
                        entity.setChanged();
                        level.setBlockAndUpdate(entity.getBlockPos(), (BlockState)entity.getBlockState().setValue(StockpotBlock.HAS_LID, false));
                    }
                    return lid;
                }
            }
            return ItemStack.EMPTY;
        }
    }


    public static class PotType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof PotBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new PotPoint(this, level, pos, state);
        }
    }

    public static class PotPoint extends ArmInteractionPoint{
        public PotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(this.pos).add((double)0.5F, (double)0.3125F, (double)0.5F);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 9;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof PotBlockEntity entity){
                IPotBlockEntityAccess pot = (IPotBlockEntityAccess) entity;
                if(!entity.hasHeatSource(level)){
                    return stack;
                }

                if(entity.getStatus() == 0){
                    if(!entity.getBlockState().getValue(PotBlock.HAS_OIL)){
                        ItemStack remainder = stack.copy();
                        if(!pot.getStoredRecipeItem().isEmpty() && pot.canPlaceOil(level,remainder)){
                            if(!simulate) {
                                pot.placeOilByArm(level, armBlockEntity, remainder);
                                remainder = pot.getPlaceOilReturn(remainder);
                                return remainder;
                            }else {
                                if(!remainder.is(TagMod.OIL)){
                                    return ItemStack.EMPTY;
                                }else {
                                    remainder = pot.getPlaceOilReturn(remainder);
                                    return remainder;
                                }
                            }
                        }
                    } else {
                        if(pot.canAddIngredient(level,armBlockEntity,stack)){
                            ItemStack remainder = stack.copy();
                            ItemStack toInsert = remainder.split(1);

                            if(!simulate){
                                pot.addIngredientByArm(level,armBlockEntity,toInsert);
                            }

                            return remainder;
                        } else if (stack.is(ModItems.KITCHEN_SHOVEL) && pot.inputMatchRecipe(level)){
                            ItemStack remainder = stack.copy();
                            remainder.setDamageValue(remainder.getDamageValue() + 1);
                            if(!simulate){
                                pot.onShovelHitByArm(level,armBlockEntity,remainder);
                                return remainder;
                            }
                            return ItemStack.EMPTY;
                        }
                    }
                }else if(entity.getStatus() == 1){
                    if(stack.is(ModItems.KITCHEN_SHOVEL) && level.getGameTime() - pot.getLastStirByArmTime() > 16){
                        ItemStack remainder = stack.copy();
                        if(pot.getStirFryCount() > 0){
                            remainder.setDamageValue(remainder.getDamageValue() + 1);
                        }
                        if(!simulate){
                            pot.onShovelHitByArm(level,armBlockEntity,remainder);
                            return remainder;
                        }
                        return ItemStack.EMPTY;
                    }
                }else if(entity.getStatus() == 2 || entity.getStatus() == 3){
                    if(pot.canTakeOutWithCarrier(level,stack)){
                        ItemStack remainder = pot.getResultFood(level);

                        if(!simulate){
                            entity.reset();
                        }

                        return remainder;
                    }
                }
            }
            return stack;
        }

        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof PotBlockEntity entity){
                IPotBlockEntityAccess pot = (IPotBlockEntityAccess) entity;
                if(entity.getStatus() == 2 || entity.getStatus() == 3){
                    if(pot.canTakeOutWithoutCarrier(level)){
                        ItemStack remainder = pot.getResultFood(level);

                        if(!simulate){
                            entity.reset();
                        }

                        return remainder;
                    }
                }
            }
            return ItemStack.EMPTY;
        }
    }

    public static class OilPotType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof OilPotBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new OilPotPoint(this, level, pos, state);
        }
    }

    public static class OilPotPoint extends ArmInteractionPoint{
        public OilPotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(this.pos).add((double)0.5F, (double)0.5125F, (double)0.5F);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 1;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            if(!stack.is(ModItems.KITCHEN_SHOVEL.get())){
                return stack;
            }
            if(KitchenShovelItem.hasOil(stack)){
                return stack;
            }

            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof OilPotBlockEntity entity){
                if(entity.getOilCount() > 0){
                    if(!simulate){
                        ItemStack remainder = stack.copy();
                        KitchenShovelItem.setHasOil(remainder,true);
                        entity.setOilCount(entity.getOilCount() - 1);
                        return remainder;
                    }

                    return ItemStack.EMPTY;
                }
            }

            return stack;
        }

        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof OilPotBlockEntity entity){
                if(amount > 64){
                    amount = 64;
                }
                if(entity.getOilCount() >= amount){
                    if(!simulate){
                        entity.setOilCount(entity.getOilCount() - amount);
                    }

                    return new ItemStack(ModItems.OIL.get(),amount);
                }else {
                    ItemStack remainder = new ItemStack(ModItems.OIL.get(),entity.getOilCount());

                    if(!simulate){
                        entity.setOilCount(0);
                    }

                    return remainder;
                }
            }

            return ItemStack.EMPTY;
        }
    }

    public static class SteamerType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof SteamerBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new SteamerPoint(this, level, pos, state);
        }
    }

    public static class SteamerPoint extends ArmInteractionPoint{
        public SteamerPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 8;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof SteamerBlockEntity entity){
                ISteamerBlockEntityAccess steamer = (ISteamerBlockEntityAccess) entity;
                if(steamer.canAddItemByArm(level,armBlockEntity,stack)){
                    ItemStack remainder = stack.copy();
                    ItemStack toInsert = remainder.split(steamer.getEmptySlotCount(level));

                    if(!simulate){
                        steamer.addItemByArm(level,armBlockEntity,toInsert);
                    }

                    return remainder;
                }
            }

            return stack;
        }
    }

    public static class FruitBasketType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof FruitBasketBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new FruitBasketPoint(this, level, pos, state);
        }
    }

    public static class FruitBasketPoint extends ArmInteractionPoint{
        public FruitBasketPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        protected Vec3 getInteractionPositionVector() {
            return Vec3.atLowerCornerOf(this.pos).add((double)0.5F, (double)0.5125F, (double)0.5F);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 8;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            if (!stack.is(TagMod.MEALS)){
                return stack;
            }

            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof FruitBasketBlockEntity entity){
                ItemStackHandler handler = entity.getItems();
                int space = 0;
                for(int i = 0;i < 8;i++){
                    ItemStack itemStack = handler.getStackInSlot(i);
                    if(itemStack.isEmpty()){
                        space += 64;
                    } else if(ItemStack.isSameItemSameComponents(itemStack,stack)){
                        space += 64 - itemStack.getCount();
                    }
                }

                if(space > 0){
                    ItemStack remainder = stack.copy();
                    space = Math.min(space,remainder.getCount());
                    ItemStack toInsert = remainder.split(space);

                    if(!simulate){
                        entity.putOn(toInsert);
                    }

                    return remainder;
                }
            }

            return stack;
        }
    }

    public static class ShawarmaSpitType extends ArmInteractionPointType{
        public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
            return state.getBlock() instanceof ShawarmaSpitBlock;
        }

        public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
            return new ShawarmaSpitPoint(this, level, pos, state);
        }
    }

    public static class ShawarmaSpitPoint extends ArmInteractionPoint{
        public ShawarmaSpitPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
            super(type, level, pos, state);
        }

        public int getSlotCount(ArmBlockEntity armBlockEntity) {
            return 1;
        }

        public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof ShawarmaSpitBlockEntity entity){
                IShawarmaSpitBlockEntityAccess shawarma = (IShawarmaSpitBlockEntityAccess) entity;
                if(shawarma.canPutCookingItem(level,stack)){
                    ItemStack remainder = stack.copy();
                    ItemStack toInsert = remainder.split(8);

                    if(!simulate){
                        entity.onPutCookingItem(level,toInsert);
                    }

                    return remainder;
                }
            }

            return stack;
        }

        public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(blockEntity instanceof ShawarmaSpitBlockEntity entity) {
                IShawarmaSpitBlockEntityAccess shawarma = (IShawarmaSpitBlockEntityAccess) entity;
                if(shawarma.canTakeCookedItem(level)){
                    ItemStack remainder = entity.cookedItem;

                    if(!simulate){
                        shawarma.takeCookedItemByArm(level);
                    }

                    return remainder;
                }
            }
            return ItemStack.EMPTY;
        }
    }
}
