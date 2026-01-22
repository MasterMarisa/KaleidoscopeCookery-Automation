package com.mastermarisa.kaleidoscopecookeryautomation.event;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModDataComponents;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.simibubi.create.content.logistics.packager.IdentifiedInventory;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.simibubi.create.content.logistics.packagerLink.LogisticsManager;
import com.simibubi.create.content.logistics.stockTicker.PackageOrderWithCrafts;
import com.simibubi.create.content.logistics.stockTicker.StockTickerBlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

public class ModEventHandler {
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        Level level = event.getLevel();
        Player player = event.getEntity();
        if(!level.isClientSide()){
            ItemStack itemStack = player.getMainHandItem();
            BlockEntity blockEntity = level.getBlockEntity(event.getHitVec().getBlockPos());
            if(blockEntity instanceof StockTickerBlockEntity entity){
                if(itemStack.is(ModItems.RECIPE_ITEM.get()) && RecipeItem.hasRecipe(itemStack)){
                    List<ItemStack> stacks = new ArrayList<>();
                    RecipeItem.RecipeRecord record = RecipeItem.getRecipe(itemStack);
                    for(ItemStack stack : record.input()){
                        stacks.add(stack.copy());
                    }
                    CompoundTag tag = itemStack.get(ModDataComponents.CARRIER_ITEMS);
                    if(tag != null && tag.contains("carrier_item")){
                        ItemStack carrier = ItemStack.parseOptional(level.registryAccess(),tag.getCompound("carrier_item")).copy();
                        stacks.add(carrier);
                    }
                    List<BigItemStack> requests = new ArrayList<>();
                    for(ItemStack stack : stacks){
                        requests.add(new BigItemStack(stack,stack.getCount()));
                    }
                    String address = itemStack.get(ModDataComponents.RECIPE_ADDRESS);
                    PackageOrderWithCrafts encodedRequest =  PackageOrderWithCrafts.simple(requests);
                    LogisticsManager.broadcastPackageRequest(entity.behaviour.freqId,LogisticallyLinkedBehaviour.RequestType.RESTOCK,encodedRequest,(IdentifiedInventory)null,address);
                    //LogisticallyLinkedBehaviour link = (LogisticallyLinkedBehaviour) BlockEntityBehaviour.get(level, event.getHitVec().getBlockPos(), LogisticallyLinkedBehaviour.TYPE);
                }
            } else if(player.isSecondaryUseActive() && blockEntity instanceof SignBlockEntity sign){
                SignText text = sign.getText(true);
                String address = "";

                for(Component component : text.getMessages(false)) {
                    String string = component.getString();
                    if (!string.isBlank()) {
                        address = address + string.trim() + " ";
                    }
                }

                text = sign.getText(false);

                for(Component component : text.getMessages(false)) {
                    String string = component.getString();
                    if (!string.isBlank()) {
                        address = address + string.trim() + " ";
                    }
                }

                if (!address.isBlank()) {
                    itemStack.set(ModDataComponents.RECIPE_ADDRESS,address.trim());
                    player.getCooldowns().addCooldown(itemStack.getItem(),10);
                }
            } else if(player.isSecondaryUseActive() && blockEntity instanceof PackagePortBlockEntity port){
                itemStack.set(ModDataComponents.RECIPE_ADDRESS,port.addressFilter);
                player.getCooldowns().addCooldown(itemStack.getItem(),10);
            }
        }
    }
}
