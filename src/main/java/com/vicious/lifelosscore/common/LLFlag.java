package com.vicious.lifelosscore.common;

import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.lifelosscore.common.network.CPacketSendGameFlags;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.viciouscore.common.util.server.ServerHelper;
import net.minecraft.server.level.ServerPlayer;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public enum LLFlag {
    TEAMSACTIVE(()->LLCFG.getInstance().teamMode.getBoolean()),
    KYRAMODEACTIVE(()->LLCFG.getInstance().kyraMode.getBoolean()),
    KYRAMODEFAILED(()-> ILLGlobalData.getInstance().kyraFailed()),
    LIVESACTIVE(()->LLCFG.getInstance().livesMode.getBoolean());

    final Supplier<Boolean> isActive;
    LLFlag(Supplier<Boolean> isActive){
        this.isActive=isActive;
    }

    public static void updateFlags(){
        active.clear();
        inactive.clear();
        for (LLFlag value : values()) {
            if(value.isActive.get()){
                active.add(value);
            }
            else inactive.add(value);
        }
        for (ServerPlayer player : ServerHelper.getPlayers()) {
            sendToPlayer(player);
        }
    }
    public static void sendToPlayer(ServerPlayer plr){
        LLNetwork.getInstance().sendToPlayer(plr, new CPacketSendGameFlags().active(active).inactive(inactive));
    }
    static final Set<LLFlag> active = EnumSet.noneOf(LLFlag.class);
    static final Set<LLFlag> inactive = EnumSet.noneOf(LLFlag.class);
}
