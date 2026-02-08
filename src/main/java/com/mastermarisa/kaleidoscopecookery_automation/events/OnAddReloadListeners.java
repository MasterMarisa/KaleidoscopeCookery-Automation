package com.mastermarisa.kaleidoscopecookery_automation.events;

import com.mastermarisa.kaleidoscopecookery_automation.utils.RecipeProcessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

public class OnAddReloadListeners {
    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new RecipeProcessor());
    }
}
