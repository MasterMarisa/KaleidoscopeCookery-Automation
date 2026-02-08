package com.mastermarisa.kaleidoscopecookery_automation.init;

import com.mastermarisa.kaleidoscopecookery_automation.KaleidoscopeCookeryAutomation;
import com.mastermarisa.kaleidoscopecookery_automation.arm.type.*;
import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public interface InitArmInteractionPointTypes {
    DeferredRegister<ArmInteractionPointType> REGISTER = DeferredRegister.create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, KaleidoscopeCookeryAutomation.MOD_ID);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> STOCKPOT_POINT = REGISTER.register("stockpot", StockPotType::new);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> POT_POINT = REGISTER.register("pot", PotType::new);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> STEAMER_POINT = REGISTER.register("steamer", SteamerType::new);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> FRUIT_BASKET_POINT = REGISTER.register("fruit_basket", FruitBasketType::new);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> SHAWARMA_SPIT_POINT = REGISTER.register("shawarma_spit", ShawarmaSpitType::new);

    DeferredHolder<ArmInteractionPointType, ArmInteractionPointType> TABLE_POINT = REGISTER.register("table", TableType::new);

    static void register(IEventBus mod) {
        REGISTER.register(mod);
    }
}
