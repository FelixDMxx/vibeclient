package de.felix.test.mixin;

import de.felix.test.Test;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class TestMixin {
    @Inject(at = @At("HEAD"), method = "init()V")
    public void init(CallbackInfo ci) {
    }


}
