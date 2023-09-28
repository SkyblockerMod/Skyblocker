package me.xmrvizzy.skyblocker.skyblock.diana;

import com.mojang.brigadier.Command;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MythologicalRitual {
    private static final Pattern GRIFFIN_BURROW_DUG = Pattern.compile("(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d)/4\\)");
    private static final float[] WHITE_COLOR_COMPONENTS = {1.0f, 1.0f, 1.0f};
    private static final Map<BlockPos, GriffinBurrow> griffinBurrows = new HashMap<>();
    @Nullable
    private static BlockPos lastDugBurrowPos;
    private static GriffinBurrow previousBurrow;

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MythologicalRitual::render);
        AttackBlockCallback.EVENT.register(MythologicalRitual::onAttackBlock);
        UseBlockCallback.EVENT.register(MythologicalRitual::onUseBlock);
        ClientReceiveMessageEvents.GAME.register(MythologicalRitual::onChatMessage);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("diana")
                .then(literal("clearGriffinBurrows").executes(context -> {
                    griffinBurrows.clear();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("clearGriffinBurrow")
                        .then(argument("pos", BlockPosArgumentType.blockPos()).executes(context -> {
                            griffinBurrows.remove(context.getArgument("pos", BlockPos.class));
                            return Command.SINGLE_SUCCESS;
                        }))
                )
        )));
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (isActive()) {
            if (ParticleTypes.CRIT.equals(packet.getParameters().getType()) || ParticleTypes.ENCHANT.equals(packet.getParameters().getType())) {
                BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).down();
                if (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
                    return;
                }
                GriffinBurrow burrow = griffinBurrows.computeIfAbsent(pos, pos1 -> new GriffinBurrow());
                if (ParticleTypes.CRIT.equals(packet.getParameters().getType())) burrow.critParticle++;
                if (ParticleTypes.ENCHANT.equals(packet.getParameters().getType())) burrow.enchantParticle++;
                if (burrow.critParticle >= 5 && burrow.enchantParticle >= 5 && burrow.confirmed == TriState.FALSE) {
                    griffinBurrows.get(pos).init();
                }
            } else if (ParticleTypes.DUST.equals(packet.getParameters().getType())) {
                BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).down(2);
                GriffinBurrow burrow = griffinBurrows.get(pos);
                if (burrow == null) {
                    return;
                }
                burrow.regression.addData(packet.getX(), packet.getZ());
                double slope = burrow.regression.getSlope();
                if (Double.isNaN(slope)) {
                    return;
                }
                Vec3d nextBurrowDirection = new Vec3d(100, 2, slope * 100).normalize().multiply(500);
                burrow.nextBurrowPlane = new Vec3d[]{
                        Vec3d.of(pos).add(nextBurrowDirection),
                        Vec3d.of(pos).subtract(nextBurrowDirection)
                };
            }
        }
    }

    public static void render(WorldRenderContext context) {
        if (isActive()) {
            for (Map.Entry<BlockPos, GriffinBurrow> burrowEntry : griffinBurrows.entrySet()) {
                GriffinBurrow burrow = burrowEntry.getValue();
                if (burrow.confirmed == TriState.TRUE) {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, burrowEntry.getKey(), DyeColor.GREEN.getColorComponents(), 0.5F);
                }
                if (burrow.confirmed != TriState.FALSE && burrow.nextBurrowPlane != null) { // TODO try before debug render?
                    RenderHelper.renderLinesFromPoints(context, burrow.nextBurrowPlane, WHITE_COLOR_COMPONENTS, 1, 5);
                }
            }
        }
    }

    public static ActionResult onAttackBlock(PlayerEntity player, World world, Hand hand, BlockPos pos, Direction direction) {
        return onInteractBlock(pos);
    }

    public static ActionResult onUseBlock(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        return onInteractBlock(hitResult.getBlockPos());
    }

    @NotNull
    private static ActionResult onInteractBlock(BlockPos pos) {
        if (isActive() && griffinBurrows.containsKey(pos)) {
            lastDugBurrowPos = pos;
        }
        return ActionResult.PASS;
    }

    public static void onChatMessage(Text message, boolean overlay) {
        if (isActive() && GRIFFIN_BURROW_DUG.matcher(message.getString()).matches()) {
            if (previousBurrow != null) {
                previousBurrow.confirmed = TriState.FALSE;
            }
            previousBurrow = griffinBurrows.get(lastDugBurrowPos);
            previousBurrow.confirmed = TriState.DEFAULT;
        }
    }

    private static boolean isActive() {
        return SkyblockerConfig.get().general.fairySouls.enableFairySoulsHelper && Utils.getLocationRaw().equals("hub"); // TODO Change to actual config option
    }

    private static class GriffinBurrow {
        private int critParticle;
        private int enchantParticle;
        private TriState confirmed = TriState.FALSE;
        private final SimpleRegression regression = new SimpleRegression();
        private Vec3d[] nextBurrowPlane;

        private void init() {
            confirmed = TriState.TRUE;
            regression.clear();
        }
    }
}
