package com.mastermarisa.kaleidoscopecookery_automation.arm.point;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.KitchenShovelItem;
import com.github.ysbbbbbb.kaleidoscopecookery.item.OilPotItem;
import com.mastermarisa.kaleidoscopecookery_automation.attachment.StoredRecipe;
import com.mastermarisa.kaleidoscopecookery_automation.utils.FakePlayerUtils;
import com.mastermarisa.kaleidoscopecookery_automation.utils.ItemHandlerUtils;
import com.mastermarisa.kaleidoscopecookery_automation.utils.StackPredicate;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

import java.util.List;

public class PotPoint extends ArmInteractionPoint {
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
        if (blockEntity instanceof PotBlockEntity pot) {
            RecipeHolder<? extends Recipe<?>> holder = pot.getData(StoredRecipe.TYPE).holder;
            if (holder == null || !holder.value().getType().equals(ModRecipes.POT_RECIPE)) return stack;
            if(!pot.hasHeatSource(level)) return stack;
            PotRecipe recipe = (PotRecipe) holder.value();
            FakePlayer fakePlayer = FakePlayerUtils.getPlayer((ServerLevel) level);
            switch (pot.getStatus()) {
                case 0:
                    if (pot.getBlockState().getValue(PotBlock.HAS_OIL)) {
                        List<StackPredicate> required = recipe.ingredients().stream().filter(i->!i.isEmpty()).map(StackPredicate::new).toList();
                        required = ItemHandlerUtils.getRequired(required,pot.getInputs());
                        if (!required.isEmpty()) {
                            if (required.stream().anyMatch(p -> p.test(stack))) {
                                ItemStack remainder = stack.copy();
                                ItemStack toInsert = remainder.split(1);
                                if (!simulate) {
                                    pot.addIngredient(level,fakePlayer,toInsert);
                                }
                                return remainder;
                            }
                        } else {
                            if (stack.is(TagMod.KITCHEN_SHOVEL)) {
                                ItemStack remainder = stack.copy();
                                remainder.setDamageValue(remainder.getDamageValue() + 1);
                                if(!simulate){
                                    pot.onShovelHit(level,fakePlayer,remainder);
                                    return remainder;
                                }
                                return ItemStack.EMPTY;
                            }
                        }
                    } else {
                        if (isOilItem(stack)) {
                            if (!simulate) {
                                pot.onPlaceOil(level,fakePlayer,new ItemStack(ModItems.OIL.get()));
                            }
                            return getPlaceOilReturn(stack);
                        }
                    }
                    break;
                case 1:
                    if (stack.is(TagMod.KITCHEN_SHOVEL)) {
                        if(!simulate){
                            pot.onShovelHit(level,fakePlayer,stack);
                            return stack;
                        }
                        return ItemStack.EMPTY;
                    }
                    break;
                case 2:
                    if (pot.hasCarrier()) {
                        if (recipe.carrier().test(stack) && stack.getCount() == pot.getResult().getCount()) {
                            ItemStack result = pot.getResult();
                            if (!simulate) {
                                pot.takeOutProduct(level,fakePlayer,stack.copy());
                            }
                            return result;
                        }
                    }
                    break;
            }
        }

        return stack;
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if (blockEntity instanceof PotBlockEntity pot) {
            RecipeHolder<? extends Recipe<?>> holder = pot.getData(StoredRecipe.TYPE).holder;
            if (holder == null || !holder.value().getType().equals(ModRecipes.POT_RECIPE)) return ItemStack.EMPTY;
            if(!pot.hasHeatSource(level)) return ItemStack.EMPTY;
            if (pot.getStatus() == 2) {
                ItemStack result = pot.getResult();
                if (!simulate) {
                    pot.reset();
                }
                return result;
            }
        }

        return ItemStack.EMPTY;
    }

    private ItemStack getPlaceOilReturn(ItemStack stack){
        ItemStack ans = ItemStack.EMPTY;
        if (stack.is(TagMod.OIL)) {
            ans = stack.copy();
            ans.split(1);
        } else if (stack.is(TagMod.KITCHEN_SHOVEL) && KitchenShovelItem.hasOil(stack)) {
            ans = stack.copy();
            KitchenShovelItem.setHasOil(ans, false);
        } else if (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack)) {
            ans = stack.copy();
            OilPotItem.shrinkOilCount(ans);
        }

        return ans;
    }

    private boolean isOilItem(ItemStack stack) {
        return stack.is(TagMod.OIL) || (stack.is(ModItems.KITCHEN_SHOVEL.get()) && KitchenShovelItem.hasOil(stack)) || (stack.is(ModItems.OIL_POT.get()) && OilPotItem.hasOil(stack));
    }
}
