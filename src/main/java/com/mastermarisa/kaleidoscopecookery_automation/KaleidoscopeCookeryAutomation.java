package com.mastermarisa.kaleidoscopecookery_automation;

import com.mastermarisa.kaleidoscopecookery_automation.data.DataGenerators;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitArmInteractionPointTypes;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitAttachments;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitDataComponents;
import com.mastermarisa.kaleidoscopecookery_automation.init.InitEvents;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;

@Mod(KaleidoscopeCookeryAutomation.MOD_ID)
public class KaleidoscopeCookeryAutomation {
    public static final String MOD_ID = "kaleidoscopecookery_automation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KaleidoscopeCookeryAutomation(IEventBus modEventBus, ModContainer modContainer) {
        InitArmInteractionPointTypes.register(modEventBus);
        InitAttachments.register(modEventBus);
        InitDataComponents.register(modEventBus);
        InitEvents.register(NeoForge.EVENT_BUS);

        modEventBus.register(DataGenerators.class);
    }
}
