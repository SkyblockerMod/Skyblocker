package de.hysky.skyblocker.skyblock.waypoint;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
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
    private static final float[] ORANGE_COLOR_COMPONENTS = DyeColor.ORANGE.getColorComponents();
    private static long lastEchoTime;
    private static final Map<BlockPos, GriffinBurrow> griffinBurrows = new HashMap<>();
    @Nullable
    private static BlockPos lastDugBurrowPos;
    private static GriffinBurrow previousBurrow = new GriffinBurrow(BlockPos.ORIGIN);

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MythologicalRitual::render);
        AttackBlockCallback.EVENT.register(MythologicalRitual::onAttackBlock);
        UseBlockCallback.EVENT.register(MythologicalRitual::onUseBlock);
        UseItemCallback.EVENT.register(MythologicalRitual::onUseItem);
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

        // Put a root burrow so echo detection works without a previous burrow
        previousBurrow.confirmed = TriState.DEFAULT;
        griffinBurrows.put(BlockPos.ORIGIN, previousBurrow);
    }

    public static void onParticle(ParticleS2CPacket packet) {
        if (isActive()) {
            if (ParticleTypes.CRIT.equals(packet.getParameters().getType()) || ParticleTypes.ENCHANT.equals(packet.getParameters().getType())) {
                BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ()).down();
                if (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world.getBlockState(pos).isOf(Blocks.GRASS_BLOCK)) {
                    return;
                }
                GriffinBurrow burrow = griffinBurrows.computeIfAbsent(pos, GriffinBurrow::new);
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
                Vec3d nextBurrowDirection = new Vec3d(100, 0, slope * 100).normalize();
                if (burrow.nextBurrowLine == null) {
                    burrow.nextBurrowLine = new Vec3d[1001];
                }
                fillLine(burrow.nextBurrowLine, Vec3d.of(pos), nextBurrowDirection);
            } else if (ParticleTypes.DRIPPING_LAVA.equals(packet.getParameters().getType()) && packet.getCount() == 2) {
                if (System.currentTimeMillis() > lastEchoTime + 10_000) {
                    return;
                }
                if (previousBurrow.echoBurrowDirection == null) {
                    previousBurrow.echoBurrowDirection = new Vec3d[2];
                }
                previousBurrow.echoBurrowDirection[0] = previousBurrow.echoBurrowDirection[1];
                previousBurrow.echoBurrowDirection[1] = new Vec3d(packet.getX(), packet.getY(), packet.getZ());
                if (previousBurrow.echoBurrowDirection[0] == null || previousBurrow.echoBurrowDirection[1] == null) {
                    return;
                }
                Vec3d echoBurrowDirection = previousBurrow.echoBurrowDirection[1].subtract(previousBurrow.echoBurrowDirection[0]).normalize();
                if (previousBurrow.echoBurrowLine == null) {
                    previousBurrow.echoBurrowLine = new Vec3d[1001];
                }
                fillLine(previousBurrow.echoBurrowLine, previousBurrow.echoBurrowDirection[0], echoBurrowDirection);
            }
        }
    }

    static void fillLine(Vec3d[] line, Vec3d start, Vec3d direction) {
        assert line.length % 2 == 1;
        int middle = line.length / 2;
        line[middle] = start;
        for (int i = 0; i < middle; i++) {
            line[middle + 1 + i] = line[middle + i].add(direction);
            line[middle - 1 - i] = line[middle - i].subtract(direction);
        }
    }

    public static void render(WorldRenderContext context) {
        if (isActive()) {
            for (GriffinBurrow burrow : griffinBurrows.values()) {
                if (burrow.shouldRender()) {
                    burrow.render(context);
                }
                if (burrow.confirmed != TriState.FALSE) {
                    if (burrow.nextBurrowLine != null) {
                        RenderHelper.renderLinesFromPoints(context, burrow.nextBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F);
                    }
                    if (burrow.echoBurrowLine != null) {
                        RenderHelper.renderLinesFromPoints(context, burrow.echoBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F);
                    }
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

    public static TypedActionResult<ItemStack> onUseItem(PlayerEntity player, World world, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (isActive() && ItemUtils.getItemId(stack).equals("ANCESTRAL_SPADE")) {
            lastEchoTime = System.currentTimeMillis();
        }
        return TypedActionResult.pass(stack);
    }

    public static void onChatMessage(Text message, boolean overlay) {
        if (isActive() && GRIFFIN_BURROW_DUG.matcher(message.getString()).matches()) {
            previousBurrow.confirmed = TriState.FALSE;
            previousBurrow = griffinBurrows.get(lastDugBurrowPos);
            previousBurrow.confirmed = TriState.DEFAULT;
        }
    }

    private static boolean isActive() {
        return SkyblockerConfigManager.get().general.mythologicalRitual.enableMythologicalRitualHelper && Utils.getLocationRaw().equals("hub");
    }

    private static class GriffinBurrow extends Waypoint {
        private int critParticle;
        private int enchantParticle;
        private TriState confirmed = TriState.FALSE;
        private final SimpleRegression regression = new SimpleRegression();
        @Nullable
        private Vec3d[] nextBurrowLine;
        @Nullable
        private Vec3d[] echoBurrowDirection;
        @Nullable
        private Vec3d[] echoBurrowLine;

        private GriffinBurrow(BlockPos pos) {
            super(pos, Type.WAYPOINT, ORANGE_COLOR_COMPONENTS, 0.25F);
        }

        private void init() {
            confirmed = TriState.TRUE;
            regression.clear();
        }

        @Override
        public boolean shouldRender() {
            return super.shouldRender() && confirmed == TriState.TRUE;
        }
    }
}
