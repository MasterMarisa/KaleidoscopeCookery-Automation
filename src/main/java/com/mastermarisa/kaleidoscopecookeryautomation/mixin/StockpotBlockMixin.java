package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IStockpot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.StockpotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoundType;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IStockpotBlockEntityAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;

@Mixin({StockpotBlock.class})
public class StockpotBlockMixin extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {
    public StockpotBlockMixin() {
        super(Properties.of().mapColor(MapColor.METAL).sound(ModSoundType.POT).noOcclusion().strength(1.5F, 6.0F));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.SOUTH)).setValue(StockpotBlock.WATERLOGGED, false)).setValue(StockpotBlock.HAS_LID, false)).setValue(StockpotBlock.HAS_BASE, false)).setValue(StockpotBlock.HAS_CHAINS, false));
    }

    @Shadow
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return StockpotBlock.CODEC;
    }

    @Shadow
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }

    @Shadow
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor levelAccessor, BlockPos pos, BlockPos neighborPos) {
        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof IStockpot)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                IStockpot stockpot = (IStockpot)blockEntity;
                ItemStack mainHandItem = player.getMainHandItem();
                if (((IStockpotBlockEntityAccess)blockEntity).takeArmRecipe(level,player,mainHandItem)){
                    return ItemInteractionResult.SUCCESS;
                } else if (stockpot.onLitClick(level, player, mainHandItem)) {
                    return ItemInteractionResult.SUCCESS;
                } else if (stockpot.addSoupBase(level, player, mainHandItem)) {
                    ((ModEventTrigger)ModTrigger.EVENT.get()).trigger(player, "put_soup_base_in_stockpot");
                    return ItemInteractionResult.SUCCESS;
                } else if (stockpot.removeSoupBase(level, player, mainHandItem)) {
                    return ItemInteractionResult.SUCCESS;
                } else if (!mainHandItem.isEmpty() && stockpot.addIngredient(level, player, mainHandItem)) {
                    return ItemInteractionResult.SUCCESS;
                } else if ((mainHandItem.isEmpty() || mainHandItem.is(TagMod.INGREDIENT_CONTAINER)) && stockpot.removeIngredient(level, player)) {
                    return ItemInteractionResult.SUCCESS;
                } else {
                    return stockpot.takeOutProduct(level, player, mainHandItem) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            }
        }
    }

    @Shadow
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    @Shadow
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @Shadow
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return null;
    }

    @Shadow
    public FluidState getFluidState(BlockState state) {
        return null;
    }

    @Shadow
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    @Shadow
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return null;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder lootParamsBuilder) {
        List<ItemStack> drops = super.getDrops(state, lootParamsBuilder);
        if ((Boolean)state.getValue(StockpotBlock.HAS_LID)) {
            label19: {
                BlockEntity parameter = (BlockEntity)lootParamsBuilder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
                if (parameter instanceof StockpotBlockEntity) {
                    StockpotBlockEntity stockpot = (StockpotBlockEntity)parameter;
                    if (!stockpot.getLidItem().isEmpty()) {
                        drops.add(stockpot.getLidItem().copy());
                        break label19;
                    }
                }

                drops.add(new ItemStack((ItemLike) ModItems.STOCKPOT_LID.get()));
            }
        }

        BlockEntity parameter = (BlockEntity)lootParamsBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        if (parameter instanceof StockpotBlockEntity stockpotBlock) {
            if (stockpotBlock.getStatus() == 1) {
                stockpotBlock.getInputs().forEach((stack) -> {
                    if (!stack.isEmpty()) {
                        drops.add(stack);
                    }

                });
            }
            if(((IStockpotBlockEntityAccess)stockpotBlock).getStoredRecipeItem() != null
                    && !((IStockpotBlockEntityAccess)stockpotBlock).getStoredRecipeItem().isEmpty()){
                drops.add(((IStockpotBlockEntityAccess)stockpotBlock).getStoredRecipeItem());
            }
        }

        return drops;
    }

    @Shadow
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
