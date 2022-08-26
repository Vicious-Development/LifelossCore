package com.vicious.lifelosscore.common.teams;

import com.mojang.authlib.GameProfile;
import com.vicious.lifelosscore.api.ILLGlobalData;
import com.vicious.lifelosscore.api.ILLPlayerData;
import com.vicious.lifelosscore.common.LLCFG;
import com.vicious.lifelosscore.common.network.CPacketSendTeamCenter;
import com.vicious.lifelosscore.common.network.CPacketTeamInfo;
import com.vicious.lifelosscore.common.network.LLNetwork;
import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import com.vicious.viciouscore.common.data.DataAccessor;
import com.vicious.viciouscore.common.data.IVCNBTSerializable;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncableGlobalData;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncablePlayerData;
import com.vicious.viciouscore.common.phantom.WorldPos;
import com.vicious.viciouscore.common.util.server.BetterChatMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

public class Team implements IVCNBTSerializable {
    private final Set<UUID> members = new HashSet<>();
    private final Set<ServerPlayer> onlineMembers = new HashSet<>();
    private BlockPos avg = new BlockPos(0,0,0);
    private GraceState graceState = new Cooldown(0);
    private String name;
    private UUID uuid;

    public Team(){}
    public Team(String name){
        this.name=name;
        this.uuid = UUID.randomUUID();
    }

    public boolean isInTeam(Player plr){
        return members.contains(plr.getUUID());
    }

    public void onLogin(ServerPlayer plr){
        onlineMembers.add(plr);
        distCalc();
        distanceFairnessCheck(plr,GraceReason.LOGIN);
    }
    public void onLogout(ServerPlayer plr){
        onlineMembers.remove(plr);
        distCalc();
    }
    public void onClone(ServerPlayer plr, ServerPlayer original){
        onlineMembers.remove(original);
        onlineMembers.add(plr);
        distCalc();
        distanceFairnessCheck(plr,GraceReason.DEATH);
    }
    public int getExceededDistance(ServerPlayer plr){
        int multiplier = DimensionalDistanceCalculator.getMultiplier(plr.getLevel());
        BlockPos p = new BlockPos(avg.getX()*multiplier,avg.getY()*multiplier,avg.getZ()*multiplier);
        return (int) Math.sqrt(p.distToCenterSqr(plr.position())-LLCFG.getInstance().healthLossStart.value());
    }
    public void distanceFairnessCheck(ServerPlayer plr, GraceReason reason){
        int exceeded = getExceededDistance(plr);
        if(exceeded > 0){
            grace(new GraceState(reason,exceeded),plr);
        }
    }
    public void grace(GraceState state, ServerPlayer causer){
        if(graceState.reason.force() || !(graceState instanceof Cooldown)){
            graceState = state;
            if(!graceState.hasEnded()){
                state.getReason().getMessage(causer,state.time/20).send(onlineMembers);
            }
        }
    }

    public boolean isWithinSafeZone(ServerPlayer plr){
        return getExceededDistance(plr) > 0;
    }


    public void tick() {
        //Calculate centerpoint.
        distCalc();

        //Check if the grace period has ended, send a warning informing all members. Alert members out of the zone their health is being reduced.
        if(graceState.hasEnded()){
            if(!(graceState instanceof Cooldown)){
                graceState = new Cooldown(LLCFG.getInstance().healthLossGraceCooldown.value()*20);
                BetterChatMessage graceWarning = LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<lifeloss.graceremoved>");
                BetterChatMessage warning = LifelossChatMessage.from(ChatFormatting.DARK_RED,ChatFormatting.BOLD,"<lifeloss.healthdraining>");
                for (ServerPlayer m : onlineMembers) {
                    graceWarning.send(m);
                    if(!isWithinSafeZone(m)){
                        warning.send(m);
                    }
                }
            }

        }
        //Tick the cooldown/grace period.
        else{
            graceState.tick();
        }

        //Check the distances of all players.
        //If they have exceeded the maximum distance prevent removal of the current grace period.
        //Reduce the health of those outside the region.
        boolean removeGrace = true;
        for(ServerPlayer m : onlineMembers){
            int exceeded = getExceededDistance(m);
            if(exceeded > 0){
                if(m.gameMode.isSurvival()) {
                    grace(new GraceState(GraceReason.OUTOFBOUNDS, exceeded), m);
                    if (graceState.noGrace()) {
                        healthCalc(m, exceeded);
                    }
                    removeGrace = false;
                }
            }
            else{
                AttributeInstance maxhp = m.getAttribute(Attributes.MAX_HEALTH);
                if(maxhp != null) maxhp.removeModifier(((ILLGlobalData) SyncableGlobalData.getInstance()).getMaxHealthAttributeModUUID());
            }
        }
        if(removeGrace && !(graceState instanceof Cooldown)){
            LifelossChatMessage.from(ChatFormatting.RED,ChatFormatting.BOLD,"<lifeloss.graceremovedsafe>");
            graceState = new Cooldown(LLCFG.getInstance().healthLossGraceCooldown.value());
        }
    }

