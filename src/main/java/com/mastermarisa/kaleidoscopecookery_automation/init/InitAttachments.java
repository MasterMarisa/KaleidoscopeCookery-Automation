package com.mastermarisa.kaleidoscopecookery_automation.init;

import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import com.mastermarisa.kaleidoscopecookery_automation.attachment.StoredRecipe;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public interface InitAttachments {
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, KaleidoscopeCookeryAutomation.MOD_ID);

    Supplier<AttachmentType<StoredRecipe>> STORED_RECIPE = ATTACHMENT_TYPES.register("stored_recipe", () -> StoredRecipe.TYPE);

    static void register(IEventBus mod) {
        ATTACHMENT_TYPES.register(mod);
    }
}
