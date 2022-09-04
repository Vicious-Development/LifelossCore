package com.vicious.lifelosscore.common.util;

import com.vicious.viciouscore.common.util.server.BetterChatMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class LifelossChatMessage extends BetterChatMessage {
    public LifelossChatMessage(List<Object> objects) {
        this(objects.toArray());
    }

    private LifelossChatMessage(Object... objects) {
        super(objects);
        MutableComponent c1 = BetterChatMessage.from(ChatFormatting.DARK_GREEN, ChatFormatting.BOLD, "[", ChatFormatting.RESET, ChatFormatting.GREEN, "LL", ChatFormatting.DARK_GREEN, ChatFormatting.BOLD, "] ", ChatFormatting.RESET).component;
        c1.append(component);
        component = c1;
    }
    
    public static LifelossChatMessage from(Object... objects){
        return new LifelossChatMessage(objects);
    }
}
