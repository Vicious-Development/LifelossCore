package com.vicious.lifelosscore.common.network;

import com.vicious.lifelosscore.client.gui.ClientData;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketSendTeamCenter extends LLPacket {
    private final BlockPos pos;

    @Override
    public boolean handleOnClient() {
        return true;
    }

    public CPacketSendTeamCenter(BlockPos p){
        this.pos = p;
    }
    public CPacketSendTeamCenter(FriendlyByteBuf buf){
        this.pos = new BlockPos(buf.readInt(),buf.readInt(),buf.readInt());
    }
    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(pos.getX());
        friendlyByteBuf.writeInt(pos.getY());
        friendlyByteBuf.writeInt(pos.getZ());
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ClientData.setCenter(pos);
    }
}
