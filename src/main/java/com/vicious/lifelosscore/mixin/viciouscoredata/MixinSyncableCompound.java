package com.vicious.lifelosscore.mixin.viciouscoredata;

import com.vicious.viciouscore.common.data.structures.SyncableCompound;
import com.vicious.viciouscore.common.data.structures.SyncableValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = SyncableCompound.class)
public abstract class MixinSyncableCompound {
    @Shadow(remap = false)
    public <V extends SyncableValue<?>> V add(V val){
        return null;
    }
}
