package com.vicious.lifelosscore;

import com.mojang.logging.LogUtils;
import com.vicious.lifelosscore.client.gui.ClientData;
import com.vicious.lifelosscore.client.gui.GUIRegistry;
import com.vicious.lifelosscore.common.LLCFG;
import com.vicious.lifelosscore.common.LLCommands;
import com.vicious.lifelosscore.common.LLEventHandler;
import com.vicious.lifelosscore.common.LLFlag;
import com.vicious.lifelosscore.common.events.Ticker;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.lifelosscore.common.teams.TeamManager;
import com.vicious.viciouscore.common.util.server.ServerHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkEvent;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Lifelosscore.MODID)
public class Lifelosscore {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "lifelosscore";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public Lifelosscore() {
        LLNetwork.getInstance();
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        AtomicBoolean reInitTeams = new AtomicBoolean(false);
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);
        modEventBus.addListener(GUIRegistry::onRegister);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(TeamManager.class);
        MinecraftForge.EVENT_BUS.register(Ticker.class);
        reInitTeams.set(true);
        LLCFG.getInstance().changeListeners.add((c)->{
            LLFlag.updateFlags();
            if(c.teamMode.getBoolean()){
                if(reInitTeams.get()) {
                    for (ServerPlayer player : ServerHelper.getPlayers()) {
                        //Reload all teams.
                        TeamManager.onLogout(new PlayerEvent.PlayerLoggedOutEvent(player));
                        TeamManager.onLogin(new PlayerEvent.PlayerLoggedInEvent(player));
                    }
                    reInitTeams.set(false);
                }
            }
            else{
                reInitTeams.set(true);
            }
            LLCFG.getInstance().save();
        });
        MinecraftForge.EVENT_BUS.register(LLEventHandler.class);
        MinecraftForge.EVENT_BUS.register(LLCommands.class);
    }


    private void commonSetup(final FMLCommonSetupEvent event) {

    }
    private void clientSetup(FMLClientSetupEvent event){

    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LLFlag.updateFlags();
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event){
        LLCFG.getInstance().save();
    }

    @SubscribeEvent
    public void clientJoinWorld(NetworkEvent.ClientCustomPayloadLoginEvent ev){
        ClientData.reset();

    }
}
