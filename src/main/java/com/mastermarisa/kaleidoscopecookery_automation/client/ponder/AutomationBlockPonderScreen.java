package com.mastermarisa.kaleidoscopecookery_automation.client.ponder;

import com.github.ysbbbbbb.kaleidoscopecookery.KaleidoscopeCookery;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene.PotScenes;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene.ShawarmaSpitScenes;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene.SteamerScenes;
import com.mastermarisa.kaleidoscopecookery_automation.client.ponder.scene.StockpotScenes;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class AutomationBlockPonderScreen {
    public static void register(PonderSceneRegistrationHelper<ResourceLocation> helper){
        helper.forComponents(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"stockpot"))
                .addStoryBoard("stockpot/stockpot_automation", StockpotScenes::introduction, (entry) -> {
                    entry.orderAfter("ponderforkc", "stockpot");
                });

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"pot"))
                .addStoryBoard("pot/pot_automation", PotScenes::introduction, (entry) -> {
                    entry.orderAfter("ponderforkc", "pot");
                });

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"steamer"))
                .addStoryBoard("steamer/steamer_automation", SteamerScenes::introduction, (entry) -> {
                    entry.orderAfter("ponderforkc", "steamer_1");
                });

        helper.forComponents(ResourceLocation.fromNamespaceAndPath(KaleidoscopeCookery.MOD_ID,"shawarma_spit"))
                .addStoryBoard("shawarma_spit/shawarma_spit_automation", ShawarmaSpitScenes::introduction, (entry) -> {
                    entry.orderAfter("ponderforkc", "shawarma_spit");
                });
    }
}
