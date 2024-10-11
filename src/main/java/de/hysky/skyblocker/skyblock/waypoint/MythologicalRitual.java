package de.hysky.skyblocker.skyblock.waypoint;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.ParticleType;
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
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
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
    private static final float[] ORANGE_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.ORANGE);
    private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
    private static long lastEchoTime;
    private static final Map<BlockPos, GriffinBurrow> griffinBurrows = new HashMap<>();
    @Nullable
    private static BlockPos lastDugBurrowPos;
    private static GriffinBurrow previousBurrow = new GriffinBurrow(BlockPos.ORIGIN);

    @Init
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MythologicalRitual::render);
        AttackBlockCallback.EVENT.register(MythologicalRitual::onAttackBlock);
        UseBlockCallback.EVENT.register(MythologicalRitual::onUseBlock);
        UseItemCallback.EVENT.register(MythologicalRitual::onUseItem);
        ClientReceiveMessageEvents.GAME.register(MythologicalRitual::onChatMessage);
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("diana")
                .then(literal("clearGriffinBurrows").executes(context -> {
                    reset();
                    return Command.SINGLE_SUCCESS;
                }))
                .then(literal("clearGriffinBurrow")
                        .then(argument("position", ClientBlockPosArgumentType.blockPos()).executes(context -> {
                            griffinBurrows.remove(context.getArgument("position", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource()));
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
            switch (packet.getParameters().getType()) {
                case ParticleType<?> type when ParticleTypes.CRIT.equals(type) || ParticleTypes.ENCHANT.equals(type) -> handleBurrowParticle(packet);
                case ParticleType<?> type when ParticleTypes.DUST.equals(type) -> handleNextBurrowParticle(packet);
                case ParticleType<?> type when ParticleTypes.DRIPPING_LAVA.equals(type) && packet.getCount() == 2 -> handleEchoBurrowParticle(packet);
                case null, default -> {}
            }
        }
    }

    /**
     * Updates the crit and enchant particle counts and initializes the burrow if both counts are greater or equal to 5.
     */
    private static void handleBurrowParticle(ParticleS2CPacket packet) {
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
    }

    /**
     * Updates the regression of the burrow (if a burrow exists), tries to {@link #estimateNextBurrow(GriffinBurrow) estimate the next burrow}, and updates the line in the direction of the next burrow.
     */
    private static void handleNextBurrowParticle(ParticleS2CPacket packet) {
        BlockPos pos = BlockPos.ofFloored(packet.getX(), packet.getY(), packet.getZ());
        GriffinBurrow burrow = griffinBurrows.get(pos.down(2));
        if (burrow == null) {
            return;
        }
        burrow.regression.addData(packet.getX(), packet.getZ());
        double slope = burrow.regression.getSlope();
        if (Double.isNaN(slope)) {
            return;
        }
        Vec3d nextBurrowDirection = new Vec3d(100, 0, slope * 100).normalize();

        // Save the line of the next burrow and try to estimate the next burrow
        Vector2D pos2D = new Vector2D(pos.getX() + 0.5, pos.getZ() + 0.5);
        burrow.nextBurrowLineEstimation = new Line(pos2D, pos2D.add(new Vector2D(nextBurrowDirection.x, nextBurrowDirection.z)), 0.0001);
        estimateNextBurrow(burrow);

        // Fill line in the direction of the next burrow
        if (burrow.nextBurrowLine == null) {
            burrow.nextBurrowLine = new Vec3d[1001];
        }
        fillLine(burrow.nextBurrowLine, Vec3d.ofCenter(pos.up()), nextBurrowDirection);
    }

    /**
     * Saves the echo particle in {@link GriffinBurrow#echoBurrowDirection}, if the player used echo within 10 seconds.
     * Tries to {@link #estimateNextBurrow(GriffinBurrow) estimate the next burrow} and updates the line through the two echo burrow particles if there is already a particle saved in {@link GriffinBurrow#echoBurrowDirection}.
     */
    private static void handleEchoBurrowParticle(ParticleS2CPacket packet) {
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

        // Save the line of the echo burrow and try to estimate the next burrow
        Vector2D pos1 = new Vector2D(previousBurrow.echoBurrowDirection[0].x, previousBurrow.echoBurrowDirection[0].z);
        Vector2D pos2 = new Vector2D(previousBurrow.echoBurrowDirection[1].x, previousBurrow.echoBurrowDirection[1].z);
        previousBurrow.echoBurrowLineEstimation = new Line(pos1, pos2, 0.0001);
        estimateNextBurrow(previousBurrow);

        // Fill line in the direction of the echo burrow
        Vec3d echoBurrowDirection = previousBurrow.echoBurrowDirection[1].subtract(previousBurrow.echoBurrowDirection[0]).normalize();
        if (previousBurrow.echoBurrowLine == null) {
            previousBurrow.echoBurrowLine = new Vec3d[1001];
        }
        fillLine(previousBurrow.echoBurrowLine, previousBurrow.echoBurrowDirection[0], echoBurrowDirection);
    }

    /**
     * Tries to estimate the position of the next burrow
     * by intersecting the line of the next burrow and
     * the line of the echo burrow and saves the result in the burrow.
     * @param burrow The burrow to estimate the next burrow for
     */
    private static void estimateNextBurrow(GriffinBurrow burrow) {
        if (burrow.nextBurrowLineEstimation == null || burrow.echoBurrowLineEstimation == null) {
            return;
        }
        Vector2D intersection = burrow.nextBurrowLineEstimation.intersection(burrow.echoBurrowLineEstimation);
        burrow.nextBurrowEstimatedPos = BlockPos.ofFloored(intersection.getX(), 5, intersection.getY());
    }

    /**
     * Fills the {@link Vec3d} array to form a line centered on {@code start} with step sizes of {@code direction}
     * @param line The line to fill
     * @param start The center of the line
     * @param direction The step size of the line
     */
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
                        RenderHelper.renderLinesFromPoints(context, burrow.nextBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F, false);
                    }
                    if (burrow.echoBurrowLine != null) {
                        RenderHelper.renderLinesFromPoints(context, burrow.echoBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F, false);
                    }
                    if (burrow.nextBurrowEstimatedPos != null && burrow.confirmed == TriState.DEFAULT) {
                        RenderHelper.renderFilledWithBeaconBeam(context, burrow.nextBurrowEstimatedPos, RED_COLOR_COMPONENTS, 0.5f, true);
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
        return SkyblockerConfigManager.get().helpers.mythologicalRitual.enableMythologicalRitualHelper && Utils.getLocation() == Location.HUB;
    }

    private static void reset() {
        griffinBurrows.clear();
        lastDugBurrowPos = null;
        previousBurrow = new GriffinBurrow(BlockPos.ORIGIN);

        // Put a root burrow so echo detection works without a previous burrow
        previousBurrow.confirmed = TriState.DEFAULT;
        griffinBurrows.put(BlockPos.ORIGIN, previousBurrow);
    }

    private static class GriffinBurrow extends Waypoint {
        private int critParticle;
        private int enchantParticle;
        /**
         * The state of the burrow where {@link TriState#FALSE} means the burrow has been dug, is not the last dug burrow, and should not be rendered,
         * {@link TriState#DEFAULT} means the burrow is not confirmed by particles or
         * has been dug but is the last dug burrow and has to render the line pointing to the next burrow and the line from echo burrow, and
         * {@link TriState#TRUE} means the burrow is confirmed by particles and is waiting to be dug.
         */
        private TriState confirmed = TriState.FALSE;
        private final SimpleRegression regression = new SimpleRegression();
        @Nullable
        private Vec3d[] nextBurrowLine;
        /**
         * The positions of the last two echo burrow particles.
         */
        @Nullable
        private Vec3d[] echoBurrowDirection;
        @Nullable
        private Vec3d[] echoBurrowLine;
        @Nullable
        private BlockPos nextBurrowEstimatedPos;
        /**
         * The line in the direction of the next burrow estimated by the previous burrow particles.
         */
        @Nullable
        private Line nextBurrowLineEstimation;
        /**
         * The line in the direction of the next burrow estimated by the echo ability.
         */
        @Nullable
        private Line echoBurrowLineEstimation;

        private GriffinBurrow(BlockPos pos) {
            super(pos, Type.WAYPOINT, ORANGE_COLOR_COMPONENTS, 0.25F);
        }

        private void init() {
            confirmed = TriState.TRUE;
            regression.clear();
        }

        /**
         * @return {@code true} only if the burrow is confirmed by particles and is waiting to be dug
         */
        @Override
        public boolean shouldRender() {
            return super.shouldRender() && confirmed == TriState.TRUE;
        }
    }
}
