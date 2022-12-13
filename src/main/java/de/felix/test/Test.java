package de.felix.test;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.lang.reflect.Array;
import java.util.ArrayList;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.world.GameMode;
import org.lwjgl.glfw.GLFW;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Implement ModInitializer interface
@Environment(EnvType.CLIENT)
public class Test implements ClientModInitializer {
    private static final KeyBinding FLY_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("Fly", GLFW.GLFW_KEY_F, "VibeClient Movement"));
    private static final KeyBinding NOFALL_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("NoFall", GLFW.GLFW_KEY_N, "VibeClient Movement"));
    private static final KeyBinding MULTIAURA_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("MultiAura", GLFW.GLFW_KEY_R, "VibeClient Combat"));
    private static final KeyBinding XRAY_KEYBINDING = KeyBindingHelper.registerKeyBinding(new KeyBinding("X-Ray", GLFW.GLFW_KEY_X, "VibeClient Visuals"));
    static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final boolean isFallingFastEnoughToCauseDamage(ClientPlayerEntity player) {
        return player.getVelocity().getY() < -0.5;
    }
    public static final class Rotation {
        private final float yaw;
        private final float pitch;
        public Rotation(float yaw, float pitch) {
            this.yaw = MathHelper.wrapDegrees(yaw);
            this.pitch = MathHelper.wrapDegrees(pitch);
        }
        public float getYaw() {
            return yaw;
        }
        public float getPitch() {
            return pitch;
        }
    }
    public static BlockState getState(BlockPos pos)
    {
        return MC.world.getBlockState(pos);
    }
    private static VoxelShape getOutlineShape(BlockPos pos) {
        return getState(pos).getOutlineShape(MC.world, pos);
    }

    public static Box getBoundingBox(BlockPos pos) {
        return getOutlineShape(pos).getBoundingBox().offset(pos);
    }
    public static Vec3d getEyesPos() {
        ClientPlayerEntity player = MC.player;

        return new Vec3d(player.getX(), player.getY() + player.getEyeHeight(player.getPose()), player.getZ());
    }
    public static Rotation getNeededRotations(Vec3d vec) {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new Rotation(yaw, pitch);
    }
    public boolean doCritical() {
        if (!MC.player.isOnGround()) {
            return false;
        }
        if (MC.player.isTouchingWater() || MC.player.isInLava()) {
            return false;
        }

        return true;
    }
    public void doFly() {
        ClientPlayerEntity player = MC.player;
        PlayerAbilities abilities = player.getAbilities();
        abilities.flying = true;
        if (abilities.flying) {
            if (player.getPos().getY() >= oldY-0.0433D) {
                tickCounter++;
            }

            oldY = player.getPos().getY();
            if (tickCounter > 20 && player.world.getBlockState(new BlockPos(player.getPos().subtract(0,0.0433D,0))).isAir()) {
                PacketHelper.sendPosition(player.getPos().subtract(0.0,0.05,0.0), false);
                tickCounter = 0;
            }
        }
    }
    public void doNoFall() {
        ClientPlayerEntity player = MC.player;
        if (player.fallDistance <= (player.isFallFlying() ? 1 : 2)) {
            return;
        }

        if (player.isFallFlying() && !isFallingFastEnoughToCauseDamage(player)) {
            return;
        }

        PacketHelper.sendOnGround(true);
    }
    public void doMultiAura() {
        ClientPlayerEntity player = MC.player;
        ClientWorld world = MC.world;

        if (!(player.getAttackCooldownProgress(0) >= 1)) {
            return;
        }

        Stream<Entity> stream = StreamSupport.stream(world.getEntities().spliterator(), true)
                .filter(e -> !e.isRemoved())
                .filter(e -> e instanceof LivingEntity && ((LivingEntity)e).getHealth() > 0 || e instanceof EndCrystalEntity || e instanceof ShulkerBulletEntity)
                .filter(e -> player.squaredDistanceTo(e) <= rangeSq)
                .filter(e -> e != player);

        ArrayList<Entity> entities = stream.collect(Collectors.toCollection(ArrayList::new));

        if (entities.isEmpty()) {
            return;
        }

        for(Entity entity : entities) {
            //Rotation rotations = getNeededRotations(entity.getBoundingBox().getCenter());
            //PacketHelper.sendLook(rotations.getYaw(), rotations.getPitch(), player.isOnGround());
            MC.interactionManager.attackEntity(player, entity);
        }
    }
    private static final double rangeSq = Math.pow(4.5, 2);
    public static boolean flyEnabled = false;
    public static boolean noFallEnabled = false;
    public static boolean multiAuraEnabled = false;
    public static boolean xRayEnabled = false;
    public static ArrayList xRayBlocks = new ArrayList();
    private Double defaultGamma;
    private double oldY = 0.0;
    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        MinecraftClient MC = MinecraftClient.getInstance();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = MC.player;
            ClientWorld world = MC.world;
            if (player != null) {
                PlayerAbilities abilities = player.getAbilities();
                client.interactionManager.setGameMode(GameMode.SURVIVAL);
                while (FLY_KEYBINDING.wasPressed()) {
                    player.sendMessage(Text.of("Flying " + (flyEnabled ? "Disabled!" : "Enabled!")), false);

                    flyEnabled = !flyEnabled;
                    //player.getAbilities().flying = !flyEnabled ? flyEnabled : player.getAbilities().flying;
                    if (flyEnabled) {
                        abilities.flying = true;
                        tickCounter = 0;
                    } else {
                        boolean creative = player.isCreative();
                        abilities.flying = creative && !player.isOnGround();
                        abilities.allowFlying = creative;

                        MC.options.jumpKey.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_SPACE));
                        MC.options.sneakKey.setPressed(InputUtil.isKeyPressed(MC.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT));
                    }
                    //if (!flyEnabled && player.getAbilities().flying) {player.getAbilities().flying = flyEnabled;}
                }

                while (NOFALL_KEYBINDING.wasPressed()) {
                    player.sendMessage(Text.of("NoFall " + (noFallEnabled ? "Disabled!" : "Enabled!")), false);

                    noFallEnabled = !noFallEnabled;
                }

                while (MULTIAURA_KEYBINDING.wasPressed()) {
                    player.sendMessage(Text.of("MultiAura " + (multiAuraEnabled ? "Disabled!" : "Enabled!")), false);

                    multiAuraEnabled = !multiAuraEnabled;
                }

                while (XRAY_KEYBINDING.wasPressed()) {
                    player.sendMessage(Text.of("X-Ray " + (xRayEnabled ? "Disabled!" : "Enabled!")), false);

                    xRayEnabled = !xRayEnabled;
                    if (xRayEnabled) {
                        defaultGamma = client.options.getGamma().getValue();
                        client.options.getGamma().setValue(10.0);
                        player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, Integer.MAX_VALUE));
                        client.worldRenderer.reload();
                    } else {
                        client.options.getGamma().setValue(defaultGamma);
                        player.removeStatusEffect(StatusEffects.NIGHT_VISION);
                        client.worldRenderer.reload();
                    }
                }

                if (flyEnabled) {
                    doFly();
                }

                if (noFallEnabled) {
                    doNoFall();
                }

                if (multiAuraEnabled) {
                    doMultiAura();
                }
            }
        });
    }
}