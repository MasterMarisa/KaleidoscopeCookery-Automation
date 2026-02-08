package com.mastermarisa.kaleidoscopecookery_automation.utils;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class RecipeProcessor implements PreparableReloadListener {
    @Override
    public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager resourceManager, ProfilerFiller preparationsProfiler,
                                          ProfilerFiller reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {

        return CompletableFuture.supplyAsync(() -> {
            preparationsProfiler.startTick();
            preparationsProfiler.push("preprocess_recipes");

            RecipeUtils.processRecipes();

            preparationsProfiler.pop();
            preparationsProfiler.endTick();
            return null;

        }, backgroundExecutor).thenCompose(barrier::wait).thenAcceptAsync((preparedData) -> {
            reloadProfiler.startTick();
            reloadProfiler.push("apply_preprocessed_recipes");

            reloadProfiler.pop();
            reloadProfiler.endTick();
        }, gameExecutor);
    }
}
