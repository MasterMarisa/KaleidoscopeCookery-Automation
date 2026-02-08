package com.mastermarisa.kaleidoscopecookery_automation.arm.point;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.decoration.TableBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.kaleidoscopecookery_automation.data.TagItem;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;


public class TablePoint extends ArmInteractionPoint {
    public TablePoint(ArmInteractionPointType type, Level level, BlockPos pos, BlockState state) {
        super(type, level, pos, state);
    }

    public int getSlotCount(ArmBlockEntity armBlockEntity) {
        return 4;
    }

    public ItemStack insert(ArmBlockEntity armBlockEntity, ItemStack stack, boolean simulate) {
        if (!stack.is(TagItem.RESULT)) return stack;

        if (level.getBlockEntity(pos) instanceof TableBlockEntity table) {
            ItemStackHandler tableItems = table.getItems();
            if (!simulate) return ItemHandlerHelper.insertItemStacked(tableItems,stack,false);
            else return ItemHandlerHelper.insertItemStacked(tableItems,stack,true);
        }

        return stack;
    }

    public ItemStack extract(ArmBlockEntity armBlockEntity, int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }
}
