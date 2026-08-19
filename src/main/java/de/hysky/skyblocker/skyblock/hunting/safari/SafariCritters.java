package de.hysky.skyblocker.skyblock.hunting.safari;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
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
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.HuntingConfig;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;

public class SafariCritters {
	private static final Logger LOGGER = LoggerFactory.getLogger(SafariCritters.class);
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private static final float[] HONEYBUG_NEST_COLOR = ColorUtils.getFloatComponents(DyeColor.BLUE.getTextColor());
	private static final float[] SNOOZLE_WALL_COLOR = ColorUtils.getFloatComponents(DyeColor.ORANGE.getTextColor());
	private static final Pattern CAUGHT_REGEX = Pattern.compile("^CAPTURE! You (?:caught an?|found the) (?:SPARKLING )?(?<capture>[\\w\\s]+?),? and|^LOOT SHARE! You received.+(?:catching an?|finding the) (?:SPARKLING )?(?<share>[\\w\\s]+)!$");
	private static final Pattern NAMETAG_REGEX = Pattern.compile(" (?<sparkling>SPARKLING )?(?<critter>[\\w ]+)$");

	private static final Map<SafariUtils.Critters, Integer> caughtCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final Map<SafariUtils.Critters, Integer> nearbyCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final Map<SafariUtils.Critters, Integer> sparklingCritters = new EnumMap<>(SafariUtils.Critters.class);
	private static final List<Boolean> snoozleWalls = Arrays.asList(false, false, false, false, false);
	private static int totalHoneybugs = 0;
	private static @Nullable List<BlockPos> honeybugNests = null;
	private static boolean started = false;

