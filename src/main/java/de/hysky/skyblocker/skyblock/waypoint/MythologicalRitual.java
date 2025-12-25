package de.hysky.skyblocker.skyblock.waypoint;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.ParticleEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.math3.geometry.euclidean.twod.Line;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class MythologicalRitual {
	private static final Pattern GRIFFIN_BURROW_DUG = Pattern.compile("(?<message>You dug out a Griffin Burrow!|You finished the Griffin burrow chain!) \\((?<index>\\d+)/(?<length>\\d+)\\)");
	private static final float[] ORANGE_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.ORANGE);
	private static final float[] RED_COLOR_COMPONENTS = ColorUtils.getFloatComponents(DyeColor.RED);
	private static final Set<String> SPADES = Set.of("ANCESTRAL_SPADE", "ARCHAIC_SPADE", "DEIFIC_SPADE");

	private static long lastEchoTime;
	private static final Map<BlockPos, GriffinBurrow> griffinBurrows = new HashMap<>();
	private static @Nullable BlockPos lastDugBurrowPos;
	private static GriffinBurrow previousBurrow = new GriffinBurrow(BlockPos.ZERO);

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(MythologicalRitual::extractRendering);
		AttackBlockCallback.EVENT.register(MythologicalRitual::onAttackBlock);
		UseBlockCallback.EVENT.register(MythologicalRitual::onUseBlock);
		UseItemCallback.EVENT.register(MythologicalRitual::onUseItem);
		ClientReceiveMessageEvents.ALLOW_GAME.register(MythologicalRitual::onChatMessage);
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
		ParticleEvents.FROM_SERVER.register(MythologicalRitual::onParticle);

		// Put a root burrow so echo detection works without a previous burrow
		previousBurrow.confirmed = TriState.DEFAULT;
		griffinBurrows.put(BlockPos.ZERO, previousBurrow);
	}

	private static void onParticle(ClientboundLevelParticlesPacket packet) {
		if (isActive()) {
			switch (packet.getParticle().getType()) {
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
	private static void handleBurrowParticle(ClientboundLevelParticlesPacket packet) {
		BlockPos pos = BlockPos.containing(packet.getX(), packet.getY(), packet.getZ()).below();
		if (Minecraft.getInstance().level == null || !Minecraft.getInstance().level.getBlockState(pos).is(Blocks.GRASS_BLOCK)) {
			return;
		}
		GriffinBurrow burrow = griffinBurrows.computeIfAbsent(pos, GriffinBurrow::new);
		if (ParticleTypes.CRIT.equals(packet.getParticle().getType())) burrow.critParticle++;
		if (ParticleTypes.ENCHANT.equals(packet.getParticle().getType())) burrow.enchantParticle++;
		if (burrow.critParticle >= 5 && burrow.enchantParticle >= 5 && burrow.confirmed == TriState.FALSE) {
			griffinBurrows.get(pos).init();
		}
	}

	/**
	 * Updates the regression of the burrow (if a burrow exists), tries to {@link #estimateNextBurrow(GriffinBurrow) estimate the next burrow}, and updates the line in the direction of the next burrow.
	 */
	private static void handleNextBurrowParticle(ClientboundLevelParticlesPacket packet) {
		BlockPos pos = BlockPos.containing(packet.getX(), packet.getY(), packet.getZ());
		GriffinBurrow burrow = griffinBurrows.get(pos.below(2));
		if (burrow == null) {
			return;
		}
		burrow.regression.addData(packet.getX(), packet.getZ());
		double slope = burrow.regression.getSlope();
		if (Double.isNaN(slope)) {
			return;
		}
		Vec3 nextBurrowDirection = new Vec3(100, 0, slope * 100).normalize();

		// Save the line of the next burrow and try to estimate the next burrow
		Vector2D pos2D = new Vector2D(pos.getX() + 0.5, pos.getZ() + 0.5);
		burrow.nextBurrowLineEstimation = new Line(pos2D, pos2D.add(new Vector2D(nextBurrowDirection.x, nextBurrowDirection.z)), 0.0001);
		estimateNextBurrow(burrow);

		// Fill line in the direction of the next burrow
		if (burrow.nextBurrowLine == null) {
			burrow.nextBurrowLine = new Vec3[1001];
		}
		fillLine(burrow.nextBurrowLine, Vec3.atCenterOf(pos.above()), nextBurrowDirection);
	}

	/**
	 * Saves the echo particle in {@link GriffinBurrow#echoBurrowDirection}, if the player used echo within 10 seconds.
	 * Tries to {@link #estimateNextBurrow(GriffinBurrow) estimate the next burrow} and updates the line through the two echo burrow particles if there is already a particle saved in {@link GriffinBurrow#echoBurrowDirection}.
	 */
	private static void handleEchoBurrowParticle(ClientboundLevelParticlesPacket packet) {
		if (System.currentTimeMillis() > lastEchoTime + 10_000) {
			return;
		}
		if (previousBurrow.echoBurrowDirection == null) {
			previousBurrow.echoBurrowDirection = new Vec3[2];
		}
		previousBurrow.echoBurrowDirection[0] = previousBurrow.echoBurrowDirection[1];
		previousBurrow.echoBurrowDirection[1] = new Vec3(packet.getX(), packet.getY(), packet.getZ());
		if (previousBurrow.echoBurrowDirection[0] == null || previousBurrow.echoBurrowDirection[1] == null) {
			return;
		}

		// Save the line of the echo burrow and try to estimate the next burrow
		Vector2D pos1 = new Vector2D(previousBurrow.echoBurrowDirection[0].x, previousBurrow.echoBurrowDirection[0].z);
		Vector2D pos2 = new Vector2D(previousBurrow.echoBurrowDirection[1].x, previousBurrow.echoBurrowDirection[1].z);
		previousBurrow.echoBurrowLineEstimation = new Line(pos1, pos2, 0.0001);
		estimateNextBurrow(previousBurrow);

		// Fill line in the direction of the echo burrow
		Vec3 echoBurrowDirection = previousBurrow.echoBurrowDirection[1].subtract(previousBurrow.echoBurrowDirection[0]).normalize();
		if (previousBurrow.echoBurrowLine == null) {
			previousBurrow.echoBurrowLine = new Vec3[1001];
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
		if (intersection == null) {
			return;
		}
		burrow.nextBurrowEstimatedPos = BlockPos.containing(intersection.getX(), 5, intersection.getY());
	}

	/**
	 * Fills the {@link Vec3} array to form a line centered on {@code start} with step sizes of {@code direction}
	 * @param line The line to fill
	 * @param start The center of the line
	 * @param direction The step size of the line
	 */
	static void fillLine(Vec3[] line, Vec3 start, Vec3 direction) {
		assert line.length % 2 == 1;
		int middle = line.length / 2;
		line[middle] = start;
		for (int i = 0; i < middle; i++) {
			line[middle + 1 + i] = line[middle + i].add(direction);
			line[middle - 1 - i] = line[middle - i].subtract(direction);
		}
	}

	public static void extractRendering(PrimitiveCollector collector) {
		if (isActive()) {
			for (GriffinBurrow burrow : griffinBurrows.values()) {
				if (burrow.shouldRender()) {
					burrow.extractRendering(collector);
				}
				if (burrow.confirmed != TriState.FALSE) {
					if (burrow.nextBurrowLine != null) {
						collector.submitLinesFromPoints(burrow.nextBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F, false);
					}
					if (burrow.echoBurrowLine != null) {
						collector.submitLinesFromPoints(burrow.echoBurrowLine, ORANGE_COLOR_COMPONENTS, 0.5F, 5F, false);
					}
					if (burrow.nextBurrowEstimatedPos != null && burrow.confirmed == TriState.DEFAULT) {
						collector.submitFilledBoxWithBeaconBeam(burrow.nextBurrowEstimatedPos, RED_COLOR_COMPONENTS, 0.5f, true);
					}
				}
			}
		}
	}

	public static InteractionResult onAttackBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
		return onInteractBlock(pos);
	}

	public static InteractionResult onUseBlock(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
		return onInteractBlock(hitResult.getBlockPos());
	}

	private static InteractionResult onInteractBlock(BlockPos pos) {
		if (isActive() && griffinBurrows.containsKey(pos)) {
			lastDugBurrowPos = pos;
		}
		return InteractionResult.PASS;
	}

	public static InteractionResult onUseItem(Player player, Level world, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		if (isActive() && SPADES.contains(stack.getSkyblockId())) {
			lastEchoTime = System.currentTimeMillis();
		}
		return InteractionResult.PASS;
	}

	@SuppressWarnings("SameReturnValue")
	public static boolean onChatMessage(Component message, boolean overlay) {
		if (isActive() && GRIFFIN_BURROW_DUG.matcher(message.getString()).matches()) {
			previousBurrow.confirmed = TriState.FALSE;
			if (lastDugBurrowPos == null) return true;
			previousBurrow = griffinBurrows.get(lastDugBurrowPos);
			previousBurrow.confirmed = TriState.DEFAULT;
		}

		return true;
	}

	private static boolean isActive() {
		return SkyblockerConfigManager.get().helpers.mythologicalRitual.enableMythologicalRitualHelper && Utils.getLocation() == Location.HUB;
	}

	private static void reset() {
		griffinBurrows.clear();
		lastDugBurrowPos = null;
		previousBurrow = new GriffinBurrow(BlockPos.ZERO);

		// Put a root burrow so echo detection works without a previous burrow
		previousBurrow.confirmed = TriState.DEFAULT;
		griffinBurrows.put(BlockPos.ZERO, previousBurrow);
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
		private @Nullable Vec3[] nextBurrowLine;
		/**
		 * The positions of the last two echo burrow particles.
		 */
		private @Nullable Vec3[] echoBurrowDirection;
		private @Nullable Vec3[] echoBurrowLine;
		private @Nullable BlockPos nextBurrowEstimatedPos;
		/**
		 * The line in the direction of the next burrow estimated by the previous burrow particles.
		 */
		private @Nullable Line nextBurrowLineEstimation;
		/**
		 * The line in the direction of the next burrow estimated by the echo ability.
		 */
		private @Nullable Line echoBurrowLineEstimation;

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
