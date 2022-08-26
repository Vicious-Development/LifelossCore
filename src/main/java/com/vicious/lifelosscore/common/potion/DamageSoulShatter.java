package com.vicious.lifelosscore.common.potion;

import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.entity.Entity;

public class DamageSoulShatter extends EntityDamageSource {
    public DamageSoulShatter(Entity e) {
        super("soulshatter",e);
    }

}
