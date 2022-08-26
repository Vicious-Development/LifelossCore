package com.vicious.lifelosscore.common.network;

import com.vicious.lifelosscore.client.gui.ClientData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class CPacketSendLives extends LLPacket {
    private final int lives;
    public CPacketSendLives(int lives){
        this.lives = lives;
    }
    public CPacketSendLives(FriendlyByteBuf buf){
        this.lives=buf.readInt();
    }

    @Override
    public boolean handleOnClient() {
        return true;
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(lives);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        ClientData.lives = lives;
    }
}
