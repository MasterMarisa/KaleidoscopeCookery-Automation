package com.mastermarisa.kaleidoscopecookery_automation.utils;

import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.PotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.crafting.recipe.StockpotRecipe;
import com.github.ysbbbbbb.kaleidoscopecookery.init.ModRecipes;
import com.github.ysbbbbbb.kaleidoscopecookery.item.RecipeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class RecipeUtils {
    private static RecipeManager recipeManager;
    private static ConcurrentHashMap<String, LinkedList<RecipeHolder<StockpotRecipe>>> stockpotQuickMatch;
    private static ConcurrentHashMap<String, LinkedList<RecipeHolder<PotRecipe>>> potQuickMatch;

    public static void setRecipeManager(RecipeManager manager) { recipeManager = manager; }

    public static @Nullable RecipeManager getRecipeManager() { return recipeManager; }

    private static boolean matchesShapeless(List<ItemStack> itemStacks, List<StackPredicate> ingredients) {
        List<ItemStack> nonEmptyItems = itemStacks.stream().filter((s)-> !s.isEmpty()).toList();
        List<StackPredicate> remainingIngredients = new ArrayList<>(ingredients);
        if (nonEmptyItems.size() != remainingIngredients.size()) return false;

        for (ItemStack stack : nonEmptyItems) {
            boolean foundMatch = false;
            for (int i = 0; i < remainingIngredients.size(); i++) {
                StackPredicate ingredient = remainingIngredients.get(i);
                if (ingredient.test(stack)) {
                    remainingIngredients.remove(i);
                    foundMatch = true;
                    break;
                }
            }
            if (!foundMatch) return false;
        }

        return remainingIngredients.isEmpty();
    }

    public static Optional<RecipeHolder<StockpotRecipe>> getStockpotRecipe(RecipeItem.RecipeRecord record){
        if (record.type().equals(RecipeItem.STOCKPOT)){
            String key = EncodeUtils.encode(record.output()).toString();
            if (stockpotQuickMatch.containsKey(key)){
                LinkedList<RecipeHolder<StockpotRecipe>> linkedList = stockpotQuickMatch.get(key);
                if (linkedList.size() == 1)
                    return Optional.of(linkedList.getFirst());
                else {
                    for (RecipeHolder<StockpotRecipe> holder : linkedList){
                        List<StackPredicate> ingredients = holder.value().getIngredients().stream().filter(i-> !i.isEmpty()).map(StackPredicate::new).toList();
                        if (matchesShapeless(record.input(), ingredients))
                            return Optional.of(holder);
                    }
                }
            }
        }

        return Optional.empty();
    }

    public static Optional<RecipeHolder<PotRecipe>> getPotRecipe(RecipeItem.RecipeRecord record){
        if (record.type().equals(RecipeItem.POT)){
            String key = EncodeUtils.encode(record.output()).toString();
            if (potQuickMatch.containsKey(key)){
                LinkedList<RecipeHolder<PotRecipe>> linkedList = potQuickMatch.get(key);
                if (linkedList.size() == 1)
                    return Optional.of(linkedList.getFirst());
                else {
                    for (RecipeHolder<PotRecipe> holder : linkedList){
                        List<StackPredicate> ingredients = holder.value().getIngredients().stream().filter(i-> !i.isEmpty()).map(StackPredicate::new).toList();
                        if (matchesShapeless(record.input(), ingredients))
                            return Optional.of(holder);
                    }
                }
            }
        }

        return Optional.empty();
    }

    private static void processStockpotRecipes(RecipeManager manager){
        List<RecipeHolder<StockpotRecipe>> recipes = manager.getAllRecipesFor(ModRecipes.STOCKPOT_RECIPE);
        ConcurrentHashMap<String, LinkedList<RecipeHolder<StockpotRecipe>>> match = new ConcurrentHashMap<>();

        for (RecipeHolder<StockpotRecipe> holder : recipes){
            StockpotRecipe recipe = holder.value();
            String key = EncodeUtils.encode(recipe.result()).toString();
            if (!match.containsKey(key)) match.put(key,new LinkedList<>());
            match.get(key).add(holder);
        }

        stockpotQuickMatch = match;
    }

    private static void processPotRecipes(RecipeManager manager){
        List<RecipeHolder<PotRecipe>> recipes = manager.getAllRecipesFor(ModRecipes.POT_RECIPE);
        ConcurrentHashMap<String, LinkedList<RecipeHolder<PotRecipe>>> match = new ConcurrentHashMap<>();

        for (RecipeHolder<PotRecipe> holder : recipes){
            PotRecipe recipe = holder.value();
            String key = EncodeUtils.encode(recipe.result()).toString();
            if (!match.containsKey(key)) match.put(key,new LinkedList<>());
            match.get(key).add(holder);
        }

        potQuickMatch = match;
    }

    public static void processRecipes() {
        if (recipeManager != null) {
            processStockpotRecipes(recipeManager);
            processPotRecipes(recipeManager);
        }
    }
}
