package de.felix.test.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.border.WorldBorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldBorder.class)
@Environment(EnvType.CLIENT)
public class WorldBorderMixin {
    @Inject(method = "setSize", at = @At("HEAD"), cancellable = true)
    private void setSize(double size, CallbackInfo ci) {
        ci.cancel();
    }
}