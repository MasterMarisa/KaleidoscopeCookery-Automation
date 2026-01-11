package com.mastermarisa.kaleidoscopecookeryautomation.registry;

import com.mastermarisa.kaleidoscopecookeryautomation.KaleidoscopeCookeryAutomation;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES;
    public static final Supplier<DataComponentType<CompoundTag>> CARRIER_ITEMS;
    public static final Supplier<DataComponentType<String>> RECIPE_ADDRESS;

    static {
        DATA_COMPONENT_TYPES = DeferredRegister.create(BuiltInRegistries.DATA_COMPONENT_TYPE, KaleidoscopeCookeryAutomation.MOD_ID);
        CARRIER_ITEMS = DATA_COMPONENT_TYPES.register("carrier_items",()-> DataComponentType.<CompoundTag>builder().persistent(CompoundTag.CODEC).networkSynchronized(ByteBufCodecs.COMPOUND_TAG).build());
        RECIPE_ADDRESS = DATA_COMPONENT_TYPES.register("recipe_address",()-> DataComponentType.<String>builder().persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8).build());
    }
}
