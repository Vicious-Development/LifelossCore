package com.vicious.lifelosscore.common;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.vicious.lifelosscore.api.ILLPlayerData;
import com.vicious.lifelosscore.common.teams.LeaveReason;
import com.vicious.lifelosscore.common.teams.Team;
import com.vicious.lifelosscore.common.teams.TeamManager;
import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import com.vicious.viciouscore.common.data.implementations.attachable.SyncablePlayerData;
import com.vicious.viciouscore.common.util.SidedExecutor;
import com.vicious.viciouscore.common.util.server.BetterChatMessage;
import com.vicious.viciouscore.common.util.server.ServerHelper;
import com.vicious.viciouslib.database.tracking.values.TrackableValue;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class LLCommands {
    @SubscribeEvent
    public static void register(RegisterCommandsEvent event){
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal("lifeloss")
                .then(Commands.literal("givelives").requires((plr)-> plr.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.argument("targets", EntityArgument.players())
                            .executes((ctx)->giveLives(ctx.getSource(),EntityArgument.getPlayers(ctx,"targets"),1))
                            .then(Commands.argument("amount",IntegerArgumentType.integer())
                                .executes((ctx)->giveLives(ctx.getSource(),EntityArgument.getPlayers(ctx,"targets"),IntegerArgumentType.getInteger(ctx,"amount"))))))
                .then(Commands.literal("seelives").requires((p)->p.hasPermission(Commands.LEVEL_ALL))
                        .executes((ctx)->seeLives(ctx.getSource()))
                        .then(Commands.argument("targets",EntityArgument.players())
                                .executes((ctx)->seeLives(ctx.getSource(),EntityArgument.getPlayers(ctx, "targets")))))
                .then(Commands.literal("team")
                        .then(Commands.literal("create")
                                .then(Commands.argument("name", StringArgumentType.string())
                                .executes((ctx)->createTeam(StringArgumentType.getString(ctx,"name"),ctx.getSource()))
                                ))
                        .then(Commands.literal("invite")
                                .then(Commands.literal("accept")
                                        .executes((ctx)->acceptInvite(ctx.getSource())))
                                .then(Commands.literal("send")
                                        .then(Commands.argument("targets",EntityArgument.players())
                                        .executes((ctx)->sendInvites(EntityArgument.getPlayers(ctx, "targets"),ctx.getSource()))))
                        )
                        .then(Commands.literal("delete").requires(ILLPlayerData::ownsTeam)
                                .executes((src)-> deleteTeam(src.getSource())))
                        .then(Commands.literal("info")
                                .executes(ctx->sendTeamInfo(ctx.getSource()))
                                .then(Commands.argument("team",StringArgumentType.string())
                                        .executes(ctx->sendTeamInfo(ctx.getSource(),StringArgumentType.getString(ctx,"team")))))
                        .then(Commands.literal("leave")
                                .executes(ctx->leaveTeam(ctx.getSource())))
                        .then(Commands.literal("kick").requires(ILLPlayerData::ownsTeam)
                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                        .executes(ctx->kickMembers(ctx.getSource(),StringArgumentType.getString(ctx,"team"),GameProfileArgument.getGameProfiles(ctx,"targets")))))
                        .then(Commands.literal("promote").requires(ILLPlayerData::ownsTeam)
                                .then(Commands.argument("target",GameProfileArgument.gameProfile())
                                        .executes((ctx)->promote(ctx.getSource(),GameProfileArgument.getGameProfiles(ctx,"target")))))
                        .then(Commands.literal("admin").requires((p)->p.hasPermission(Commands.LEVEL_ADMINS))
                                .then(Commands.argument("team",StringArgumentType.string())
                                        .then(Commands.literal("delete")
                                                .executes((ctx)->deleteTeam(StringArgumentType.getString(ctx,"team"),ctx.getSource())))
                                        .then(Commands.literal("join")
                                                .executes(ctx->joinTeam(ctx.getSource(),StringArgumentType.getString(ctx,"team")))
                                                .then(Commands.argument("targets",EntityArgument.players())
                                                        .executes(ctx->{
                                                            String team = StringArgumentType.getString(ctx,"team");
                                                            Team t = TeamManager.getTeam(team);
                                                            if(t != null){
                                                                for (ServerPlayer target : EntityArgument.getPlayers(ctx, "targets")) {
                                                                    t.addMember(target);
                                                                }
                                                            }
                                                            else{
                                                                noSuchTeam(team,ctx.getSource());
                                                            }
                                                            return 1;
                                                        })))
                                        .then(Commands.literal("kick")
                                                .then(Commands.argument("targets", GameProfileArgument.gameProfile())
                                                        .executes(ctx->kickMembers(ctx.getSource(),StringArgumentType.getString(ctx,"team"),GameProfileArgument.getGameProfiles(ctx,"targets")))))
                                        .then(Commands.literal("promote")
                                                .then(Commands.argument("target",GameProfileArgument.gameProfile())
                                                        .executes((ctx)->promote(ctx.getSource(),StringArgumentType.getString(ctx,"team"),GameProfileArgument.getGameProfiles(ctx,"target")))))
                                )
                        ))
                .then(Commands.literal("config").requires((p)->p.hasPermission(Commands.LEVEL_ADMINS))
                        .then(Commands.argument("feature",StringArgumentType.string())
                                .executes((ctx)->{
                                    String name = StringArgumentType.getString(ctx,"feature");
                                    Object v =  LLCFG.getInstance().getValue(name);
                                    if(v == null) v = "does not exist";
                                    else if(v instanceof TrackableValue trackable){
                                        v = trackable.value();
                                    }
                                    LifelossChatMessage.from(ChatFormatting.GREEN,ChatFormatting.BOLD, name,ChatFormatting.RESET, ChatFormatting.YELLOW, " = ",ChatFormatting.DARK_GREEN, v.toString()).send(ctx.getSource());
                                    return 1;
                                })
                                .then(Commands.argument("newvalue",StringArgumentType.greedyString())
                                        .executes(ctx->{
                                            try {
                                                String name = StringArgumentType.getString(ctx, "feature");
                                                TrackableValue<?> v = LLCFG.getInstance().values.get(name);
                                                if (v == null) {
                                                    LifelossChatMessage.from(ChatFormatting.RED, "<1lifeloss.nosuchconfigvalue>", name).send(ctx.getSource());
                                                } else {
                                                    try {
                                                        v.setFromStringWithUpdate(StringArgumentType.getString(ctx, "newvalue"));
                                                    } catch (Exception e) {
                                                        BetterChatMessage.from(ChatFormatting.RED, ChatFormatting.BOLD, "<lifeloss.invalidstring>");
                                                    }
                                                    LifelossChatMessage.from(ChatFormatting.GREEN, ChatFormatting.BOLD, name, ChatFormatting.RESET, ChatFormatting.YELLOW, " set to ", ChatFormatting.DARK_GREEN, v.toString()).send(ctx.getSource());
                                                }
                                            }catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            return 1;
                                        }))));
        event.getDispatcher().register(cmd);

        SidedExecutor.clientOnly(()->{

        });
    }

    private static int promote(CommandSourceStack source, Collection<GameProfile> target) {
        if(source.getEntity() instanceof ServerPlayer sp) {
            SyncablePlayerData.executeIfPresent(sp,pd->{
                for (GameProfile gameProfile : target) {
                    TeamManager.getTeam(pd.getTeamID()).setOwner(gameProfile);
                    break;
                }
            },ILLPlayerData.class);
        }
        return 1;
    }
    private static int promote(CommandSourceStack src,String team, Collection<GameProfile> target) {
        Team t = TeamManager.getTeam(team);
        if(t != null){
            for (GameProfile gameProfile : target) {
                t.setOwner(gameProfile);
                break;
            }
        }
        else{
            noSuchTeam(team,src);
        }
        return 1;
    }

    private static int deleteTeam(CommandSourceStack src) {
        if(src.getEntity() instanceof ServerPlayer) {
            SyncablePlayerData.executeIfPresent(src.getEntity(), pd -> {
                Team t = TeamManager.getTeam(pd.getTeamID());
                deleteTeam(t.getName(),src);
            }, ILLPlayerData.class);
        }
        return 1;
    }

    private static int sendTeamInfo(CommandSourceStack source){
        try {
            if (source.getEntity() instanceof ServerPlayer sp) {
                SyncablePlayerData.executeIfPresent(sp, (pd) ->{
                    Team t = TeamManager.getTeam(pd.getTeamID());
                    if(t == null){
                        LifelossChatMessage.from(ChatFormatting.RED,"<lifeloss.commandneedsteam>").send(sp);
                    }
                    else{
                        sendTeamInfo(source, t.getName());
                    }
                },ILLPlayerData.class);
            } else {
                onlyPlayers(source);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
    private static int sendTeamInfo(CommandSourceStack source, String name){
        try {
            Team t = TeamManager.getTeam(name);
            if (t != null) {
                List<Object> msg = Lists.newArrayList(ChatFormatting.AQUA, ChatFormatting.BOLD, t.getName(), ChatFormatting.RESET, ChatFormatting.DARK_AQUA, "\n");
                msg.add("Members: ");
                int i = 0;
                for (UUID member : t.getMembers()) {
                    Optional<GameProfile> p = ServerHelper.server.getProfileCache().get(member);
                    if (p.isPresent()) {
                        GameProfile prof = p.get();
                        if(prof.getId().equals(t.getOwner())){
                            msg.add(ChatFormatting.BOLD);
                            msg.add(prof.getName());
                            msg.add(ChatFormatting.RESET);
                            msg.add(ChatFormatting.DARK_AQUA);
                        }
                        else{
                            msg.add(prof.getName());
                        }
                        if (i < t.getMembers().size() - 1) {
                            msg.add(", ");
                        }
                    }
                    i++;
                }
                new LifelossChatMessage(msg).send(source);
            } else {
                noSuchTeam(name, source);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
    private static int kickMembers(CommandSourceStack kicker, String name, Collection<GameProfile> targets){
        Team t = TeamManager.getTeam(name);
        if(t != null){
            for (GameProfile target : targets) {
                if(t.isInTeam(target)){
                    ServerPlayer plr = ServerHelper.server.getPlayerList().getPlayer(target.getId());
                    if(plr == null){
                        LifelossChatMessage.from(ChatFormatting.BLUE,"<2lifeloss.kickedteam>",target.getName(),kicker.getDisplayName()).send(t.getOnlineMembers());
                        t.removeMember(target.getId());
                    }
                    else{
                        t.removeMember(plr,kicker,LeaveReason.KICKED);
                    }
                }
                else{
                    LifelossChatMessage.from(ChatFormatting.DARK_RED, "<2lifeloss.notinteam>",target.getName(),t.getName()).send(kicker);
                }
            }
        }
        else{
            noSuchTeam(name, kicker);
        }
        return 1;
    }

    private static void noSuchTeam(String name, CommandSourceStack src) {
        LifelossChatMessage.from(ChatFormatting.RED,"<1lifeloss.nosuchteam>",name).send(src);
    }

    private static int joinTeam(CommandSourceStack joiner, String name){
        if(joiner.getEntity() instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                Team transfer = TeamManager.getTeam(name);
                if(transfer != null) {
                    if (pd.hasTeam()) {
                        TeamManager.getTeam(pd.getTeamID()).removeMember(sp, joiner, LeaveReason.LEFT);
                    }
                    transfer.addMember(sp);
                }
                else{
                    noSuchTeam(name,joiner);
                }
            },ILLPlayerData.class);
        }
        else{
            onlyPlayers(joiner);
        }
        return 1;
    }
    private static int leaveTeam(CommandSourceStack leaver){
        if(leaver.getEntity() instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                if(pd.hasTeam()){
                    TeamManager.getTeam(pd.getTeamID()).removeMember(sp,leaver,LeaveReason.LEFT);
                }
                else{
                    LifelossChatMessage.from(ChatFormatting.RED,"<lifeloss.commandneedsteam>").send(sp);
                }
            },ILLPlayerData.class);
        }
        else{
            onlyPlayers(leaver);
        }
        return 1;
    }
    private static int deleteTeam(String name, CommandSourceStack deleter){
        Team team = TeamManager.getTeam(name);
        if(team != null){
            TeamManager.deleteTeam(team);
            for (UUID member : team.getMembers()) {
                ServerPlayer plr = ServerHelper.server.getPlayerList().getPlayer(member);
                if(plr != null){
                    ILLPlayerData.get(plr).setTeamID(null);
                }
            }
            LifelossChatMessage.from(ChatFormatting.GOLD,"<2lifeloss.deletedteam>",deleter.getDisplayName(),name).send(ServerHelper.getPlayers());
        }
        else{
            noSuchTeam(name,deleter);
        }
        return 1;
    }
    private static int createTeam(String name, CommandSourceStack stack){
        try {
            if (TeamManager.getTeam(name) != null) {
                LifelossChatMessage.from(ChatFormatting.RED, "<1lifeloss.teamexists>", name).send(stack);
                return 1;
            }
            if (ILLPlayerData.hasTeam(stack)) {
                if (stack.hasPermission(Commands.LEVEL_ADMINS)) {
                    LifelossChatMessage.from(ChatFormatting.GREEN, "<1lifeloss.createdemptyteam>", name).send(stack);
                } else {
                    LifelossChatMessage.from(ChatFormatting.RED, "<lifeloss.onlyoneteam>").send(stack);
                }
            } else {
                Team t = TeamManager.createTeam(name);
                ServerPlayer player = stack.getPlayer();
                LifelossChatMessage.from(ChatFormatting.GREEN, "<2lifeloss.createdteam>", stack.getDisplayName(), name).send(ServerHelper.getPlayers());
                if (player != null){
                    t.setOwner(stack.getPlayer().getGameProfile());
                    t.addMember(player);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
    private static int acceptInvite(CommandSourceStack source){
        if(source.getEntity() instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                if(pd.hasTeamInvite()){
                    Team transfer = TeamManager.getTeam(pd.getTeamInvite());
                    if(pd.hasTeam()){
                        TeamManager.getTeam(pd.getTeamID()).removeMember(sp,source, LeaveReason.LEFT);
                    }
                    transfer.addMember(sp);
                }
                else{
                    LifelossChatMessage.from(ChatFormatting.RED,"<lifeloss.noteaminvite>").send(sp);
                }
            },ILLPlayerData.class);
        }
        else{
            onlyPlayers(source);
        }
        return 1;    }

    private static int sendInvites(Collection<ServerPlayer> targets, CommandSourceStack source){
        if(source.getEntity() instanceof ServerPlayer sp){
            SyncablePlayerData.executeIfPresent(sp,(pd)->{
                if(pd.hasTeam()){
                    Team t = TeamManager.getTeam(pd.getTeamID());
                    BetterChatMessage alert = LifelossChatMessage.from(ChatFormatting.GOLD,"<2lifeloss.teaminvitereceived>",sp.getDisplayName(),t.getName());
                    for (ServerPlayer invitee : targets) {
                        LifelossChatMessage.from(ChatFormatting.GOLD, "<2lifeloss.teaminvitesent>",sp.getDisplayName(),invitee.getDisplayName()).send(t.getOnlineMembers());
                        SyncablePlayerData.executeIfPresent(invitee,(idat)->{
                            alert.send(invitee);
                            idat.setTeamInvite(pd.getTeamID());
                        },ILLPlayerData.class);
                    }
                }
                else{
                    LifelossChatMessage.from(ChatFormatting.RED,"<lifeloss.commandneedsteam>").send(sp);
                }
            },ILLPlayerData.class);
        }
        else{
            onlyPlayers(source);
        }
        return 1;
    }

    private static int seeLives(CommandSourceStack source) {
        try {
            if(source.getEntity() instanceof ServerPlayer) {
                SyncablePlayerData.executeIfPresent(source.getEntity(), pdata -> {
                    LifelossChatMessage.from(ChatFormatting.GREEN, "<1lifeloss.seelivesself>", "" + pdata.getLives()).send(source);
                }, ILLPlayerData.class);
            }
            else onlyPlayers(source);
        } catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }
    private static void onlyPlayers(CommandSourceStack source){
        LifelossChatMessage.from(ChatFormatting.RED, "<lifeloss.onlyplayercommand>").send(source);
    }
    private static int seeLives(CommandSourceStack source, Collection<ServerPlayer> plrs) {
        try {
            for (ServerPlayer plr : plrs) {
                SyncablePlayerData.executeIfPresent(plr, pdata -> {
                    LifelossChatMessage.from(ChatFormatting.GREEN, "<2lifeloss.seelivesother>", plr.getDisplayName(), "" + pdata.getLives()).send(source);
                }, ILLPlayerData.class);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

    private static int giveLives(CommandSourceStack sender, Collection<ServerPlayer> players, int lives) {
        try {
            for (ServerPlayer player : players) {
                SyncablePlayerData.executeIfPresent(player, dat -> {
                    boolean wasDead = dat.isDead();
                    dat.growLives(lives);
                    if (lives > 1) {
                        LifelossChatMessage.from(ChatFormatting.GREEN, "<2lifeloss.givelives>", player.getDisplayName(), "" + lives).send(sender);
                    } else
                        LifelossChatMessage.from(ChatFormatting.GREEN, "<1lifeloss.givelife>", player.getDisplayName()).send(sender);
                    if (wasDead) {
                        LLEventHandler.resurrect(player);
                    }
                }, ILLPlayerData.class);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return 1;
    }
}
