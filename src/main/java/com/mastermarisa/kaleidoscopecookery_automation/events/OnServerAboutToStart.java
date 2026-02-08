package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.mastermarisa.kaleidoscopecookery_automation.utils.RecipeUtils;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

public class OnServerAboutToStart {
    @SubscribeEvent
    public static void onServerAboutToStart(ServerAboutToStartEvent event){
        MinecraftServer server = event.getServer();
        RecipeUtils.setRecipeManager(server.getRecipeManager());
        RecipeUtils.processRecipes();
    }
}