	@Init
	public static void init() {
		for (var critter : SafariUtils.Critters.values()) {
			caughtCritters.put(critter, 0);
		}
		Scheduler.INSTANCE.scheduleCyclic(SafariCritters::update, 20);
		ClientReceiveMessageEvents.ALLOW_GAME.register(SafariCritters::onChatMessage);
		AttackBlockCallback.EVENT.register((_, _, _, pos, _) -> attackOrUseBlock(pos));
		UseBlockCallback.EVENT.register((_, _, _, hitResult) -> attackOrUseBlock(hitResult.getBlockPos()));
		// are both of these needed?
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
			snoozleWalls.replaceAll(_ -> false);
			honeybugNests = null;
			totalHoneybugs = 0;
		}
	}

	private static void update() {
		if (MINECRAFT.level == null || !Utils.isInSafari()) return;

		// Check for nearby nametags
		nearbyCritters.clear();
		sparklingCritters.clear();

		for (Entity entity : MINECRAFT.level.entitiesForRendering()) {
			if (entity instanceof ArmorStand) {
				var name = entity.getCustomName();
				if (name != null && entity.isCustomNameVisible()) {
					Matcher match = NAMETAG_REGEX.matcher(name.getString());
					if (!match.find()) continue;
					SafariUtils.Critters critter = getCritter(match.group("critter"), false);
					if (critter == null) continue;
					if (match.group("sparkling") == null) {
						nearbyCritters.merge(critter, 1, Integer::sum);
					} else {
						sparklingCritters.merge(critter, 1, Integer::sum);
					}
				}
			}
		}

		// Check if snoozle walls are broken or not
		if (SafariUtils.isInCavernBiome()) {
			for (int i = 0; i < SafariUtils.SNOOZLE_WALL_CORES.size(); i++) {
				BlockState block = MINECRAFT.level.getBlockState(SafariUtils.SNOOZLE_WALL_CORES.get(i));
				snoozleWalls.set(i, block.isAir());
			}
		}

		// Check for honeybug nests on initial forest entry
		if (SafariUtils.isInForestBiome() && honeybugNests == null) {
			honeybugNests = new ArrayList<>();
			for (BlockPos nest : SafariUtils.HONEYBUG_HIVES) {
				BlockState block = MINECRAFT.level.getBlockState(nest);
				if (block.is(Blocks.BEE_NEST)) honeybugNests.add(nest);
			}
			totalHoneybugs = honeybugNests.size();
		}

		// Tell critter hud that information may have changed
		CritterHudWidget.getInstance().enableUpdate();
	}

	private static InteractionResult attackOrUseBlock(BlockPos pos) {
		// we only care about bee nests in the safari forest biome
		if (MINECRAFT.level == null || honeybugNests == null || honeybugNests.isEmpty() || !Utils.isInSafari()) return InteractionResult.PASS;
		BlockState block = MINECRAFT.level.getBlockState(pos);
		if (block.is(Blocks.BEE_NEST)) {
			honeybugNests.remove(pos);
		}
		return InteractionResult.PASS;
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (!Utils.isInSafari()) return true;
		if (!started) start();

		SafariUtils.Critters critter = parseCritter(ChatFormatting.stripFormatting(text.getString()));

		if (critter != null) {
			caughtCritters.merge(critter, 1, Integer::sum);

			// clear all highlighted bee nests if someone catches the final honeybug
			if (honeybugNests != null && critter == SafariUtils.Critters.HONEYBUG && caughtCritters.get(critter) == totalHoneybugs) {
				honeybugNests.clear();
			}
		}

		return true;
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (MINECRAFT.level == null || MINECRAFT.player == null || !Utils.isInSafari() || honeybugNests == null) return;

		// highlight snoozle walls while in the cave section of the cavern biome
		if (SkyblockerConfigManager.get().hunting.safari.highlightSnoozleWalls && SafariUtils.isInCavernBiome() && MINECRAFT.player.getY() < 55) {
			var snoozleWallBlocks = SafariUtils.getSnoozleWalls();
			for (int i = 0; i < snoozleWallBlocks.size(); i++) {
				if (!snoozleWalls.get(i)) {
					var SNOOZLE_WALL = SafariUtils.getSnoozleWalls().get(i);
					HuntingConfig.Safari.WallHighlightType highlightType = SkyblockerConfigManager.get().hunting.safari.wallHighlightType;
					if (highlightType != HuntingConfig.Safari.WallHighlightType.HIGHLIGHT) {
						collector.submitOutlinedConnected(SNOOZLE_WALL, SNOOZLE_WALL_COLOR, 5f, false);
					}
					if (highlightType != HuntingConfig.Safari.WallHighlightType.OUTLINE) {
						for (BlockPos pos : SNOOZLE_WALL) {
							collector.submitFilledBox(pos, SNOOZLE_WALL_COLOR, 0.4f, false);
						}
					}
				}
			}
		}

		// highlight honeybug nests until right-clicked or all honeybugs are caught
		if (SkyblockerConfigManager.get().hunting.safari.highlightHoneybugNests && SafariUtils.isInForestBiome()) {
			for (BlockPos nest : honeybugNests) {
				AABB outline = RenderHelper.getBlockBoundingBox(MINECRAFT.level, nest);

				if (outline != null) {
					collector.submitFilledBox(outline, HONEYBUG_NEST_COLOR, 0.4f, false);
				}
			}
		}
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
		return switch (critter) {
			// snoozles are a 50% chance per wall, so 0 snoozles possible
			case SafariUtils.Critters.SNOOZLE -> caughtCritters.get(critter) > 0 || (!snoozleWalls.contains(false) && !nearbyCritters.containsKey(SafariUtils.Critters.SNOOZLE));
			// TODO: check for leftover bird items in inventory
			// once all birds have spawned any missing uniques are no longer possible
			case SafariUtils.Critters.BLUEBIRD, SafariUtils.Critters.PARAKEET, SafariUtils.Critters.MACAW -> caughtCritters.get(critter) > 0 || caughtCritters.get(SafariUtils.Critters.BLUEBIRD) + caughtCritters.get(SafariUtils.Critters.PARAKEET) + (caughtCritters.get(SafariUtils.Critters.MACAW) / 2) >= 9;
			default -> caughtCritters.get(critter) > 0;
		};
	}

	public static int getCaught(SafariUtils.Critters critter) {
		return caughtCritters.get(critter);
	}

	public static int getMinimum(SafariUtils.Critters critter) {
		// TODO: check for leftover coins in inventory for gimmiegold
		return switch (critter) {
			case SafariUtils.Critters.HONEYBUG -> totalHoneybugs;
			// check number of remaining snoozle walls
			case SafariUtils.Critters.SNOOZLE -> Collections.frequency(snoozleWalls, false);
			default -> SafariUtils.CRITTER_DETAILS.get(critter).min();
		};
	}

	public static @Nullable Map<SafariUtils.Critters, Integer> getSparklings() {
		return sparklingCritters.isEmpty() ? null : sparklingCritters;
	}

	public static int getNearby(SafariUtils.Critters critter) {
		return nearbyCritters.getOrDefault(critter, 0);
	}
}
