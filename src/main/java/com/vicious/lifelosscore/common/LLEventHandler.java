package com.vicious.lifelosscore.common;


import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.lifelosscore.api.ILLPlayerData;
import com.vicious.lifelosscore.common.network.CPacketSendLossStart;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.lifelosscore.common.potion.LLPotions;
import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import com.vicious.viciouscore.common.capability.VCCapabilities;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncablePlayerData;
import com.vicious.viciouscore.common.events.TotemUsedEvent;
import com.vicious.viciouscore.common.phantom.WorldPos;
import com.vicious.viciouscore.common.util.FuckLazyOptionals;
import com.vicious.viciouscore.common.util.server.BetterChatMessage;
import com.vicious.viciouscore.common.util.server.ServerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class LLEventHandler {
    public static void resurrect(ServerPlayer player) {
        WorldPos spawnPoint = new WorldPos(ServerHelper.server.getLevel(player.getRespawnDimension()),player.getRespawnPosition());
        if(spawnPoint.position == null){
            spawnPoint = ServerHelper.getDefaultRespawnPos();
        }
        player.teleportTo((ServerLevel) spawnPoint.level,spawnPoint.position.getX(),spawnPoint.position.getY(),spawnPoint.position.getZ(),0,0);
        player.setGameMode(GameType.SURVIVAL);
        LifelossChatMessage.from(ChatFormatting.BOLD,ChatFormatting.GREEN,"<lifeloss.backfromthegrave>").send(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDrink(MobEffectEvent.Remove pice){
        if (pice.isCanceled()) return;
        LivingEntity e = pice.getEntity();
        MobEffectInstance soulweakness = e.getEffect(LLPotions.SOULWEAKNESS);
        if(soulweakness != null){
            LifelossChatMessage.from(ChatFormatting.BOLD,ChatFormatting.RED,"<lifeloss.soulweaknessuncurable>").send(e);
            pice.setCanceled(true);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTotemDeath(TotemUsedEvent.Post ere){
        try{
            if(ere.isCanceled()) return;
            if(ere.getEntity() instanceof Player p){
                SyncablePlayerData.executeIfPresent(p,(pdat)->{
                    boolean soulWeakened = pdat.reduceLivesToOne(1);
                    if(!soulWeakened){
                        LifelossChatMessage.from(ChatFormatting.GOLD,ChatFormatting.BOLD,"<lifeloss.totemlifeloss>").send(p);
                        LifelossChatMessage.from(ChatFormatting.GOLD,ChatFormatting.BOLD,"<1lifeloss.othertotemlifeloss>",p.getDisplayName()).sendExcept(p,ServerHelper.getPlayers());
                    }
                    else{
                        LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<lifeloss.gotsoulweakness>").send(p);
                        LifelossChatMessage.from(ChatFormatting.GOLD,ChatFormatting.BOLD,"<1lifeloss.totemantipermadeath>",p.getDisplayName()).sendExcept(p,ServerHelper.getPlayers());
                        MobEffectInstance sws = p.getEffect(LLPotions.SOULWEAKNESS);
                        if(sws != null) {
                            p.addEffect(new MobEffectInstance(LLPotions.SOULWEAKNESS, sws.getDuration()+300, sws.getAmplifier()+1));
                        }
                        else p.addEffect(new MobEffectInstance(LLPotions.SOULWEAKNESS, 300, 1));
                    }
                },ILLPlayerData.class);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDeath(LivingDeathEvent pde){
        if(!pde.isCanceled() && pde.getEntity() instanceof ServerPlayer victim && FuckLazyOptionals.getOrNull(victim.getCapability(VCCapabilities.PLAYERDATA)) instanceof ILLPlayerData vdat) {
            DamageSource cause = pde.getSource();
            Entity perpetrator = cause.getDirectEntity();
            if (perpetrator != null && perpetrator != victim && FuckLazyOptionals.getOrNull(perpetrator.getCapability(VCCapabilities.PLAYERDATA)) instanceof ILLPlayerData perpdat) {
                LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<2lifeloss.playerkill>",victim.getDisplayName(),ChatFormatting.GOLD,ChatFormatting.BOLD,perpetrator.getDisplayName()).send(ServerHelper.getPlayers());
                perpdat.growLives(1);
                LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<1lifeloss.livesleft>",""+(vdat.getLives()-1)).send(victim);
            } else {
                LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD, "<1lifeloss.otherlostlife>",victim.getDisplayName()).send(ServerHelper.getPlayers());
                LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<1lifeloss.livesleft>",""+(vdat.getLives()-1)).send(victim);
            }
            vdat.shrinkLives(1);
        }
    }
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event){
        if(!event.isEndConquered()) {
            SyncablePlayerData.executeIfPresent(event.getEntity(), (pd) -> {
                if (pd.isDead()) {
                    LifelossChatMessage.from(ChatFormatting.RED, ChatFormatting.BOLD, "<1lifeloss.otherhaunt>", event.getEntity().getDisplayName()).sendExcept(event.getEntity(), ServerHelper.getPlayers());
                    haunt(event.getEntity());
                }
            }, ILLPlayerData.class);
        }
    }



    private static void haunt(Player p) {
        if(p != null){
            LifelossChatMessage.from(ChatFormatting.RED, ChatFormatting.BOLD,"<lifeloss.selfhaunt>").send(p);
            if(LLCFG.getInstance().kyraMode.value()){
                SyncableGlobalData.getInstance().executeAs(ILLGlobalData.class,(global)->{
                    global.setKyraMode(true);
                });
                LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD, "<lifeloss.kyracauser>").send(p);
                BetterChatMessage punishment = LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<1lifeloss.kyrapunishment>", p.getDisplayName());
                ServerHelper.forAllPlayersExcept(p,(plr)->{
                    punishment.send(plr);
                    plr.setGameMode(GameType.SPECTATOR);
                });
            }
            ((ServerPlayer)p).setGameMode(GameType.SPECTATOR);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        if(event.getEntity() instanceof ServerPlayer plr) {
            LLFlag.sendToPlayer(plr);
            LLNetwork.getInstance().sendToPlayer(plr,new CPacketSendLossStart(LLCFG.getInstance().healthLossStart.value()));
            SyncablePlayerData.executeIfPresent(plr, (dat)->{
                dat.sendLives();
                if (dat.isDead()) {
                    LifelossChatMessage.from(ChatFormatting.RED, ChatFormatting.BOLD, "<lifeloss.hauntonjoin>");
                    plr.setGameMode(GameType.SPECTATOR);
                } else if (plr.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    resurrect(plr);
                }
            },ILLPlayerData.class);
        }
    }
}
