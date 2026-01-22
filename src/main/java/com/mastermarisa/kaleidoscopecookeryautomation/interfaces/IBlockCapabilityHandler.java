package com.mastermarisa.kaleidoscopecookeryautomation.interfaces;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public interface IBlockCapabilityHandler {
    public IItemHandler getHandler(@Nullable Direction side);
}
