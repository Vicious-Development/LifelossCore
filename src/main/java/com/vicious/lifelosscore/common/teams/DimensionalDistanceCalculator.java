package com.vicious.lifelosscore.common.teams;

import com.vicious.viciouscore.common.phantom.WorldPos;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;

import java.util.HashMap;
import java.util.Map;

public class DimensionalDistanceCalculator {
    public static final Map<ResourceKey<DimensionType>,Integer> distanceMultipliers = new HashMap<>();
    static{
        distanceMultipliers.put(BuiltinDimensionTypes.NETHER, 8);
    }

    public static BlockPos calculateTruePos(WorldPos pos) {
        int multiplier = getMultiplier((ServerLevel) pos.level);
        if(multiplier != 1){
            return new BlockPos(pos.position.getX()*multiplier,pos.position.getY()*multiplier,pos.position.getZ()*multiplier);
        } else return pos.position;
    }

    public static int getMultiplier(ServerLevel l){
        Integer i = distanceMultipliers.get(l.dimensionTypeId());
        if(i == null) return 1;
        return i;
    }
}
