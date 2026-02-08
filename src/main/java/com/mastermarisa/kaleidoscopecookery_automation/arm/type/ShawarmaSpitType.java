package com.mastermarisa.kaleidoscopecookery_automation.arm.type;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.ShawarmaSpitBlock;
import com.mastermarisa.kaleidoscopecookery_automation.arm.point.ShawarmaSpitPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ShawarmaSpitType extends ArmInteractionPointType {
    public boolean canCreatePoint(Level level, BlockPos pos, BlockState state) {
        return state.getBlock() instanceof ShawarmaSpitBlock;
    }

    public ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state) {
        return new ShawarmaSpitPoint(this, level, pos, state);
    }
}
