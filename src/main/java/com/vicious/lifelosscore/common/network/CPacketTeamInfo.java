package com.vicious.lifelosscore.common.network;

import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.common.teams.Team;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketTeamInfo extends LLPacket {
    private final String teamName;
    public CPacketTeamInfo(Team t){
        teamName=t.getName();
    }

    @Override
    public boolean handleOnClient() {
        return true;
    }

    public CPacketTeamInfo(FriendlyByteBuf buf){
        teamName=buf.readUtf();
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeUtf(teamName);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ClientData.teamName=teamName;
    }
}
