package com.mastermarisa.kaleidoscopecookery_automation.data;

import com.github.ysbbbbbb.kaleidoscopecookery.init.ModItems;
import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.concurrent.CompletableFuture;

@ParametersAreNonnullByDefault
public class TagItem extends ItemTagsProvider {
    public static final TagKey<Item> RESULT = ItemTags.create(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookeryAutomation.MOD_ID, "result"));

    public TagItem(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                   CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, KaleidoscopeCookeryAutomation.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(RESULT)
                .add(ModItems.BEEF_MEATBALL_SOUP.get());
    }
}
