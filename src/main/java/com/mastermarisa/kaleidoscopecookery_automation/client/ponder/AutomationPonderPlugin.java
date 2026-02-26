package com.mastermarisa.kaleidoscopecookery_automation.client.ponder;

import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.resources.ResourceLocation;

public class AutomationPonderPlugin implements PonderPlugin {
    @Override
    public String getModId(){
        return KaleidoscopeCookeryAutomation.MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper){
        AutomationBlockPonderScreen.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {

    }

    public static void init() {
        PonderIndex.addPlugin(new AutomationPonderPlugin());
    }
}
