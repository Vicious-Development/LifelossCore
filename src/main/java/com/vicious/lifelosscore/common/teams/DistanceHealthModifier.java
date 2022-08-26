package com.vicious.lifelosscore.common.teams;

import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public class DistanceHealthModifier extends AttributeModifier {
    public DistanceHealthModifier(double effect) {
        super(((ILLGlobalData)SyncableGlobalData.getInstance()).getMaxHealthAttributeModUUID(), "Distance Health", effect, Operation.MULTIPLY_TOTAL);
    }
}
