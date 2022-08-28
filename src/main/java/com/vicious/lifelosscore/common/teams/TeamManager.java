package com.vicious.lifelosscore.common.teams;

import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.lifelosscore.api.ILLPlayerData;
import com.vicious.lifelosscore.common.LLCFG;
import com.vicious.lifelosscore.common.network.CPacketTeamInfo;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncablePlayerData;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Locale;
import java.util.UUID;

public class TeamManager {
    public static Team createTeam(String name){
        Team team = new Team(name);
        SyncableGlobalData.getInstance().executeAs(ILLGlobalData.class,(gd)-> gd.addTeam(team));
        return team;
    }
    public static void deleteTeam(Team team){
        SyncableGlobalData.getInstance().executeAs(ILLGlobalData.class,(gd)-> gd.removeTeam(team));
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(!LLCFG.getInstance().teamMode.getBoolean()) return;
        Player p = event.getEntity();
        if(p instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                Team t = getTeam(pd.getTeamID());
                if(t != null){
                    if(t.isInTeam(sp)) {
                        t.onLogin(sp);
                        LLNetwork.getInstance().sendToPlayer(sp,new CPacketTeamInfo(t));
                    }
                    else{
                        LifelossChatMessage.from(ChatFormatting.RED,"<1lifeloss.nolongerinteam>",t.getName()).send(sp);
                        pd.setTeamID(null);
                    }
                }
                else if(pd.getTeamID() != null){
                    pd.setTeamID(null);
                    LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<lifeloss.teamgone>").send(sp);
                }
            },ILLPlayerData.class);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        if(!LLCFG.getInstance().teamMode.getBoolean()) return;
        Player p = event.getEntity();
        if(p instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                Team t = getTeam(pd.getTeamID());
                if(t != null){
                    t.onLogout(sp);
                }
            },ILLPlayerData.class);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClone(PlayerEvent.Clone event){
        if(event.isWasDeath()) {
            SyncablePlayerData.executeIfPresent(event.getEntity(), (pd) -> {
                if (pd.hasTeam()) {
                    getTeam(pd.getTeamID()).onClone((ServerPlayer) event.getEntity(), (ServerPlayer) event.getOriginal());
                }
            }, ILLPlayerData.class);
        }
    }
    public static void tick(){
        ILLGlobalData.getInstance().getTeams().forEach(Team::tick);
    }

    public static Team getTeam(UUID id) {
        return ILLGlobalData.getInstance().getTeams().get(id);
    }
    public static Team getTeam(String name){
        return ILLGlobalData.getInstance().getTeams().get(name.toLowerCase(Locale.ROOT));
    }
}
