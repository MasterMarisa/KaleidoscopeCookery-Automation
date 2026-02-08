package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SteamerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class SteamerPlacementHelper {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        Level level = event.getLevel();
        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        BlockHitResult hitResult = event.getHitVec();

        if (level.isClientSide()) return;
        if (player.isSecondaryUseActive()) return;
        if (!itemStack.is(ModItems.STEAMER)) return;
        SteamerItem steamerItem = (SteamerItem) itemStack.getItem();
        BlockPos pos = hitResult.getBlockPos();
        BlockState blockState = level.getBlockState(pos);
        while (blockState.is(ModBlocks.STEAMER) && !blockState.getValue(SteamerBlock.HAS_LID) && !blockState.getValue(SteamerBlock.HALF)){
            pos = pos.above();
            blockState = level.getBlockState(pos);
        }
        InteractionResult place;
        if(blockState.canBeReplaced() || (blockState.is(ModBlocks.STEAMER) && blockState.getValue(SteamerBlock.HALF) && !blockState.getValue(SteamerBlock.HAS_LID)))
            place = steamerItem.place(new BlockPlaceContext(player, event.getHand(), itemStack, hitResult.withDirection(Direction.UP).withPosition(pos)));
        else
            place = steamerItem.place(new BlockPlaceContext(player, event.getHand(), itemStack, hitResult.withDirection(Direction.DOWN).withPosition(pos)));

        if (place.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(place);
        }
    }
}
