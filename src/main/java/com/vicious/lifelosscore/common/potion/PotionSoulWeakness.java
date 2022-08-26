package com.vicious.lifelosscore.common.potion;

import com.vicious.lifelosscore.common.util.LifelossChatMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionSoulWeakness extends MobEffect {

    protected PotionSoulWeakness() {
        super(MobEffectCategory.HARMFUL, 0xfdb5ff);
    }

    @Override
    public void applyEffectTick(@NotNull LivingEntity target, int amplifier) {
        if(amplifier >= 10){
            if(!target.isDeadOrDying()) target.hurt(new DamageSoulShatter(target.getKillCredit()),target.getMaxHealth()*(amplifier-9));
        }
    }

    @Override
    public void applyInstantenousEffect(@Nullable Entity source, @Nullable Entity potionEntity, @NotNull LivingEntity target, int strength, double splash) {
        super.applyInstantenousEffect(source, potionEntity, target, strength, splash);
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,  strength*300,strength));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,  strength*300,strength));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN,  strength*300,strength));
        if(strength >= 3){
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 5*(strength-1), strength-2));
        }
        if(strength >= 10){
            LifelossChatMessage.from(ChatFormatting.RED,"<lifelosscore:soulweaknessabsolution>");
        }
    }
}
