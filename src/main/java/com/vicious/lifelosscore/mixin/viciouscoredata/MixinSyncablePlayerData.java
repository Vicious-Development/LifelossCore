package com.vicious.lifelosscore.mixin.viciouscoredata;

import com.vicious.lifelosscore.api.ILLPlayerData;
import com.vicious.lifelosscore.common.network.CPacketSendLives;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncablePlayerData;
import com.vicious.viciouscore.common.data.structures.SyncablePrimitive;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

import java.util.UUID;

@Mixin(SyncablePlayerData.class)
public class MixinSyncablePlayerData extends MixinSyncableAttachableCompound<Player> implements ILLPlayerData {
    public SyncablePrimitive<Integer> lives = add(new SyncablePrimitive<>("lives",3));
    public SyncablePrimitive<UUID> teamID = add(new SyncablePrimitive<>("teamID",null,UUID.class));
    public SyncablePrimitive<UUID> teamInvite = add(new SyncablePrimitive<>("teamInvite",null,UUID.class));


    @Override
    public UUID getTeamInvite() {
        return teamInvite.value;
    }

    @Override
    public void setTeamInvite(UUID id) {
        teamInvite.setValue(id);
    }

    @Override
    public int getLives() {
        return lives.value;
    }

    @Override
    public void setLives(int lives) {
        this.lives.setValue(lives);
        sendLives();
    }

    @Override
    public UUID getTeamID() {
        return teamID.value;
    }

    @Override
    public void setTeamID(UUID id) {
        teamID.setValue(id);
    }

    @Override
    public void sendLives() {
        if (getHolder() instanceof ServerPlayer sp) LLNetwork.getInstance().sendToPlayer(sp, new CPacketSendLives(lives.value));
    }
}
