package me.xmrvizzy.skyblocker.skyblock.diana;

import com.mojang.brigadier.Command;
import it.unimi.dsi.fastutil.booleans.BooleanBooleanMutablePair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
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
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MythologicalRitual {
    private static final Pattern GRIFFIN_BURROW_DUG = Pattern.compile("(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d)/4\\)");
    private static final Map<BlockPos, BooleanBooleanMutablePair> particlesMap = new HashMap<>();
    private static final Object2LongMap<BlockPos> griffinBurrows = new Object2LongOpenHashMap<>();
    @Nullable
    private static BlockPos lastDugBurrowPos;

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
                .then(literal("clearGriffinBurrow").then(argument("pos", BlockPosArgumentType.blockPos()).executes(context -> {
                    griffinBurrows.removeLong(context.getArgument("pos", BlockPos.class));
                    return Command.SINGLE_SUCCESS;
                }))))));
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (isActive() && ParticleTypes.CRIT.equals(packet.getParameters().getType()) || ParticleTypes.ENCHANT.equals(packet.getParameters().getType())) {
            BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).down();
            if (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
                return;
            }
            BooleanBooleanMutablePair particlesAtPos = particlesMap.computeIfAbsent(pos, pos1 -> BooleanBooleanMutablePair.of(false, false));
            particlesAtPos.left(particlesAtPos.leftBoolean() || ParticleTypes.CRIT.equals(packet.getParameters().getType()));
            particlesAtPos.right(particlesAtPos.rightBoolean() || ParticleTypes.ENCHANT.equals(packet.getParameters().getType()));
            if (particlesAtPos.leftBoolean() && particlesAtPos.rightBoolean() && griffinBurrows.getLong(pos) + 1000 < System.currentTimeMillis()) {
                griffinBurrows.put(pos, 0);
            }
        }
    }

    public static void render(WorldRenderContext context) {
        if (isActive()) {
            for (Object2LongMap.Entry<BlockPos> griffinBorrow : griffinBurrows.object2LongEntrySet()) {
                if (griffinBorrow.getLongValue() <= 0) {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, griffinBorrow.getKey(), DyeColor.GREEN.getColorComponents(), 0.5F);
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
            griffinBurrows.put(lastDugBurrowPos, System.currentTimeMillis());
        }
    }

    private static boolean isActive() {
        return SkyblockerConfig.get().general.fairySouls.enableFairySoulsHelper && Utils.getLocationRaw().equals("hub"); // TODO Change to actual config option
    }
}
