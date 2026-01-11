package com.mastermarisa.kaleidoscopecookeryautomation.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import javax.annotation.Nullable;
import java.util.Map;

@Mixin(RecipeManager.class)
public interface RecipeManagerAccessor {
    @Accessor("byName")
    Map<ResourceLocation, RecipeHolder<?>> getByName();

    @Nullable
    @Invoker("byKeyTyped")
    <T extends Recipe<?>> RecipeHolder<T> invokeByKeyTyped(RecipeType<T> type, ResourceLocation name);
}
