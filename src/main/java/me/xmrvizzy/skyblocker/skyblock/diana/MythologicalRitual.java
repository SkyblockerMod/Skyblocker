package me.xmrvizzy.skyblocker.skyblock.diana;

import com.mojang.brigadier.Command;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanMutablePair;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MythologicalRitual {
    private static final Map<BlockPos, BooleanBooleanMutablePair> particlesMap = new HashMap<>();
    private static final Set<BlockPos> griffinBurrows = new HashSet<>();

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MythologicalRitual::render);
        AttackBlockCallback.EVENT.register(MythologicalRitual::onAttackBlock);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                literal(SkyblockerMod.NAMESPACE).then(literal("diana").then(literal("clearGriffinBurrows").executes(context -> {
                    griffinBurrows.clear();
                    return Command.SINGLE_SUCCESS;
                }))))
        );
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (isActive()&& ParticleTypes.CRIT.equals(packet.getParameters().getType()) || ParticleTypes.ENCHANT.equals(packet.getParameters().getType())) {
            BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());
            BooleanBooleanMutablePair particlesAtPos = particlesMap.computeIfAbsent(pos, pos1 -> BooleanBooleanMutablePair.of(false, false));
            particlesAtPos.left(particlesAtPos.leftBoolean() || ParticleTypes.CRIT.equals(packet.getParameters().getType()));
            particlesAtPos.right(particlesAtPos.rightBoolean() || ParticleTypes.ENCHANT.equals(packet.getParameters().getType()));
            if (particlesAtPos.leftBoolean() && particlesAtPos.rightBoolean()) {
                griffinBurrows.add(pos);
            }
        }
    }

    public static void render(WorldRenderContext context) {
        if (isActive()) {
            for (BlockPos griffinBorrow : griffinBurrows) {
                RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, griffinBorrow, DyeColor.GREEN.getColorComponents(), 0.5F);
            }
        }
    }

    public static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        if (isActive()) {
            griffinBurrows.remove(pos);
        }
        return ActionResult.PASS;
    }

    private static boolean isActive() {
        return SkyblockerConfig.get().general.fairySouls.enableFairySoulsHelper && Utils.getLocationRaw().equals("hub") ; // TODO Change to actual config option
    }
}
