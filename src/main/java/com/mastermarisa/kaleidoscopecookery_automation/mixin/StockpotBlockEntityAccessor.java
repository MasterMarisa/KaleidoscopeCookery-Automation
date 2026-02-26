package com.mastermarisa.kaleidoscopecookery_automation.mixin;

import com.github.ysbbbbbb.kaleidoscopecookery.blockentity.kitchen.StockpotBlockEntity;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.container.StockpotInput;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({StockpotBlockEntity.class})
public interface StockpotBlockEntityAccessor {
    @Accessor
    void setStatus(int status);

    @Invoker("applyRecipe")
    void invokeApplyRecipe(Level level, StockpotInput container, RecipeHolder<StockpotRecipe> recipe);
}
