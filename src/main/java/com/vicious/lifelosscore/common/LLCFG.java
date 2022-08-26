package com.vicious.lifelosscore.common;

import com.vicious.viciouscore.common.util.file.ViciousDirectories;
import com.vicious.viciouslib.configuration.ConfigurationValue;
import com.vicious.viciouslib.configuration.JSONConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LLCFG extends JSONConfig {
    private static LLCFG instance;
    public List<Consumer<LLCFG>> changeListeners = new ArrayList<>();

    public static LLCFG getInstance() {
        if(instance == null) instance = new LLCFG();
        return instance;
    }
    public ConfigurationValue<Boolean> livesMode = add(new ConfigurationValue<>("LivesMode", ()->true,this).description("Enables the lives system."));
    public ConfigurationValue<Boolean> kyraMode = add(new ConfigurationValue<>("KyraMode", ()->false,this).description("If one person dies, everyone dies.")).parent(livesMode);
    public ConfigurationValue<Boolean> teamMode = add(new ConfigurationValue<>("TeamMode", ()->false,this).description("When added to a team, players will lose max health based on distance from each other."));
    public ConfigurationValue<Integer> healthLossStart = (add(new ConfigurationValue<>("HealthLossStartDistance", () -> 50, this).description("Controls the distance between teammates before health begins to reduce"))).parent(teamMode);
    public ConfigurationValue<Float> healthLossMultiplier = (add(new ConfigurationValue<>("HealthLossFactor", () -> 1.001F, this).description("Controls the rate at which max health is lost"))).parent(teamMode);
    public ConfigurationValue<Integer> healthLossGrace = (add(new ConfigurationValue<>("TicksHealthLossGrace", () -> 200, this).description("Controls time in seconds after entering loss zone before health will be lost."))).parent(teamMode);
    public ConfigurationValue<Integer> healthLossTeleportGrace = (add(new ConfigurationValue<>("TicksHealthLossTeleportGracePerBlock", () -> 20, this).description("Controls time in ticks per block after entering loss zone before health will be lost. (on login and on death)"))).parent(teamMode);
    public ConfigurationValue<Integer> healthLossGraceCooldown = (add(new ConfigurationValue<>("TicksHealthLossGraceCooldown", () -> 1200, this).description("Controls the time in seconds before grace effect can be reobtained."))).parent(teamMode);

    public ConfigurationValue<Boolean> debugMode = add(new ConfigurationValue<>("DebugMode", ()->false,this).description("Debug"));
    public LLCFG() {
        super(((Supplier<Path>) () -> {
            if(ViciousDirectories.configDirectory == null) ViciousDirectories.initializeConfigDependents();
            return Paths.get(ViciousDirectories.configDirectory.toAbsolutePath() + "/lifeloss.json");
        }).get());
    }

    @Override
    public void markDirty(String variablename, Object var) {
        super.markDirty(variablename, var);
        for (Consumer<LLCFG> changeListener : changeListeners) {
            changeListener.accept(this);
        }
    }
}
