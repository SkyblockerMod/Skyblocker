package de.hysky.skyblocker.skyblock.hunting.safari;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HuntingConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class SafariCritters {
	private static final Logger LOGGER = LoggerFactory.getLogger(SafariCritters.class);
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final float[] HONEYBUG_NEST_COLOR = ColorUtils.getFloatComponents(DyeColor.BLUE.getTextColor());
	private static final float[] SNOOZLE_WALL_COLOR = ColorUtils.getFloatComponents(DyeColor.ORANGE.getTextColor());
	private static final int CAVERN_CAVE_Y_LEVEL = 55;
	private static final Pattern CAUGHT_REGEX = Pattern.compile("^CAPTURE! You (?:caught an?|found the) (?:SPARKLING )?(?<capture>[\\w\\s]+?),? and|^LOOT SHARE! You received.+(?:catching an?|finding the) (?:SPARKLING )?(?<share>[\\w\\s]+)!$");
	private static final Pattern SPARKLING_REGEX = Pattern.compile("^SPARKLING! \\S+ (?:caught an?|found the) SPARKLING (?<sparkling>[^!]+)!$");
	private static final Pattern NAMETAG_REGEX = Pattern.compile(" (?<sparkling>SPARKLING )?(?<critter>[\\w ]+)$");

	private static final Map<SafariUtils.Critters, Integer> caughtCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final Map<SafariUtils.Critters, Integer> nearbyCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final Map<SafariUtils.Critters, Integer> sparklingCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final List<SafariUtils.BlockLocation> snoozleWalls = new ArrayList<>();
	private static final List<SafariUtils.BlockLocation> honeybugNests = new ArrayList<>();
	private static boolean started = false;

	@Init
	public static void init() {
		for (SafariUtils.Critters critter : SafariUtils.Critters.values()) {
			caughtCritters.put(critter, 0);
		}
		for (int i = 0; i < SafariUtils.getSnoozleWalls().size(); i++) {
			snoozleWalls.add(SafariUtils.BlockLocation.UNKNOWN);
		}
		for (int i = 0; i < SafariUtils.HONEYBUG_HIVES.size(); i++) {
			honeybugNests.add(SafariUtils.BlockLocation.UNKNOWN);
		}

		Scheduler.INSTANCE.scheduleCyclic(SafariCritters::tick, 20);
		ClientReceiveMessageEvents.ALLOW_GAME.register(SafariCritters::onChatMessage);
		AttackBlockCallback.EVENT.register((_, _, _, pos, _) -> attackOrUseBlock(pos));
		UseBlockCallback.EVENT.register((_, _, _, hitResult) -> attackOrUseBlock(hitResult.getBlockPos()));
		// TODO: Question for reviewers, are both DISCONNECT and JOIN needed?
		ClientPlayConnectionEvents.DISCONNECT.register((_, _) -> reset());
		ClientPlayConnectionEvents.JOIN.register(((_, _, _) -> reset()));
		LevelRenderExtractionCallback.EVENT.register(SafariCritters::extractRendering);
	}

	private static void start() {
		started = true;
	}

	private static void reset() {
		if (started) {
			started = false;
			caughtCritters.replaceAll((_, _) -> 0);
			nearbyCritters.clear();
			sparklingCritters.clear();
			snoozleWalls.replaceAll(_ -> SafariUtils.BlockLocation.UNKNOWN);
			honeybugNests.replaceAll(_ -> SafariUtils.BlockLocation.UNKNOWN);
		}
	}

	private static void tick() {
		if (MINECRAFT.level == null || MINECRAFT.player == null || !Utils.isInSafari()) return;

		// Check nearby nametags for both regular and sparkling critters
		nearbyCritters.clear();
		var sparkling = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		for (Entity entity : MINECRAFT.level.entitiesForRendering()) {
			if (entity instanceof ArmorStand) {
				Component name = entity.getCustomName();
				if (name != null && entity.isCustomNameVisible()) {
					Matcher match = NAMETAG_REGEX.matcher(name.getString());
					if (!match.find()) continue;
					SafariUtils.Critters critter = getCritter(match.group("critter"), false);
					if (critter == null) continue;
					if (match.group("sparkling") == null) {
						nearbyCritters.merge(critter, 1, Integer::sum);
					} else {
						sparkling.merge(critter, 1, Integer::sum);
					}
				}
			}
		}
		// Keep sparkling critters tracked until caught even if their nametag disappears
		sparkling.forEach((critter, count) -> sparklingCritters.merge(critter, count, Integer::max));

		Frustum frustum = RenderHelper.getCamera().getCullFrustum();
		// Check if snoozle walls are broken or not
		if (isInCavernCaveSection()) {
			for (int i = 0; i < snoozleWalls.size(); i++) {
				SafariUtils.BlockLocation location = snoozleWalls.get(i);
				BlockPos pos = SafariUtils.SNOOZLE_WALL_CORES.get(i);
				if (location == SafariUtils.BlockLocation.CLEAR || cantSeeBlock(frustum, pos)) continue;
				// check block to see if it's solid or air
				BlockState block = MINECRAFT.level.getBlockState(pos);
				if (block.isAir()) snoozleWalls.set(i, SafariUtils.BlockLocation.CLEAR);
				else if (location == SafariUtils.BlockLocation.UNKNOWN) snoozleWalls.set(i, SafariUtils.BlockLocation.FOUND);
			}
		}
		// Check for newly spotted honeybug nests
		if (SafariUtils.isInForestBiome()) {
			for (int i = 0; i < honeybugNests.size(); i++) {
				BlockPos pos = SafariUtils.HONEYBUG_HIVES.get(i);
				if (honeybugNests.get(i) != SafariUtils.BlockLocation.UNKNOWN || cantSeeBlock(frustum, pos)) continue;
				// check block to see if it's a nest or hive
				BlockState block = MINECRAFT.level.getBlockState(pos);
				if (block.is(Blocks.BEE_NEST)) {
					LOGGER.info("[Skyblocker] Honeybug Nest spotted at {}", pos);
					honeybugNests.set(i, SafariUtils.BlockLocation.FOUND);
				} else {
					LOGGER.info("[Skyblocker] Honeybug Hive spotted at {}", pos);
					honeybugNests.set(i, SafariUtils.BlockLocation.OTHER);
				}
			}
		}

		// Tell critter hud that information may have changed
		CritterHudWidget.getInstance().enableUpdate();
	}

	private static InteractionResult attackOrUseBlock(BlockPos pos) {
		// We only care about bee nests in the safari
		if (MINECRAFT.level == null || !Utils.isInSafari()) return InteractionResult.PASS;
		BlockState block = MINECRAFT.level.getBlockState(pos);
		if (block.is(Blocks.BEE_NEST)) {
			for (int i = 0; i < honeybugNests.size(); i++) {
				if (SafariUtils.HONEYBUG_HIVES.get(i).equals(pos) && honeybugNests.get(i) == SafariUtils.BlockLocation.FOUND) {
					honeybugNests.set(i, SafariUtils.BlockLocation.CLEAR);
				}
			}
		}
		return InteractionResult.PASS;
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (!Utils.isInSafari()) return true;
		// There's probably a better way but this works perfectly.
		if (!started) start();

		String message = ChatFormatting.stripFormatting(text.getString());
		SafariUtils.Critters critter = parseCritter(message);

		if (critter == null) {
			Matcher match = SPARKLING_REGEX.matcher(message);

			// When a sparkling critter is caught remove it from the spotted list
			if (match.matches()) {
				SafariUtils.Critters sparkling = getCritter(match.group("sparkling"), true);
				int count = sparklingCritters.getOrDefault(sparkling, 0);
				if (sparkling != null && count != 0) {
					sparklingCritters.put(sparkling, count - 1);
				}
			}
		} else {
			caughtCritters.merge(critter, 1, Integer::sum);

			// Stop highlighting honeybug nests if someone else catches the final honeybug
			if (critter == SafariUtils.Critters.HONEYBUG && getCaught(critter) == getTotalHoneybugs(false)) {
				honeybugNests.replaceAll(loc -> loc == SafariUtils.BlockLocation.FOUND ? SafariUtils.BlockLocation.CLEAR : loc);
			}
		}

		return true;
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (MINECRAFT.level == null || MINECRAFT.player == null || !Utils.isInSafari()) return;

		// Highlight snoozle walls while in the cave section of the cavern biome
		if (SkyblockerConfigManager.get().hunting.safari.highlightSnoozleWalls && isInCavernCaveSection()) {
			var snoozleWallBlocks = SafariUtils.getSnoozleWalls();
			for (int i = 0; i < snoozleWalls.size(); i++) {
				if (snoozleWalls.get(i) == SafariUtils.BlockLocation.CLEAR) continue;
				var wallBlocks = snoozleWallBlocks.get(i);
				HuntingConfig.Safari.WallHighlightType highlightType = SkyblockerConfigManager.get().hunting.safari.wallHighlightType;
				if (highlightType != HuntingConfig.Safari.WallHighlightType.HIGHLIGHT) {
					collector.submitOutlinedConnected(wallBlocks, SNOOZLE_WALL_COLOR, 5f, false);
				}
				if (highlightType != HuntingConfig.Safari.WallHighlightType.OUTLINE) {
					/* TODO: This is very ugly on air blocks, technically works but a collector.submitFilledConnected()
					 * method would look much nicer during snoozle wall interactions
					 */
					for (BlockPos pos : wallBlocks) {
						collector.submitFilledBox(pos, SNOOZLE_WALL_COLOR, 0.4f, false);
					}
				}
			}
		}

		// Highlight unconfirmed or uncollected honeybug nests
		if (SkyblockerConfigManager.get().hunting.safari.highlightHoneybugNests && SafariUtils.isInForestBiome() && !SafariUtils.isInSpawn(MINECRAFT.player)) {
			for (int i = 0; i < honeybugNests.size(); i++) {
				SafariUtils.BlockLocation location = honeybugNests.get(i);
				if (location != SafariUtils.BlockLocation.UNKNOWN && location != SafariUtils.BlockLocation.FOUND) continue;
				BlockPos pos = SafariUtils.HONEYBUG_HIVES.get(i);
				AABB outline = RenderHelper.getBlockBoundingBox(MINECRAFT.level, pos);

				if (outline != null) {
					collector.submitFilledBox(outline, HONEYBUG_NEST_COLOR, 0.4f, true);
				}
			}
		}
	}

	private static boolean isInCavernCaveSection() {
		assert MINECRAFT.player != null;
		return SafariUtils.isInCavernBiome() && MINECRAFT.player.getBlockY() <= CAVERN_CAVE_Y_LEVEL;
	}

	private static boolean cantSeeBlock(Frustum frustum, BlockPos pos) {
		assert MINECRAFT.level != null && MINECRAFT.player != null;

		// Confirm block is within player's camera angle
		if (!FrustumUtils.isVisible(frustum, pos)) return true;

		// Raycast to confirm if block is visible to player
		BlockHitResult blockHitResult = MINECRAFT.level.clip(new ClipContext(MINECRAFT.player.getEyePosition(), Vec3.atCenterOf(pos), ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, MINECRAFT.player));
		return blockHitResult.getType() != HitResult.Type.MISS && !blockHitResult.getBlockPos().equals(pos);
	}

	private static SafariUtils.@Nullable Critters getCritter(String name, boolean warn) {
		try {
			return SafariUtils.Critters.valueOf(name.replace(" ", "_").toUpperCase(Locale.ENGLISH));
		} catch (IllegalArgumentException _) {
			if (warn) {
				LOGGER.warn("[Skyblocker] Unknown safari critter: {}", name);
			}
			return null;
		}
	}

	@VisibleForTesting
	public static SafariUtils.@Nullable Critters parseCritter(String message) {
		Matcher match = CAUGHT_REGEX.matcher(message);
		if (match.find()) {
			return getCritter(match.group("capture") == null ? match.group("share") : match.group("capture"), true);
		}
		return null;
	}

	public static boolean hasUnique(SafariUtils.Critters critter) {
		// TODO: Check for leftover bird seeds in player inventory
		return switch (critter) {
			// Snoozles are a 50% chance per wall, so 0 snoozles is possible
			case SafariUtils.Critters.SNOOZLE -> caughtCritters.get(critter) > 0 || (getMinimum(critter) == 0 && !nearbyCritters.containsKey(SafariUtils.Critters.SNOOZLE));
			// Once all birds have been caught any missing uniques are no longer possible
			case SafariUtils.Critters.BLUEBIRD, SafariUtils.Critters.PARAKEET, SafariUtils.Critters.MACAW -> caughtCritters.get(critter) > 0 || caughtCritters.get(SafariUtils.Critters.BLUEBIRD) + caughtCritters.get(SafariUtils.Critters.PARAKEET) + (caughtCritters.get(SafariUtils.Critters.MACAW) / 2) >= 9;
			default -> caughtCritters.get(critter) > 0;
		};
	}

	public static int getCaught(SafariUtils.Critters critter) {
		return caughtCritters.get(critter);
	}

	private static int getTotalHoneybugs(boolean minimum) {
		int found = 0;
		for (SafariUtils.BlockLocation location : honeybugNests) {
			if (location == SafariUtils.BlockLocation.UNKNOWN) {
				return minimum ? SafariUtils.CRITTER_DETAILS.get(SafariUtils.Critters.HONEYBUG).min() : -1;
			} else if (location == SafariUtils.BlockLocation.FOUND) {
				++found;
			}
		}

		return found;
	}

	public static int getUnknownSnoozles() {
		return Collections.frequency(snoozleWalls, SafariUtils.BlockLocation.UNKNOWN);
	}

	public static int getUnknownHoneybugs() {
		return Collections.frequency(honeybugNests, SafariUtils.BlockLocation.UNKNOWN);
	}

	public static int getMinimum(SafariUtils.Critters critter) {
		// TODO: Check for leftover shining coins in player inventory
		return switch (critter) {
			case SafariUtils.Critters.HONEYBUG -> getTotalHoneybugs(true);
			// Check number of remaining snoozle walls
			case SafariUtils.Critters.SNOOZLE -> (int) snoozleWalls.stream().filter(loc -> loc != SafariUtils.BlockLocation.CLEAR).count();
			default -> SafariUtils.CRITTER_DETAILS.get(critter).min();
		};
	}

	// List of sparkling critters that have been spotted but not caught in the current biome
	public static @Nullable Map<SafariUtils.Critters, Integer> getSparklings(EnumSet<SafariUtils.Critters> biomeCritters) {
		if (sparklingCritters.isEmpty()) return null;

		var biomeSparklings = new EnumMap<SafariUtils.Critters, Integer>(SafariUtils.Critters.class);
		for (SafariUtils.Critters sparkling : sparklingCritters.keySet()) {
			if (!biomeCritters.contains(sparkling)) continue;
			biomeSparklings.put(sparkling, sparklingCritters.get(sparkling));
		}

		return biomeSparklings.isEmpty() ? null : biomeSparklings;
	}

	public static int getNearby(SafariUtils.Critters critter) {
		return nearbyCritters.getOrDefault(critter, 0);
	}
}
