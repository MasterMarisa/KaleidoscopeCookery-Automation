package com.mastermarisa.kaleidoscopecookery_automation.client.event;

import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.AutomationPonderPlugin;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = KaleidoscopeCookeryAutomation.MOD_ID, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event){
        AutomationPonderPlugin.init();
    }
}
