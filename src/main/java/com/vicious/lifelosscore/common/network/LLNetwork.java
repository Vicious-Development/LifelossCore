package com.vicious.lifelosscore.common.network;

import com.vicious.viciouscore.common.network.VCNetwork;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class LLNetwork extends VCNetwork{
    public static LLNetwork instance;
    public static LLNetwork getInstance(){
        if(instance == null){
            instance = new LLNetwork();
            LLPacket.registerLL(CPacketSendTeamCenter.class, CPacketSendTeamCenter::new);
            LLPacket.registerLL(CPacketSendLives.class, CPacketSendLives::new);
            LLPacket.registerLL(CPacketSendGameFlags.class, CPacketSendGameFlags::new);
            LLPacket.registerLL(CPacketTeamInfo.class,CPacketTeamInfo::new);
            LLPacket.registerLL(CPacketSendLossStart.class,CPacketSendLossStart::new);
        }
        return instance;
    }

    @Override
    public SimpleChannel getChannel() {
        if(channel == null) channel = NetworkRegistry.newSimpleChannel(new ResourceLocation("lifeloss", "network"),VCNetwork::getProtocolVersion,getProtocolVersion()::equals,getProtocolVersion()::equals);
        return channel;

    }
}
