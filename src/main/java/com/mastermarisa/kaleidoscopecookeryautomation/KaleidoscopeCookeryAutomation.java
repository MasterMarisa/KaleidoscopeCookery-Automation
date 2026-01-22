package com.mastermarisa.kaleidoscopecookeryautomation;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModBlocks;
import com.mastermarisa.kaleidoscopecookeryautomation.event.ModEventHandler;
import com.mastermarisa.kaleidoscopecookeryautomation.interfaces.IBlockCapabilityHandler;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModArmInteractionPointTypes;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModConfig;
import com.mastermarisa.kaleidoscopecookeryautomation.registry.ModDataComponents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;

@Mod(KaleidoscopeCookeryAutomation.MOD_ID)
public class KaleidoscopeCookeryAutomation {
    public static final String MOD_ID = "kaleidoscopecookery_automation";
    public static final Logger LOGGER = LogUtils.getLogger();

    public KaleidoscopeCookeryAutomation(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::registerCaps);
        ModArmInteractionPointTypes.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);

        NeoForge.EVENT_BUS.register(ModEventHandler.class);

        modContainer.registerConfig(net.neoforged.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
    }

    private void registerCaps(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.STEAMER_BE.get(),
                (be, side) -> ((IBlockCapabilityHandler)(be)).getHandler(side)
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlocks.FRUIT_BASKET_BE.get(),
                (be, side) -> ((IBlockCapabilityHandler)(be)).getHandler(side));
    }
}
