package com.vicious.lifelosscore.common.teams;

import com.vicious.viciouscore.common.phantom.WorldPos;
import com.vicious.viciouscore.common.util.server.ServerHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

public class DimensionalDistanceCalculator {
    public static final Map<String,Integer> distanceMultipliers = new HashMap<>();
    static{
        distanceMultipliers.put(Level.NETHER.location().toString(),8);
    }

    public static BlockPos calculateTruePos(WorldPos pos) {
        int multiplier = getMultiplier((ServerLevel) pos.level);
        if(multiplier != 1){
            return new BlockPos(pos.position.getX()*multiplier,pos.position.getY()*multiplier,pos.position.getZ()*multiplier);
        } else return pos.position;
    }

    public static int getMultiplier(ServerLevel l){
        String key = ServerHelper.getLevelName(l);
        Integer i = distanceMultipliers.get(key);
        if(i == null) return 1;
        return i;
    }
}
