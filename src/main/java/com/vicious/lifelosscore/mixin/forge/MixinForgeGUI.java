package com.vicious.lifelosscore.mixin.forge;

import net.minecraftforge.client.gui.overlay.ForgeGui;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ForgeGui.class)
public class MixinForgeGUI {
    @ModifyArg(method = "renderHealth", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(FF)F"),index = 1,remap = false)
    public float set0(float f){
        return 0;
    }
}
