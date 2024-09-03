package de.hysky.skyblocker.skyblock.end;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.ints.IntIntMutablePair;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.HashMap;
import java.util.Map;

public class EnderNodes {
    private static final MinecraftClient client = MinecraftClient.getInstance();
    private static final Map<BlockPos, EnderNode> enderNodes = new HashMap<>();

    @Init
    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(EnderNodes::update, 20);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(EnderNodes::render);
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            enderNodes.remove(pos);
            return ActionResult.PASS;
        });
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (!shouldProcess()) return;
        ParticleType<?> particleType = packet.getParameters().getType();
        if (!ParticleTypes.PORTAL.getType().equals(particleType) && !ParticleTypes.WITCH.getType().equals(particleType))
            return;

        double x = packet.getX();
        double y = packet.getY();
        double z = packet.getZ();
        double xFrac = MathHelper.floorMod(x, 1);
        double yFrac = MathHelper.floorMod(y, 1);
        double zFrac = MathHelper.floorMod(z, 1);
        BlockPos pos;
        Direction direction;
        if (yFrac == 0.25) {
            pos = BlockPos.ofFloored(x, y - 1, z);
            direction = Direction.UP;
        } else if (yFrac == 0.75) {
            pos = BlockPos.ofFloored(x, y + 1, z);
            direction = Direction.DOWN;
        } else if (xFrac == 0.25) {
            pos = BlockPos.ofFloored(x - 1, y, z);
            direction = Direction.EAST;
        } else if (xFrac == 0.75) {
            pos = BlockPos.ofFloored(x + 1, y, z);
            direction = Direction.WEST;
        } else if (zFrac == 0.25) {
            pos = BlockPos.ofFloored(x, y, z - 1);
            direction = Direction.SOUTH;
        } else if (zFrac == 0.75) {
            pos = BlockPos.ofFloored(x, y, z + 1);
            direction = Direction.NORTH;
        } else {
            return;
        }

        EnderNode enderNode = enderNodes.computeIfAbsent(pos, EnderNode::new);
        IntIntPair particles = enderNode.particles.get(direction);
        particles.left(particles.leftInt() + 1);
        particles.right(particles.rightInt() + 1);
    }

    private static void update() {
        if (shouldProcess()) {
            for (EnderNode enderNode : enderNodes.values()) {
                enderNode.updateParticles();
            }
        }
    }

    private static void render(WorldRenderContext context) {
        if (shouldProcess()) {
            for (EnderNode enderNode : enderNodes.values()) {
                if (enderNode.shouldRender()) {
                    enderNode.render(context);
                }
            }
        }
    }

    private static boolean shouldProcess() {
        return SkyblockerConfigManager.get().otherLocations.end.enableEnderNodeHelper && Utils.isInTheEnd();
    }

    private static void reset() {
        enderNodes.clear();
    }

    public static class EnderNode extends Waypoint {
        private final Map<Direction, IntIntPair> particles = Map.of(
                Direction.UP, new IntIntMutablePair(0, 0),
                Direction.DOWN, new IntIntMutablePair(0, 0),
                Direction.EAST, new IntIntMutablePair(0, 0),
                Direction.WEST, new IntIntMutablePair(0, 0),
                Direction.SOUTH, new IntIntMutablePair(0, 0),
                Direction.NORTH, new IntIntMutablePair(0, 0)
        );
        private long lastConfirmed;

        private EnderNode(BlockPos pos) {
            super(pos, () -> SkyblockerConfigManager.get().uiAndVisuals.waypoints.waypointType, ColorUtils.getFloatComponents(DyeColor.CYAN), false);
        }

        private void updateParticles() {
            long currentTimeMillis = System.currentTimeMillis();
            if (lastConfirmed + 2000 > currentTimeMillis || client.world == null || !particles.entrySet().stream().allMatch(entry -> entry.getValue().leftInt() >= 5 && entry.getValue().rightInt() >= 5 || !client.world.getBlockState(pos.offset(entry.getKey())).isAir())) return;
            lastConfirmed = currentTimeMillis;
            for (Map.Entry<Direction, IntIntPair> entry : particles.entrySet()) {
                entry.getValue().left(0);
                entry.getValue().right(0);
            }
        }

        @Override
        public boolean shouldRender() {
            return super.shouldRender() && lastConfirmed + 5000 > System.currentTimeMillis();
        }
    }
}
