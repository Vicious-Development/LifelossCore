package com.vicious.lifelosscore.common.network;

import com.vicious.lifelosscore.client.gui.ClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketSendLossStart extends LLPacket{
    private final int lossStart;
    public CPacketSendLossStart(int start){
        this.lossStart=start;
    }
    public CPacketSendLossStart(FriendlyByteBuf buf){
        lossStart=buf.readInt();
    }

    @Override
    public boolean handleOnClient() {
        return true;
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(lossStart);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ClientData.lossStart=lossStart;
    }
}
