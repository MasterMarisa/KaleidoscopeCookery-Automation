package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.advancements.critereon.ModEventTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.ISteamer;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.SteamerBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.SteamerBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModTrigger;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.github.ysbbbbbb.kaleidoscopecookery.item.SteamerItem;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = {SteamerBlock.class})
public class SteamerBlockMixin extends FallingBlock implements EntityBlock, SimpleWaterloggedBlock {
    public SteamerBlockMixin() {
        super(Properties.of().mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASEDRUM).instabreak().noOcclusion().pushReaction(PushReaction.DESTROY).sound(SoundType.BAMBOO));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(SteamerBlock.FACING, Direction.NORTH)).setValue(SteamerBlock.HALF, true)).setValue(SteamerBlock.HAS_LID, false)).setValue(SteamerBlock.HAS_BASE, false)).setValue(SteamerBlock.WATERLOGGED, false));
    }

    @Shadow
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @Shadow
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }

    @Shadow
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    @Shadow
    protected MapCodec<? extends FallingBlock> codec() {
        return SteamerBlock.CODEC;
    }

    @Shadow
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor levelAccessor, BlockPos pos, BlockPos neighborPos) {
        if ((Boolean)state.getValue(SteamerBlock.WATERLOGGED)) {
            levelAccessor.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }

        levelAccessor.scheduleTick(pos, this, this.getDelayAfterPlace());
        if (direction == Direction.DOWN) {
            if (isFree(neighborState)) {
                state = (BlockState)state.setValue(SteamerBlock.HAS_BASE, false);
            } else {
                state = (BlockState)state.setValue(SteamerBlock.HAS_BASE, this.shouldHasBase(levelAccessor, pos));
            }
        }

        if (direction == Direction.UP && neighborState.is(this)) {
            state = (BlockState)state.setValue(SteamerBlock.HAS_LID, false);
        }

        return state;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack itemInHand = player.getItemInHand(hand);
        Boolean hasLid = (Boolean)state.getValue(SteamerBlock.HAS_LID);
        LogUtils.getLogger().debug(String.valueOf(level.getBlockState(pos.above()).is(this)));
        if (!itemInHand.isEmpty() || !player.isSecondaryUseActive() || !hasLid && level.getBlockState(pos.above()).is(this)) {
            if (itemInHand.is(ModItems.STEAMER)) {
                Item var13 = itemInHand.getItem();
                if (var13 instanceof SteamerItem) {
                    SteamerItem steamerItem = (SteamerItem)var13;
                    BlockState blockState = state;
                    while (blockState.is(ModBlocks.STEAMER) && !blockState.getValue(SteamerBlock.HAS_LID) && !blockState.getValue(SteamerBlock.HALF)){
                        pos = pos.above();
                        blockState = level.getBlockState(pos);
                    }
                    InteractionResult place = null;
                    if(blockState.canBeReplaced() || (blockState.is(ModBlocks.STEAMER) && blockState.getValue(SteamerBlock.HALF) && !blockState.getValue(SteamerBlock.HAS_LID))){
                        place = steamerItem.place(new BlockPlaceContext(player, hand, itemInHand, hitResult.withDirection(Direction.UP).withPosition(pos)));
                    }else {
                        place = steamerItem.place(new BlockPlaceContext(player, hand, itemInHand, hitResult.withDirection(Direction.DOWN).withPosition(pos)));
                    }

                    if (place.consumesAction()) {
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }

                    return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }

                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                BlockEntity place = level.getBlockEntity(pos);
                if (place instanceof ISteamer) {
                    ISteamer steamer = (ISteamer)place;
                    if (steamer.placeFood(level, player, itemInHand)) {
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }

                    if (steamer.takeFood(level, player)) {
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                }

                return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
            }
        } else {
            level.setBlock(pos, (BlockState)state.setValue(SteamerBlock.HAS_LID, !hasLid), 3);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
    }

    @Shadow
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return null;
    }

    @Shadow
    private boolean shouldHasBase(LevelAccessor level, BlockPos pos) {
        return false;
    }

    @Shadow
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
    }

    @Shadow
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Shadow
    public FluidState getFluidState(BlockState state) {
        return (Boolean)state.getValue(SteamerBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Shadow
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    @Shadow
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return null;
    }

    @Shadow
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return (BlockState)pState.setValue(SteamerBlock.FACING, pRot.rotate((Direction)pState.getValue(SteamerBlock.FACING)));
    }

    @Shadow
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.rotate(pMirror.getRotation((Direction)pState.getValue(SteamerBlock.FACING)));
    }

    @Shadow
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SteamerBlockEntity(pos, state);
    }

    @Shadow
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder lootParamsBuilder) {
        return null;
    }
}
