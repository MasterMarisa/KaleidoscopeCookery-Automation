package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.PotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import com.mastermarisa.kaleidoscopecookery_automation.attachment.StoredRecipe;
import com.mastermarisa.kaleidoscopecookery_automation.utils.RecipeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;


public class RecipeSetter {
    @SubscribeEvent
    public static void useItemOnBlock(UseItemOnBlockEvent event) {
        Player player = event.getPlayer();
        ItemStack itemStack = event.getItemStack();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();
        BlockEntity entity = level.getBlockEntity(pos);

        if (level.isClientSide()) return;
        if (entity == null) return;

        if (RecipeItem.hasRecipe(itemStack)) {
            RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemStack);
            if (record.type().equals(RecipeItem.STOCKPOT) && entity instanceof StockpotBlockEntity) {
                RecipeUtils.getStockpotRecipe(record).ifPresent(holder -> {
                    entity.setData(StoredRecipe.TYPE, StoredRecipe.of(holder));
                    event.setCanceled(true);
                    event.setCancellationResult(ItemInteractionResult.SUCCESS);
                    KaleidoscopeCookeryAutomation.LOGGER.debug("Stockpot");
                    if (player != null) player.sendSystemMessage(Component.translatable("message.kaleidoscopecookery_automation.recipe_set"));
                });
            } else if (record.type().equals(RecipeItem.POT) && entity instanceof PotBlockEntity) {
                RecipeUtils.getPotRecipe(record).ifPresent(holder -> {
                    entity.setData(StoredRecipe.TYPE, StoredRecipe.of(holder));
                    event.setCanceled(true);
                    event.setCancellationResult(ItemInteractionResult.SUCCESS);
                    KaleidoscopeCookeryAutomation.LOGGER.debug("Pot");
                    if (player != null) player.sendSystemMessage(Component.translatable("message.kaleidoscopecookery_automation.recipe_set"));
                });
            }
        }
    }
}
