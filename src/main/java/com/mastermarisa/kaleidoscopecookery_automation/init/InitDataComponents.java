package com.mastermarisa.kaleidoscopecookery_automation.init;

import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public interface InitDataComponents {
    DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, KaleidoscopeCookeryAutomation.MOD_ID);

    Supplier<DataComponentType<String>> RECIPE_ADDRESS = DATA_COMPONENT_TYPES.register("recipe_address",()-> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());

    static void register(IEventBus mod) {
        DATA_COMPONENT_TYPES.register(mod);
    }
}
