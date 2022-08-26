package com.vicious.lifelosscore.common.teams;

import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import com.vicious.viciouscore.common.util.server.BetterChatMessage;
import net.minecraft.ChatFormatting;

public enum LeaveReason {
    LEFT("<1lifeloss.leftteam>"),
    KICKED("<2lifeloss.kickedteam>");
    final String translationKey;
    LeaveReason(String key){
        this.translationKey=key;
    }
    public BetterChatMessage getMessage(Object... components){
        if(components.length == 1) return LifelossChatMessage.from(ChatFormatting.BLUE,translationKey,components[0]);
        if(components.length == 2) return LifelossChatMessage.from(ChatFormatting.BLUE,translationKey,components[0],components[1]);
        return LifelossChatMessage.from(ChatFormatting.BLUE,translationKey,components);
    }
}
