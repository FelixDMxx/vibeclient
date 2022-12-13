package de.felix.test.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.lang.reflect.Field;

@Environment(EnvType.CLIENT)
@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    private double roundCoordinate(double n) {
        n = Math.round(n * 100) / 100d;
        return Math.nextAfter(n, n + Math.signum(n));
    }
    @Inject(method = "sendImmediately", at = @At("HEAD"))
    private void sendImmediately(Packet<?> packet, @Nullable PacketCallbacks callbacks, CallbackInfo ci) {
        if (packet instanceof PlayerMoveC2SPacket) {
            PlayerMoveC2SPacket movePacket = (PlayerMoveC2SPacket) packet;
            try {
                Field xField = PlayerMoveC2SPacket.class.getDeclaredField("x");
                Field zField = PlayerMoveC2SPacket.class.getDeclaredField("z");

                xField.setAccessible(true);
                zField.setAccessible(true);

                xField.set(movePacket, roundCoordinate((double) xField.get(movePacket)));
                zField.set(movePacket, roundCoordinate((double) zField.get(movePacket)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        if (packet instanceof VehicleMoveC2SPacket) {
            VehicleMoveC2SPacket movePacket = (VehicleMoveC2SPacket) packet;
            try {
                Field xField = VehicleMoveC2SPacket.class.getDeclaredField("x");
                Field zField = VehicleMoveC2SPacket.class.getDeclaredField("z");

                xField.setAccessible(true);
                zField.setAccessible(true);

                xField.set(movePacket, roundCoordinate((double) xField.get(movePacket)));
                zField.set(movePacket, roundCoordinate((double) zField.get(movePacket)));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }
}