package com.mastermarisa.kaleidoscopecookery_automation.client.ponder.utils;

import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

public class FunnelWrapper extends PonderBlockWrapper {
    protected final CreateSceneBuilder builder;

    public FunnelWrapper(BlockPos pos, SceneBuilder scene, SceneBuildingUtil util, CreateSceneBuilder builder) {
        super(pos, scene, util);
        this.builder = builder;
    }

    public FunnelWrapper(int x, int y, int z, SceneBuilder scene, SceneBuildingUtil util, CreateSceneBuilder builder) {
        this(util.grid().at(x,y,z), scene, util, builder);
    }

    public void transferToBeltLike(BlockPos beltLike, Direction insertSide, ItemStack itemStack) {
        modifyBlockEntity(FunnelBlockEntity.class, be -> be.onTransfer(itemStack));
        builder.world().createItemOnBeltLike(beltLike, insertSide, itemStack);
    }

    public void transferToBeltLike(Direction insertSide, ItemStack itemStack) {
        transferToBeltLike(pos.below(), insertSide, itemStack);
    }
}
