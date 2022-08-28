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
import java.util.function.Consumer;
import java.util.function.Function;

public class Team implements IVCNBTSerializable {
    private final Set<UUID> members = new HashSet<>();
    private final Set<ServerPlayer> onlineMembers = new HashSet<>();
    private BlockPos avg = new BlockPos(0,0,0);
    private String name;
    private UUID uuid;
    private Grace grace = new Grace(GraceReason.INIT,0);
    private GraceCooldown graceCooldown = new GraceCooldown(0);

    public void onGraceEnd(Grace grace){
        if(grace.getReason() != GraceReason.INIT) {
            graceCooldown = new GraceCooldown(LLCFG.getInstance().healthLossGraceCooldown.value());
            BetterChatMessage graceWarning = !grace.safe ? LifelossChatMessage.from(ChatFormatting.RED, "<1lifeloss.graceremoved>", graceCooldown.getTimeRemaining()/20) : LifelossChatMessage.from(ChatFormatting.GREEN, "<1lifeloss.graceremovedsafe>", graceCooldown.getTimeRemaining()/20);
            BetterChatMessage warning = LifelossChatMessage.from(ChatFormatting.RED, "<lifeloss.healthdraining>");
            for (ServerPlayer m : onlineMembers) {
                graceWarning.send(m);
                if (!isWithinSafeZone(m)) {
                    if(!grace.safe) warning.send(m);
                }
            }
        }
    }

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
    public void distanceFairnessCheck(ServerPlayer plr, GraceReason reason){
        int exceeded = getExceededDistance(plr);
        if(exceeded > 0){
            setGrace(new Grace(reason,exceeded, this::onGraceEnd),plr);
        }
    }
    public int getExceededDistance(ServerPlayer plr){
        int multiplier = DimensionalDistanceCalculator.getMultiplier(plr.getLevel());
        BlockPos p = new BlockPos(avg.getX()/multiplier,avg.getY()/multiplier,avg.getZ()/multiplier);
        return (int) Math.sqrt(p.distToCenterSqr(plr.position()))-LLCFG.getInstance().healthLossStart.value();
    }

    public boolean isWithinSafeZone(ServerPlayer plr){
        return getExceededDistance(plr) > 0;
    }

    public boolean hasGrace(){
        return graceCooldown.hasEnded() && !grace.hasEnded();
    }

    public void setGrace(Grace g, ServerPlayer causer){
        grace = g;
        g.getReason().getMessage(causer,g.getTimeRemaining()).send(onlineMembers);
    }

    public void tick() {
        //Calculate centerpoint.
        BlockPos og = avg;
        distCalc();
        if(og != avg){
            CPacketSendTeamCenter p = new CPacketSendTeamCenter(avg);
            boolean allSafe = true;
            for (ServerPlayer plr : onlineMembers) {
                int mult = DimensionalDistanceCalculator.getMultiplier(plr.getLevel());
                if(mult == 1) {
                    LLNetwork.getInstance().sendToPlayer(plr, p);
                }
                else{
                    LLNetwork.getInstance().sendToPlayer(plr, new CPacketSendTeamCenter(new BlockPos(avg.getX()/mult,avg.getY()/mult,avg.getZ()/mult)));
                }
                int exceeded = getExceededDistance(plr);
                if(exceeded > 0){
                    allSafe = false;
                    if(graceCooldown.hasEnded() && graceCooldown.shouldEnd && grace.hasEnded()){
                       setGrace(new Grace(GraceReason.OUTOFBOUNDS,exceeded, this::onGraceEnd),plr);
                    }
                    if(!hasGrace()) {
                        healthCalc(plr, exceeded);
                    }
                }
                else{
                    AttributeInstance maxhp = plr.getAttribute(Attributes.MAX_HEALTH);
                    if(maxhp != null) maxhp.removeModifier(((ILLGlobalData) SyncableGlobalData.getInstance()).getMaxHealthAttributeModUUID());
                }
            }
            if(allSafe){
                grace.remaining=0;
            }
            grace.safe=allSafe;
            graceCooldown.shouldEnd=allSafe;
        }
        if(!grace.hasEnded()){
            grace.tick();
        }
        if(!graceCooldown.hasEnded()){
            graceCooldown.tick();
        }
    }

