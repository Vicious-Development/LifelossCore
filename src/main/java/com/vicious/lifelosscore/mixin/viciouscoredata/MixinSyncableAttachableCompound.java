package com.vicious.lifelosscore.mixin.viciouscoredata;

import com.vicious.viciouscore.common.data.implementations.attachable.SyncableAttachableCompound;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SyncableAttachableCompound.class)
public abstract class MixinSyncableAttachableCompound<T extends ICapabilityProvider> extends MixinSyncableCompound {
    @Shadow(remap = false) public T getHolder(){return null;}
}
