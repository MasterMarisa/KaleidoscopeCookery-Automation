package com.mastermarisa.kaleidoscopecookery_automation.arm.point;

import com.github.ysbbbbbb.kaleidoscopecookery.api.recipe.soupbase.ISoupBase;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.soupbase.SoupBaseManager;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookery_automation.attachment.StoredRecipe;
import com.mastermarisa.kaleidoscopecookery_automation.utils.FakePlayerUtils;
import com.mastermarisa.kaleidoscopecookery_automation.utils.ItemHandlerUtils;
import com.mastermarisa.kaleidoscopecookery_automation.utils.StackPredicate;
import com.simibubi.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;

public class StockPotPoint extends AllArmInteractionPointTypes.TopFaceArmInteractionPoint{
    public StockPotPoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 9;
    }

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if (blockEntity instanceof StockpotBlockEntity pot) {
            RecipeHolder<? extends Recipe<?>> holder = pot.getData(StoredRecipe.TYPE).holder;
            if (holder == null || !holder.value().getType().equals(ModRecipes.STOCKPOT_RECIPE)) return stack;
            if (pot.hasLid()) return stack;
            StockpotRecipe recipe = (StockpotRecipe) holder.value();
            ISoupBase iSoupBase = SoupBaseManager.getSoupBase(recipe.soupBase());
            FakePlayer fakePlayer = FakePlayerUtils.getPlayer((ServerLevel) level);
            switch (pot.getStatus()) {
                case 0:
                    if (iSoupBase.isSoupBase(stack)) {
                        ItemStack remainder = stack.copy();
                        ItemStack toInsert = remainder.split(1);
                        if(!simulate){
                            pot.addSoupBase(level,fakePlayer,toInsert);
                            fakePlayer.getInventory().clearContent();
                        }
                        remainder = new ItemStack(ItemUtils.getContainerItem(stack));
                        return remainder;
                    }
                    break;
                case 1:
                    List<StackPredicate> required = recipe.ingredients().stream().filter(i->!i.isEmpty()).map(StackPredicate::new).toList();
                    required = ItemHandlerUtils.getRequired(required,pot.getInputs());
                    if (!required.isEmpty()) {
                        if (required.stream().anyMatch(p -> p.test(stack))) {
                            ItemStack remainder = stack.copy();
                            ItemStack toInsert = remainder.split(1);
                            if (!simulate) {
                                pot.addIngredient(level,fakePlayer,toInsert);
                                fakePlayer.getInventory().clearContent();
                            }
                            return remainder;
                        }
                    } else {
                        if (stack.is(ModItems.STOCKPOT_LID)) {
                            ItemStack remainder = stack.copy();
                            ItemStack toInsert = remainder.split(1);
                            if(!simulate){
                                pot.setLidItem(toInsert);
                                pot.setChanged();
                                pot.refresh();
                                level.setBlockAndUpdate(pot.getBlockPos(), (BlockState)pot.getBlockState().setValue(StockpotBlock.HAS_LID, true));
                            }
                            return remainder;
                        }
                    }
                    break;
                case 2:
                    break;
                case 3:
                    if(!stack.isEmpty() && !(stack.getCount() == pot.getTakeoutCount() || stack.getCount() == 1)){
                        return stack;
                    }else {
                        if(recipe.carrier().test(stack)){
                            ItemStack remainder = pot.getResult().copyWithCount(stack.getCount());
                            if(!simulate){
                                pot.takeOutProduct(level,fakePlayer,stack);
                                fakePlayer.getInventory().clearContent();
                            }
                            return remainder;
                        }
                    }
                    break;
            }
        }

        return stack;
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if(blockEntity instanceof StockpotBlockEntity pot){
            if(pot.getStatus() == 3 && pot.hasLid()){
                ItemStack lid = pot.getLidItem().isEmpty() ? ((Item)ModItems.STOCKPOT_LID.get()).getDefaultInstance() : pot.getLidItem().copy();
                if(!simulate){
                    pot.setLidItem(ItemStack.EMPTY);
                    pot.setChanged();
                    level.setBlockAndUpdate(pot.getBlockPos(), (BlockState)pot.getBlockState().setValue(StockpotBlock.HAS_LID, false));
                }
                return lid;
            }
        }
        return ItemStack.EMPTY;
    }
}
