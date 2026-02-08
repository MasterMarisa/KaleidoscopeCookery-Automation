package com.mastermarisa.kaleidoscopecookery_automation.arm.type;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.mastermarisa.kaleidoscopecookery_automation.arm.point.StockPotPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class StockPotType extends ArmInteractionPointType {
    public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof StockpotBlock;
    }

    public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
        return new StockPotPoint(this, level, pos, state);
    }
}
