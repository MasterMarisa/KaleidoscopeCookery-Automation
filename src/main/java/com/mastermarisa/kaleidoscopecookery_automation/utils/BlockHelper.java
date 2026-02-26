package com.mastermarisa.kaleidoscopecookery_automation.utils;

import com.simibubi.create.content.decoration.encasing.EncasedBlock;
import com.simibubi.create.content.decoration.encasing.EncasingRegistry;
import net.minecraft.world.level.block.Block;

import java.util.Optional;

public class BlockHelper {
    public static Optional<Block> getEncasedVariant(Block encasable, Block casing) {
        return EncasingRegistry.getVariants(encasable).stream().filter(b -> b instanceof EncasedBlock encased && encased.getCasing().equals(casing)).findFirst();
    }
}
