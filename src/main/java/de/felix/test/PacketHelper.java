package de.felix.test;

import de.felix.test.mixin.ClientConnectionInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class PacketHelper {
    private static double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
    public static void sendPosition(Vec3d pos, boolean onGround) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientConnectionInvoker conn = (ClientConnectionInvoker)client.player.networkHandler.getConnection();
        Packet packet = new PlayerMoveC2SPacket.PositionAndOnGround(roundToTwoDecimals(pos.getX()), roundToTwoDecimals(pos.getY()), roundToTwoDecimals(pos.getZ()), onGround);
        conn.sendIm(packet, null);
    }
    public static void sendLook(float Yaw, float Pitch, boolean onGround) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientConnectionInvoker conn = (ClientConnectionInvoker)client.player.networkHandler.getConnection();
        Packet packet = new PlayerMoveC2SPacket.LookAndOnGround(Yaw, Pitch, onGround);
        conn.sendIm(packet, null);
    }
    public static void sendOnGround(boolean bool) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientConnectionInvoker conn = (ClientConnectionInvoker)client.player.networkHandler.getConnection();
        Packet packet = new PlayerMoveC2SPacket.OnGroundOnly(bool);
        conn.sendIm(packet, null);
    }
}
