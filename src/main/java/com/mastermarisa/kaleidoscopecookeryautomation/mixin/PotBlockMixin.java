package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.api.blockentity.IPot;
import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModSoundType;
import com.github.ysbbbbbb.kaleidoscopecookery.init.tag.TagMod;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IPotBlockEntityAccess;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.List;

@Mixin({PotBlock.class})
public class PotBlockMixin extends HorizontalDirectionalBlock implements EntityBlock, SimpleWaterloggedBlock {

    public PotBlockMixin() {
        super(Properties.of().mapColor(MapColor.METAL).sound(ModSoundType.POT).noOcclusion().strength(1.5F, 6.0F));
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(FACING, Direction.SOUTH)).setValue(PotBlock.HAS_OIL, false)).setValue(PotBlock.SHOW_OIL, false)).setValue(PotBlock.WATERLOGGED, false)).setValue(PotBlock.HAS_BASE, false));
    }

    @Shadow
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return PotBlock.CODEC;
    }

    @Shadow
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor levelAccessor, BlockPos pos, BlockPos neighborPos) {
        return null;
    }

    @Shadow
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        } else {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof IPot)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                IPot pot = (IPot)blockEntity;
                ItemStack handItem = player.getItemInHand(hand);
                RandomSource random = level.random;
                if(((IPotBlockEntityAccess)pot).takeArmRecipe(level,player,handItem)){
                    return ItemInteractionResult.SUCCESS;
                }else if ((handItem.isEmpty() || handItem.is(TagMod.INGREDIENT_CONTAINER)) && pot.removeIngredient(level, player)) {
                    return ItemInteractionResult.SUCCESS;
                } else if (pot.takeOutProduct(level, player, player.getMainHandItem())) {
                    return ItemInteractionResult.SUCCESS;
                } else if (!pot.hasHeatSource(level)) {
                    this.sendBarMessage(player, "tip.kaleidoscope_cookery.pot.need_lit_stove");
                    return ItemInteractionResult.FAIL;
                } else if (!(Boolean)state.getValue(PotBlock.HAS_OIL)) {
                    if (pot.onPlaceOil(level, player, handItem)) {
                        return ItemInteractionResult.SUCCESS;
                    } else {
                        this.sendBarMessage(player, "tip.kaleidoscope_cookery.pot.need_oil");
                        return ItemInteractionResult.FAIL;
                    }
                } else if (handItem.is(TagMod.KITCHEN_SHOVEL)) {
                    if (level.random.nextDouble() < (double)0.25F) {
                        handItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                    }

                    pot.onShovelHit(level, player, handItem);
                    level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 1.0F + (random.nextFloat() - random.nextFloat()) * 0.8F);
                    return ItemInteractionResult.SUCCESS;
                } else {
                    return pot.addIngredient(level, player, handItem) ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
                }
            }
        }
    }

    @Unique
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder lootParamsBuilder) {
        List<ItemStack> drops = super.getDrops(state, lootParamsBuilder);
        BlockEntity parameter = (BlockEntity)lootParamsBuilder.getParameter(LootContextParams.BLOCK_ENTITY);
        if(parameter instanceof PotBlockEntity pot){
            if(!((IPotBlockEntityAccess)pot).getStoredRecipeItem().isEmpty()){
                drops.add(((IPotBlockEntityAccess)pot).getStoredRecipeItem());
            }
        }
        return drops;
    }

    @Shadow
    private void sendBarMessage(Player player, String key) {
    }

    @Shadow
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PotBlockEntity(pos, state);
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
        return (Boolean)state.getValue(PotBlock.WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Shadow
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    }

    @Shadow
    public VoxelShape getShape(BlockState state, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return null;
    }

    @Shadow
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
    }
}
