package com.vicious.lifelosscore.client.gui;

import com.vicious.lifelosscore.common.LLFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;

import java.util.EnumSet;
import java.util.Set;

public class ClientData {
    public static Set<LLFlag> activeServerFlags = EnumSet.noneOf(LLFlag.class);
    public static int lives = 0;
    public static BlockPos centerPosition = new BlockPos(0,0,0);
    public static String teamName = null;
    public static int distToCenter = 0;
    public static int lossStart = -1;

    public static void setCenter(BlockPos pos){
        centerPosition = pos;
        if(Minecraft.getInstance().player != null) distToCenter = (int)Math.sqrt(centerPosition.distToCenterSqr(Minecraft.getInstance().player.position()));
        LifelossOverlay.getInstance().distance.regenerate();
    }

    public static boolean hasTeam(){
        return !(teamName == null || teamName.isEmpty());
    }

    public static boolean flagActive(LLFlag flag){
        return activeServerFlags.contains(flag);
    }

    public static String centerString() {
        return "(" + centerPosition.getX() + "," + centerPosition.getY() + "," + centerPosition.getZ() + ")";
    }

    public static float percentUnnacceptable() {
        return Math.min(1.0f,distToCenter/(float)lossStart);
    }

    public static float getHP() {
        if(Minecraft.getInstance().player == null) return 0;
        else return Minecraft.getInstance().player.getMaxHealth();
    }
}
