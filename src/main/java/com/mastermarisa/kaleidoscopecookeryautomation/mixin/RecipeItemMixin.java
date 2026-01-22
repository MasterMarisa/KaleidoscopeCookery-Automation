package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.block.kitchen.PotBlock;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.init.registry.FoodBiteRegistry;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.github.ysbbbbbb.kaleidoscopecookery.util.ItemUtils;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IPotBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IStockpotBlockEntityAccess;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.*;

@Mixin({RecipeItem.class})
public class RecipeItemMixin extends BlockItem{

    public RecipeItemMixin() {
        super((Block) ModBlocks.RECIPE_BLOCK.get(), new Item.Properties());
    }

    @Shadow
    public Component getName(ItemStack pStack) {
        return super.getName(pStack);
    }

    @Shadow
    public InteractionResult useOn(UseOnContext context){
        return InteractionResult.PASS;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private InteractionResult onPutRecipe(BlockEntity blockEntity, Player player, ItemStack itemInHand) {
        RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemInHand);
        if (record == null) {
            return InteractionResult.PASS;
        } else {
            if (blockEntity instanceof PotBlockEntity) {
                PotBlockEntity pot = (PotBlockEntity)blockEntity;
                if (pot.getStatus() == 0 && (Boolean)pot.getBlockState().getValue(PotBlock.HAS_OIL) && record.type().equals(RecipeItem.POT) && !player.isSecondaryUseActive()) {
                    List<ItemStack> inputs = pot.getInputs().stream().filter((s) -> !s.isEmpty()).toList();
                    if (!inputs.isEmpty()) {
                        return InteractionResult.PASS;
                    }

                    return this.handlePutRecipe(player, record, () -> pot.addAllIngredients(record.input(), player));
                }else if(record.type().equals(RecipeItem.POT) && player.isSecondaryUseActive()){
                    ((IPotBlockEntityAccess)pot).setArmRecipe(pot.getLevel(),player,itemInHand);
                }
            }

            if (blockEntity instanceof StockpotBlockEntity) {
                StockpotBlockEntity stockpot = (StockpotBlockEntity)blockEntity;
                if (stockpot.getStatus() == 1 && record.type().equals(RecipeItem.STOCKPOT) && !player.isSecondaryUseActive()) {
                    List<ItemStack> inputs = stockpot.getInputs().stream().filter((s) -> !s.isEmpty()).toList();
                    if (!inputs.isEmpty()) {
                        return InteractionResult.PASS;
                    }

                    return this.handlePutRecipe(player, record, () -> stockpot.addAllIngredients(record.input(), player));
                }else if(record.type().equals(RecipeItem.STOCKPOT) && player.isSecondaryUseActive()){
                    ((IStockpotBlockEntityAccess)stockpot).setArmRecipe(stockpot.getLevel(),player,itemInHand);
                }
            }

            return InteractionResult.PASS;
        }
    }

