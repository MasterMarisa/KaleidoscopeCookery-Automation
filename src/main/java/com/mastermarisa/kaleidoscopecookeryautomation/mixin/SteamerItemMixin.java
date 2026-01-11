package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SteamerItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin({SteamerItem.class})
public class SteamerItemMixin extends BlockItem {
    public SteamerItemMixin() {
        super((Block) ModBlocks.STEAMER.get(), new Item.Properties());
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        Level level = context.getLevel();
        Direction face = context.getClickedFace();
        BlockPos clickedPos = context.getClickedPos();
        if (face != Direction.UP) {
            return false;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(clickedPos);
            ItemStack stack = context.getItemInHand();
            if (blockEntity instanceof SteamerBlockEntity) {
                SteamerBlockEntity steamer = (SteamerBlockEntity)blockEntity;
                if (stack.is(this) && stack.getCount() == 1) {
                    steamer.mergeItem(stack, context.getLevel());
                }
            }

            return super.placeBlock(context, state);
        }
    }

    @Shadow
    public int getMaxStackSize(ItemStack stack) {
        return stack.has(DataComponents.BLOCK_ENTITY_DATA) ? 1 : super.getMaxStackSize(stack);
    }

    @Shadow
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kaleidoscope_cookery.steamer").withStyle(ChatFormatting.GRAY));
    }
}
