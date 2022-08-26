package com.vicious.lifelosscore.api;

import com.vicious.viciouscore.common.capability.VCCapabilities;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.util.FuckLazyOptionals;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

public interface ILLPlayerData {
    static boolean hasTeam(CommandSourceStack source) {
        if(source.getEntity() instanceof Player prov){
            if(FuckLazyOptionals.getOrNull(prov.getCapability(VCCapabilities.PLAYERDATA)) instanceof ILLPlayerData pd){
                return pd.hasTeam();
            }
        }
        //Technically all non-players belong to the hypothetical team: "Minecraft".
        return true;
    }

    UUID getTeamInvite();
    void setTeamInvite(UUID id);
    default boolean hasTeamInvite(){
        return getTeamInvite() != null;
    }

    int getLives();
    void setLives(int lives);
    UUID getTeamID();
    void setTeamID(UUID id);
    default boolean hasTeam(){
        return getTeamID() != null;
    }

    default void growLives(int lives){
        setLives(getLives()+lives);
    }
    default void shrinkLives(int lives){
        setLives(getLives()-lives);
    }
    default boolean reduceLivesToOne(int shrinkAmount){
        int lives = getLives();
        if(lives == 1) return true;
        else {
            shrinkLives(shrinkAmount);
            if(getLives() < 1) setLives(1);
            return false;
        }
    }
    default boolean isDead(){
        if(SyncableGlobalData.getInstance() instanceof ILLGlobalData gd){
            return getLives() <= 0 || gd.kyraFailed();
        }
        return getLives() <= 0;
    }
    static ILLPlayerData get(Player plr){
        return (ILLPlayerData) FuckLazyOptionals.getOrNull(plr.getCapability(VCCapabilities.PLAYERDATA));
    }

    void sendLives();
}
