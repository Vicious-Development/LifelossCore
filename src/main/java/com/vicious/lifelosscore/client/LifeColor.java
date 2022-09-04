package com.vicious.lifelosscore.client;

import net.minecraft.ChatFormatting;

public class LifeColor {
    public static ChatFormatting of(int lives){
        if(lives > 10) return ChatFormatting.DARK_GREEN;
        else if(lives > 7) return ChatFormatting.GREEN;
        else if(lives > 4) return ChatFormatting.YELLOW;
        else if(lives > 2) return ChatFormatting.GOLD;
        else if(lives > 1) return ChatFormatting.RED;
        else if(lives > 0) return ChatFormatting.DARK_RED;
        else return ChatFormatting.BLACK;
    }
}
