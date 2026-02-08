package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitDataComponents;
import com.mastermarisa.kaleidoscopecookery_automation.utils.RecipeUtils;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class PackageSender {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        if (level.isClientSide()) return;

        Player player = event.getEntity();
        ItemStack itemStack = event.getItemStack();
        BlockEntity blockEntity = level.getBlockEntity(event.getHitVec().getBlockPos());
        if (!itemStack.is(ModItems.RECIPE_ITEM.get())) return;

        if(blockEntity instanceof StockTickerBlockEntity ticker && RecipeItem.hasRecipe(itemStack)) {
            RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemStack);
            List<ItemStack> stacks = new ArrayList<>(record.input());
            if (record.type().equals(RecipeItem.STOCKPOT)) {
                StockpotRecipe recipe = RecipeUtils.getStockpotRecipe(record).get().value();
                stacks.add(recipe.carrier().getItems()[0]);
            } else if (record.type().equals(RecipeItem.POT)) {
                PotRecipe recipe = RecipeUtils.getPotRecipe(record).get().value();
                if (recipe.carrier().getItems().length > 0)
                    stacks.add(recipe.carrier().getItems()[0]);
            }
            List<BigItemStack> requests = new ArrayList<>(stacks.stream().map(s->new BigItemStack(s,s.getCount())).toList());
            String address = itemStack.get(InitDataComponents.RECIPE_ADDRESS);
            PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.simple(requests);
            LogisticsManager.broadcastPackageRequest(ticker.behaviour.freqId, LogisticallyLinkedBehaviour.RequestType.RESTOCK,encodedRequest,null,address);
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);
        } else if (blockEntity instanceof SignBlockEntity sign) {
            if (!player.isSecondaryUseActive()) return;
            SignText text = sign.getText(true);
            StringBuilder address = new StringBuilder();

            for(Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address.append(string.trim()).append(" ");
            }

            text = sign.getText(false);

            for(Component component : text.getMessages(false)) {
                String string = component.getString();
                if (!string.isBlank())
                    address.append(string.trim()).append(" ");
            }

            if (!address.toString().isBlank()) {
                itemStack.set(InitDataComponents.RECIPE_ADDRESS, address.toString().trim());
                player.getCooldowns().addCooldown(itemStack.getItem(),10);
            }
        } else if (blockEntity instanceof PackagePortBlockEntity port) {
            if (!player.isSecondaryUseActive()) return;
            itemStack.set(InitDataComponents.RECIPE_ADDRESS,port.addressFilter);
            player.getCooldowns().addCooldown(itemStack.getItem(),10);
        }
    }
}
