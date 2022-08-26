package com.vicious.lifelosscore.common.events;

import com.vicious.lifelosscore.common.teams.TeamManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class Ticker {
    @SubscribeEvent
    public static void onTick(TickEvent.ServerTickEvent event){
        if(event.phase == TickEvent.Phase.END){
            TeamManager.tick();
        }
    }
}