    public void distCalc(){
        BlockPos newAvg = new BlockPos(0,0,0);
        int num = 0;
        for (ServerPlayer m : onlineMembers) {
            if(m.isAlive()  && m.gameMode.isSurvival()) {
                newAvg = newAvg.offset(DimensionalDistanceCalculator.calculateTruePos(new WorldPos(m.getLevel(), new BlockPos(m.getBlockX(), m.getBlockY(), m.getBlockZ()))));
                num++;
            }
        }
        if(num == 0) return;
        avg = new BlockPos(newAvg.getX()/num,newAvg.getY()/num,newAvg.getZ()/num);
    }

    public void healthCalc(ServerPlayer member, int exceeded){
        AttributeInstance maxhp = member.getAttribute(Attributes.MAX_HEALTH);
        double percentage = (double) 1/Math.pow(LLCFG.getInstance().healthLossMultiplier.value(),exceeded);
        DistanceHealthModifier mod = new DistanceHealthModifier(-(1F-percentage));
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
        SyncablePlayerData.executeIfPresent(sp,(pd)-> pd.setTeamID(null),ILLPlayerData.class);
        removeMember(sp.getUUID());
        onlineMembers.remove(sp);
        LLNetwork.getInstance().sendToPlayer(sp,new CPacketTeamInfo());
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

    @Override
    public String toString() {
        return "Team{" +
                "members=" + members +
                ", name='" + name + '\'' +
                ", uuid=" + uuid +
                '}';
    }

    private enum GraceReason{
        LOGIN("<2lifeloss.logingrace>",(i)->LLCFG.getInstance().healthLossTeleportGrace.value()*i),
        DEATH("<2lifeloss.deathgrace>",(i)->LLCFG.getInstance().healthLossTeleportGrace.value()*i),
        OUTOFBOUNDS("<2lifeloss.oobgrace>",(i)->LLCFG.getInstance().healthLossGrace.value()),
        ADMIN("<2lifeloss.admingrace>",(i)->i),
        INIT("<lifeloss.illegal>",(i)->0);
        final String translationKey;
        final Function<Integer,Integer> timeFunc;
        GraceReason(String key, Function<Integer,Integer> graceTimeFunc){
            this.translationKey=key;
            this.timeFunc=graceTimeFunc;
        }
        public int getGraceTime(int dist){
            return timeFunc.apply(dist);
        }
        public BetterChatMessage getMessage(ServerPlayer causer, int timeTicks){
            return LifelossChatMessage.from(ChatFormatting.GOLD,ChatFormatting.BOLD, translationKey, causer.getDisplayName(),"" + timeTicks/20);
        }

        public boolean force() {
            return this == LOGIN || this == DEATH || this == ADMIN;
        }
    }
    private static class Grace{
        private int remaining = 0;
        private boolean ended = false;
        private Consumer<Grace> onEnd = (g)->{};
        private GraceReason reason = GraceReason.ADMIN;
        private boolean safe = false;
        public Grace(GraceReason reason, int exceeded){
            if(reason != null) this.reason = reason;
            this.remaining = this.reason.getGraceTime(exceeded);
        }
        public Grace(GraceReason reason, int exceeded, Consumer<Grace> onEnd){
            if(reason != null) this.reason = reason;
            this.remaining = this.reason.getGraceTime(exceeded);
            this.onEnd=onEnd;
        }
        public GraceReason getReason(){
            return reason;
        }
        public int getTimeRemaining(){
            return remaining;
        }
        public void tick(){
            remaining--;
        }
        public boolean hasEnded(){
            boolean ret = remaining <= 0;
            if(ended != ret){
                onEnd.accept(this);
            }
            ended = ret;
            return ended;
        }
        public boolean forceGrace(){
            return reason.force();
        }
    }
    private static class GraceCooldown{
        private int remaining = 0;
        private boolean shouldEnd = true;
        public GraceCooldown(int time){
            this.remaining = time;
        }
        public int getTimeRemaining(){
            return remaining;
        }
        public void tick(){
            remaining--;
        }
        public boolean hasEnded(){
            return remaining <= 0;
        }
    }
}
