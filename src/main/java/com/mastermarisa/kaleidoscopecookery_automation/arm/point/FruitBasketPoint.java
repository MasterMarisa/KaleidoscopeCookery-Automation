package com.mastermarisa.kaleidoscopecookery_automation.arm.point;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.FruitBasketBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.kaleidoscopecookery_automation.data.TagItem;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class FruitBasketPoint extends ArmInteractionPoint {
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
        if (!stack.is(TagItem.RESULT)) return stack;

        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        if(blockEntity instanceof FruitBasketBlockEntity entity){
            ItemStackHandler handler = entity.getItems();
            if (!simulate) return ItemHandlerHelper.insertItemStacked(handler,stack.copy(),false);
            else return ItemHandlerHelper.insertItemStacked(handler,stack.copy(),true);
        }

        return stack;
    }
}
