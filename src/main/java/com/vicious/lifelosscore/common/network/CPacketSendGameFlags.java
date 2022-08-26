package com.vicious.lifelosscore.common.network;

import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.common.LLFlag;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.function.Supplier;

public class CPacketSendGameFlags extends LLPacket {
    private IntList active = new IntArrayList();
    private IntList inactive = new IntArrayList();
    public CPacketSendGameFlags(){}

    @Override
    public boolean handleOnClient() {
        return true;
    }

    public CPacketSendGameFlags active(Collection<LLFlag> flags){
        for (LLFlag flag : flags) {
            active.add(flag.ordinal());
        }
        return this;
    }
    public CPacketSendGameFlags inactive(Collection<LLFlag> flags){
        for (LLFlag flag : flags) {
            inactive.add(flag.ordinal());
        }
        return this;}
    public CPacketSendGameFlags(FriendlyByteBuf buf){
        this.active =buf.readIntIdList();
        this.inactive =buf.readIntIdList();
    }

    @Override
    public void toBytes(FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeIntIdList(active);
        friendlyByteBuf.writeIntIdList(inactive);
    }

    @Override
    public void handle(Supplier<NetworkEvent.Context> supplier) {
        for (Integer flag : active) {
            ClientData.activeServerFlags.add(LLFlag.values()[flag]);
        }
        for (Integer flag : inactive) {
            ClientData.activeServerFlags.remove(LLFlag.values()[flag]);
        }
    }
}
