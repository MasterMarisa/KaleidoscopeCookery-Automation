package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

public class TooltipHandler {
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        List<Component> tooltip = event.getToolTip();
        if (itemStack.is(ModItems.RECIPE_ITEM))
            if (itemStack.get(InitDataComponents.RECIPE_ADDRESS) != null)
                tooltip.add(Component.translatable("tooltip.kaleidoscopecookery_automation.recipe_item.address")
                        .append(" : " + itemStack.get(InitDataComponents.RECIPE_ADDRESS)).withStyle(ChatFormatting.GRAY));
    }
}