    @Shadow
    private @NotNull InteractionResult handlePutRecipe(Player player, RecipeItem.RecipeRecord record, Runnable success) {
        return InteractionResult.PASS;
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    private InteractionResult onRecordRecipe(Level level, Player player, BlockEntity blockEntity, RecipeManager recipeManager, ItemStack itemInHand, InteractionHand hand) {
        if (blockEntity instanceof PotBlockEntity pot) {
            if (pot.getStatus() == 0) {
                List<ItemStack> inputs = pot.getInputs().stream().filter((s) -> !s.isEmpty()).toList();
                if (inputs.isEmpty()) {
                    return InteractionResult.PASS;
                }

                ItemStack recordStack = itemInHand.copyWithCount(1);
                int count = itemInHand.getCount();
                if (count > 1) {
                    ItemStack returnStack = itemInHand.copyWithCount(count - 1);
                    ItemUtils.getItemToLivingEntity(player, returnStack);
                }

                recipeManager.getRecipeFor(ModRecipes.POT_RECIPE, pot.getContainer(), level).ifPresentOrElse((recipe) -> {
                    PotRecipe potRecipe = recipe.value();
                    ItemStack resultItem = potRecipe.getResultItem(level.registryAccess());
                    RecipeItem.setRecipe(recordStack, new RecipeItem.RecipeRecord(inputs, resultItem, RecipeItem.POT));
                    ItemStack[] carriers = potRecipe.carrier().getItems();
                    if(carriers.length > 0 && carriers[0] != null && !carriers[0].isEmpty()){
                        CompoundTag tag = new CompoundTag();
                        ItemStack carrier = new ItemStack(carriers[0].getItem(),resultItem.getCount());
                        tag.put("carrier_item",carrier.saveOptional(level.registryAccess()));
                        recordStack.set(ModDataComponents.CARRIER_ITEMS,tag);
                    }
                }, () -> {
                    ItemStack instance = FoodBiteRegistry.getItem(FoodBiteRegistry.SUSPICIOUS_STIR_FRY).getDefaultInstance();
                    RecipeItem.setRecipe(recordStack, new RecipeItem.RecipeRecord(inputs, instance, RecipeItem.POT));
                    CompoundTag tag = new CompoundTag();
                    ItemStack carrier = new ItemStack(Items.BOWL);
                    tag.put("carrier_item",carrier.saveOptional(level.registryAccess()));
                    recordStack.set(ModDataComponents.CARRIER_ITEMS,tag);
                });
                player.setItemInHand(hand, recordStack);
                return InteractionResult.SUCCESS;
            }
        }

        if (blockEntity instanceof StockpotBlockEntity stockpot) {
            if (stockpot.getStatus() == 1) {
                List<ItemStack> inputs = stockpot.getInputs().stream().filter((s) -> !s.isEmpty()).toList();
                if (inputs.isEmpty()) {
                    return InteractionResult.PASS;
                }

                ItemStack recordStack = itemInHand.copyWithCount(1);
                int count = itemInHand.getCount();
                if (count > 1) {
                    ItemStack returnStack = itemInHand.copyWithCount(count - 1);
                    ItemUtils.getItemToLivingEntity(player, returnStack);
                }

                recipeManager.getRecipeFor(ModRecipes.STOCKPOT_RECIPE, stockpot.getContainer(), level).ifPresentOrElse((recipe) -> {
                    StockpotRecipe stockpotRecipe = recipe.value();
                    ItemStack resultItem = stockpotRecipe.getResultItem(level.registryAccess());
                    RecipeItem.setRecipe(recordStack, new RecipeItem.RecipeRecord(inputs, resultItem, RecipeItem.STOCKPOT));
                    ItemStack[] carriers = stockpotRecipe.carrier().getItems();
                    if(carriers.length > 0 && carriers[0] != null && !carriers[0].isEmpty()){
                        CompoundTag tag = new CompoundTag();
                        ItemStack carrier = new ItemStack(carriers[0].getItem(),resultItem.getCount());
                        tag.put("carrier_item",carrier.saveOptional(level.registryAccess()));
                        recordStack.set(ModDataComponents.CARRIER_ITEMS,tag);
                    }
                }, () -> {
                    ItemStack instance = Items.SUSPICIOUS_STEW.getDefaultInstance();
                    RecipeItem.setRecipe(recordStack, new RecipeItem.RecipeRecord(inputs, instance, RecipeItem.STOCKPOT));
                    CompoundTag tag = new CompoundTag();
                    ItemStack carrier = new ItemStack(Items.BOWL);
                    tag.put("carrier_item",carrier.saveOptional(level.registryAccess()));
                    recordStack.set(ModDataComponents.CARRIER_ITEMS,tag);
                });
                player.setItemInHand(hand, recordStack);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    @Shadow
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return Optional.empty();
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.kaleidoscope_cookery.recipe_item").withStyle(ChatFormatting.GRAY));
        if(stack.get(ModDataComponents.RECIPE_ADDRESS) != null){
            tooltip.add(Component.translatable("tooltip.kaleidoscopecookery_automation.recipe_item.address").append(" : " + stack.get(ModDataComponents.RECIPE_ADDRESS)).withStyle(ChatFormatting.GRAY));
        }
    }
}
