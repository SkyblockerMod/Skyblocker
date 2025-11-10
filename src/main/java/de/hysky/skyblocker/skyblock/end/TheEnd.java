package de.hysky.skyblocker.skyblock.end;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.data.ProfiledData;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class TheEnd {
	protected static final Logger LOGGER = LoggerFactory.getLogger(TheEnd.class);
	private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("end.json");

	private static final Pattern END_STONE_PROTECTOR_TREMOR = Pattern.compile("^You feel a tremor from beneath the earth!$");
	private static final Pattern END_STONE_PROTECTOR_RISES = Pattern.compile("^The ground begins to shake as an End Stone Protector rises from below!$");
	private static final Pattern END_STONE_PROTECTOR_FIGHT_STARTS = Pattern.compile("^BEWARE - An End Stone Protector has risen!$");
	private static final Pattern SPECIAL_ZEALOT_SPAWNED = Pattern.compile("^A special Zealot has spawned nearby!$");
	private static final List<ProtectorLocation> PROTECTOR_LOCATIONS = List.of(
			new ProtectorLocation(-649, -219, Text.translatable("skyblocker.end.hud.protectorLocations.left")),
			new ProtectorLocation(-644, -269, Text.translatable("skyblocker.end.hud.protectorLocations.front")),
			new ProtectorLocation(-689, -273, Text.translatable("skyblocker.end.hud.protectorLocations.center")),
			new ProtectorLocation(-727, -284, Text.translatable("skyblocker.end.hud.protectorLocations.back")),
			new ProtectorLocation(-639, -328, Text.translatable("skyblocker.end.hud.protectorLocations.rightFront")),
			new ProtectorLocation(-678, -332, Text.translatable("skyblocker.end.hud.protectorLocations.rightBack"))
	);
	private static final Set<UUID> HIT_ZEALOTS = new ObjectOpenHashSet<>();
	public static final ProfiledData<EndStats> PROFILES_STATS = new ProfiledData<>(FILE, EndStats.CODEC);

	public static ProtectorLocation currentProtectorLocation = null;
	public static int stage = 0;

	@Init
	public static void init() {
		AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			if (entity instanceof EndermanEntity enderman && isZealot(enderman)) {
				HIT_ZEALOTS.add(enderman.getUuid());
			}
			return ActionResult.PASS;
		});

		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			String lowerCase = Utils.getIslandArea().toLowerCase(Locale.ENGLISH);
			if (Utils.isInTheEnd() || lowerCase.contains("the end") || lowerCase.contains("dragon's nest")) {
				ChunkPos pos = chunk.getPos();
				//
				Box box = new Box(pos.getStartX(), 0, pos.getStartZ(), pos.getEndX() + 1, 1, pos.getEndZ() + 1);
				for (ProtectorLocation protectorLocation : PROTECTOR_LOCATIONS) {
					if (box.contains(protectorLocation.x(), 0.5, protectorLocation.z())) {
						// MinecraftClient.getInstance().player.sendMessage(Text.literal("Checking: ").append(protectorLocation.name));//MinecraftClient.getInstance().player.sendMessage(Text.literal(pos.getStartX() + " " + pos.getStartZ() + " " + pos.getEndX() + " " + pos.getEndZ()));
						if (isProtectorHere(world, protectorLocation)) break;
					}
				}
			}
		});

		// Fix for when you join skyblock, and you are directly in the end
		SkyblockEvents.PROFILE_CHANGE.register((prev, profile) -> EndHudWidget.getInstance().update());

		// Reset when changing island
		SkyblockEvents.LOCATION_CHANGE.register(location -> {
			resetLocation();
			HIT_ZEALOTS.clear();
		});

		ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
			if (!Utils.isInTheEnd() || overlay) return true;
			onMessage(message.getString());
			return true;
		});

		WorldRenderExtractionCallback.EVENT.register(TheEnd::extractRendering);
		PROFILES_STATS.init();
	}

	private static void onMessage(String text) {
		if (END_STONE_PROTECTOR_TREMOR.matcher(text).matches()) {
			if (stage == 0) checkAllProtectorLocations();
			else stage += 1;
		} else if (END_STONE_PROTECTOR_RISES.matcher(text).matches()) {
			if (currentProtectorLocation == null) checkAllProtectorLocations();
			stage = 5;
		} else if (END_STONE_PROTECTOR_FIGHT_STARTS.matcher(text).matches()) {
			resetLocation();
		} else if (SPECIAL_ZEALOT_SPAWNED.matcher(text).matches()) {
			// Assume that the player will kill it and get the Summoning Eye
			onSpecialZealot();
		}

		EndHudWidget.getInstance().update();
	}

	private static void checkAllProtectorLocations() {
		ClientWorld world = MinecraftClient.getInstance().world;
		if (world == null) return;
		for (ProtectorLocation protectorLocation : PROTECTOR_LOCATIONS) {
			if (!world.isChunkLoaded(protectorLocation.x() >> 4, protectorLocation.z() >> 4)) continue;
			if (isProtectorHere(world, protectorLocation)) break;
		}
	}

	/**
	 * Checks a bunch of Ys to see if a player head is there, if it's there it returns true and updates the hud accordingly
	 *
	 * @param world             le world to check
	 * @param protectorLocation protectorLocation to check
	 * @return if found
	 */
	private static boolean isProtectorHere(ClientWorld world, ProtectorLocation protectorLocation) {
		for (int i = 0; i < 5; i++) {
			if (world.getBlockState(new BlockPos(protectorLocation.x, i + 5, protectorLocation.z)).isOf(Blocks.PLAYER_HEAD)) {
				stage = i + 1;
				currentProtectorLocation = protectorLocation;
				EndHudWidget.getInstance().update();
				return true;
			}
		}
		return false;
	}

	private static void resetLocation() {
		stage = 0;
		currentProtectorLocation = null;
	}

	public static void onEntityDeath(Entity entity) {
		if (!(entity instanceof EndermanEntity enderman) || !isZealot(enderman)) return;
		if (HIT_ZEALOTS.contains(enderman.getUuid())) {
			EndStats stats = PROFILES_STATS.computeIfAbsent(EndStats.EMPTY);
			PROFILES_STATS.put(new EndStats(stats.totalZealotKills() + 1, stats.zealotsSinceLastEye() + 1, stats.eyes()));
			HIT_ZEALOTS.remove(enderman.getUuid());
			EndHudWidget.getInstance().update();
		}
	}

	public static void onSpecialZealot() {
		EndStats stats = PROFILES_STATS.computeIfAbsent(EndStats.EMPTY);
		PROFILES_STATS.put(new EndStats(stats.totalZealotKills(), 0, stats.eyes() + 1));
	}

	public static boolean isZealot(EndermanEntity enderman) {
		if (enderman.getName().getString().toLowerCase(Locale.ENGLISH).contains("zealot")) return true; // Future-proof. If they someday decide to actually rename the entities
		assert MinecraftClient.getInstance().world != null;
		List<ArmorStandEntity> entities = MinecraftClient.getInstance().world.getEntitiesByClass(
				ArmorStandEntity.class,
				enderman.getDimensions(null).getBoxAt(enderman.getPos()).expand(1),
				armorStandEntity -> armorStandEntity.getName().getString().toLowerCase(Locale.ENGLISH).contains("zealot"));
		if (entities.isEmpty()) {
			return false;
		}
		return entities.getFirst().getName().getString().toLowerCase(Locale.ENGLISH).contains("zealot");
	}

	public static boolean isSpecialZealot(EndermanEntity enderman) {
		return isZealot(enderman) && enderman.getCarriedBlock() != null && enderman.getCarriedBlock().isOf(Blocks.END_PORTAL_FRAME);
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().otherLocations.end.waypoint) return;
		if (currentProtectorLocation == null || stage != 5) return;
		currentProtectorLocation.waypoint().extractRendering(collector);
	}

	public record EndStats(int totalZealotKills, int zealotsSinceLastEye, int eyes) {
		private static final Codec<EndStats> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				Codec.INT.fieldOf("totalZealotKills").forGetter(EndStats::totalZealotKills),
				Codec.INT.fieldOf("zealotsSinceLastEye").forGetter(EndStats::zealotsSinceLastEye),
				Codec.INT.fieldOf("eyes").forGetter(EndStats::eyes)
		).apply(instance, EndStats::new));
		public static final Supplier<EndStats> EMPTY = () -> new EndStats(0, 0, 0);
	}

	public record ProtectorLocation(int x, int z, Text name, Waypoint waypoint) {
		public ProtectorLocation(int x, int z, Text name) {
			this(x, z, name, new Waypoint(new BlockPos(x, 0, z), Waypoint.Type.WAYPOINT, ColorUtils.getFloatComponents(DyeColor.MAGENTA)));
		}
	}
}
