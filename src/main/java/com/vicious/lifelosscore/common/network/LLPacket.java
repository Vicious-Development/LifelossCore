package com.vicious.lifelosscore.common.network;

import com.vicious.viciouscore.common.network.VCPacket;
import com.vicious.viciouscore.common.util.SidedExecutor;
import net.minecraft.network.FriendlyByteBuf;

import java.util.function.Function;

public abstract class LLPacket extends VCPacket{
    private static int id = -1;
    public static int nextId() {
        ++id;
        return id;
    }

    public static <T extends LLPacket> void registerLL(Class<T> type, Function<FriendlyByteBuf, T> decoderConstructor) {
        LLNetwork.getInstance().getChannel().registerMessage(nextId(),type, (pk,buf)->{
            try{
                pk.toBytes(buf);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        },decoderConstructor,(pk,ctx)->{
            try{
                if(pk.handleOnClient()){
                    SidedExecutor.clientOnly(()->pk.handle(ctx));
                }
                else if(pk.handleOnServer()){
                    SidedExecutor.serverOnly(()->pk.handle(ctx));
                }
                ctx.get().setPacketHandled(true);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        });
    }
}
