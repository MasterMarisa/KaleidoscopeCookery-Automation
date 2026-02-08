package com.mastermarisa.kaleidoscopecookery_automation.attachment;

import com.mastermarisa.kaleidoscopecookery_automation.utils.RecipeUtils;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class StoredRecipe implements INBTSerializable<CompoundTag> {
    @Nullable
    public RecipeHolder<? extends Recipe<?>> holder;

    public static StoredRecipe of(RecipeHolder<? extends Recipe<?>> holder) {
        StoredRecipe storedRecipe = new StoredRecipe();
        storedRecipe.holder = holder;
        return storedRecipe;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        if (holder != null) {
            tag.putString("id",holder.id().toString());
        }

        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        if (tag.contains("id")) {
            holder = RecipeUtils.getRecipeManager().byKey(ResourceLocation.parse(tag.getString("id"))).get();
        }
    }

    public static final AttachmentType<StoredRecipe> TYPE = AttachmentType.serializable(StoredRecipe::new).build();
}
