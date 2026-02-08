package com.mastermarisa.kaleidoscopecookery_automation.init;

import com.mastermarisa.kaleidoscopecookery_automation.events.*;
import net.neoforged.bus.api.IEventBus;

public interface InitEvents {
    static void register(IEventBus bus){
        bus.register(OnAddReloadListeners.class);
        bus.register(OnServerAboutToStart.class);
        bus.register(RecipeSetter.class);
        bus.register(PackageSender.class);
        bus.register(TooltipHandler.class);
        bus.register(SteamerPlacementHelper.class);
    }
}