    public void distCalc(){
        if(onlineMembers.size() == 0) return;
        BlockPos og = avg;
        BlockPos newAvg = new BlockPos(0,0,0);
        for (ServerPlayer m : onlineMembers) {
            if(m.isAlive()  && m.gameMode.isSurvival()) {
                newAvg = newAvg.offset(DimensionalDistanceCalculator.calculateTruePos(new WorldPos(m.getLevel(), new BlockPos(m.getBlockX(), m.getBlockY(), m.getBlockZ()))));
            }
        }
        avg = new BlockPos(newAvg.getX()/onlineMembers.size(),newAvg.getY()/onlineMembers.size(),newAvg.getZ()/onlineMembers.size());
        if(og != avg){
            CPacketSendTeamCenter p = new CPacketSendTeamCenter(avg);
            for (ServerPlayer plr : onlineMembers) {
                int mult = DimensionalDistanceCalculator.getMultiplier(plr.getLevel());
                if(mult == 1) {
                    LLNetwork.getInstance().sendToPlayer(plr, p);
                }
                else{
                    LLNetwork.getInstance().sendToPlayer(plr, new CPacketSendTeamCenter(new BlockPos(avg.getX()/mult,avg.getY()/mult,avg.getZ()/mult)));
                }
            }
        }
    }

    public void healthCalc(ServerPlayer member, int exceeded){
        AttributeInstance maxhp = member.getAttribute(Attributes.MAX_HEALTH);
        double percentage = (double) 1/Math.pow(LLCFG.getInstance().healthLossMultiplier.value(),exceeded);
        DistanceHealthModifier mod = new DistanceHealthModifier(percentage);
        maxhp.removeModifier(mod.getId());
        maxhp.addTransientModifier(mod);
    }

    public UUID getUUID() {
        return uuid;
    }

    public void addMember(ServerPlayer player) {
        addMember(player.getUUID());
        SyncablePlayerData.executeIfPresent(player,(pd)-> pd.setTeamID(uuid),ILLPlayerData.class);
        onlineMembers.add(player);
        LifelossChatMessage.from(ChatFormatting.GREEN,ChatFormatting.BOLD,"<1lifeloss.joinedteam>", player.getDisplayName()).send(onlineMembers);
        LLNetwork.getInstance().sendToPlayer(player,new CPacketTeamInfo(this));
    }
    public void addMember(UUID uuid){
        members.add(uuid);
    }

    public void removeMember(ServerPlayer sp, CommandSourceStack remover, LeaveReason reason) {
        if(reason == LeaveReason.KICKED){
            reason.getMessage(sp.getDisplayName(),remover.getDisplayName()).send(onlineMembers);
        }
        else{
            reason.getMessage(sp.getDisplayName()).send(onlineMembers);
        }
        removeMember(sp);
    }
    public void removeMember(ServerPlayer sp){
        removeMember(sp.getUUID());
        SyncablePlayerData.executeIfPresent(sp,(pd)-> pd.setTeamID(null),ILLPlayerData.class);
        onlineMembers.remove(sp);
    }
    public void removeMember(UUID uuid){
        members.remove(uuid);
    }

    public String getName() {
        return name;
    }

    @Override
    public void serializeNBT(CompoundTag nbt, DataAccessor dataAccessor) {
        CompoundTag tag = new CompoundTag();
        tag.putString("n",name);
        tag.putUUID("i",uuid);
        CompoundTag listTag = new CompoundTag();
        int i = 0;
        for (UUID member : members) {
            listTag.putUUID("m"+i,member);
            i++;
        }
        tag.put("m",listTag);
        nbt.put("team",tag);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt, DataAccessor dataAccessor) {
        nbt = nbt.getCompound("team");
        name = nbt.getString("n");
        uuid = nbt.getUUID("i");
        nbt = nbt.getCompound("m");
        for (String m : nbt.getAllKeys()) {
            members.add(nbt.getUUID(m));
        }

    }

    public Set<UUID> getMembers() {
        return members;
    }

    public boolean isInTeam(GameProfile target) {
        return members.contains(target.getId());
    }

    public Collection<? extends Entity> getOnlineMembers() {
        return onlineMembers;
    }

    private static class GraceState{
        public static final GraceState NONE = new GraceState(GraceReason.OUTOFBOUNDS,0);
        protected GraceReason reason;
        protected int time;
        public GraceState(GraceReason reason, int distance){
            this.reason=reason;
            this.time=reason.getGraceTime(distance);
        }
        protected GraceState(){}
        public void tick(){
            time--;
        }
        public void end(){
            time=0;
        }
        public boolean hasEnded(){
            return time <= 0;
        }
        public GraceReason getReason(){
            return reason;
        }

        public boolean noGrace() {
            return this instanceof Cooldown || hasEnded();
        }
    }

    @Override
    public String toString() {
        return "Team{" +
                "members=" + members +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                '}';
    }

    private static class Cooldown extends GraceState{
        public Cooldown(int time) {
            reason=GraceReason.ADMIN;
            this.time=time;
        }
    }

    private enum GraceReason{
        LOGIN("<2lifeloss.logingrace>",(i)->LLCFG.getInstance().healthLossTeleportGrace.value()*i),
        DEATH("<2lifeloss.deathgrace>",(i)->LLCFG.getInstance().healthLossTeleportGrace.value()*i),
        OUTOFBOUNDS("<2lifeloss.oobgrace>",(i)->LLCFG.getInstance().healthLossGrace.value()*20),
        ADMIN("<2lifeliss.admingrace>",(i)->i);
        final String translationKey;
        final Function<Integer,Integer> timeFunc;
        GraceReason(String key, Function<Integer,Integer> graceTimeFunc){
            this.translationKey=key;
            this.timeFunc=graceTimeFunc;
        }
        public int getGraceTime(int dist){
            return timeFunc.apply(dist);
        }
        public BetterChatMessage getMessage(ServerPlayer causer, int time){
            return LifelossChatMessage.from(ChatFormatting.GOLD,ChatFormatting.BOLD, translationKey, causer.getDisplayName(),"" + time);
        }

        public boolean force() {
            return this == LOGIN || this == DEATH;
        }
    }
}
