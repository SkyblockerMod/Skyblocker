package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.command.argumenttypes.EggTypeArgumentType;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import de.hysky.skyblocker.utils.ws.Service;
import de.hysky.skyblocker.utils.ws.WsMessageHandler;
import de.hysky.skyblocker.utils.ws.WsStateManager;
import de.hysky.skyblocker.utils.ws.message.EggWaypointMessage;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.apache.commons.text.WordUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class EggFinder {
	private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Egg Finder");
	private static final Pattern EGG_FOUND_PATTERN = Pattern.compile("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper) Egg");
	private static final Set<Location> LOCATIONS = Set.of(
			Location.BACKWATER_BAYOU, Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DEEP_CAVERNS,
			Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.GALATEA, Location.GOLD_MINE, Location.HUB,
			Location.THE_END, Location.THE_FARMING_ISLAND, Location.THE_PARK, Location.SPIDERS_DEN
	);

	private static boolean isSpring = SkyblockTime.skyblockSeason.get() == SkyblockTime.Season.SPRING;

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> clearEggs());
		SkyblockEvents.LOCATION_CHANGE.register(EggFinder::handleLocationChange);
		ClientReceiveMessageEvents.ALLOW_GAME.register(EggFinder::onChatMessage);
		WorldRenderExtractionCallback.EVENT.register(EggFinder::extractRendering);

		SkyblockTime.HOUR_CHANGE.register(hour -> {
			if (!isSpring) return;
			int dayNumber = SkyblockTime.skyblockMonth.get().ordinal() * 31 + SkyblockTime.skyblockDay.get();
			boolean isOdd = dayNumber % 2 == 1;

			for (EggType type : EggType.entries) {
				if (hour == type.resetHour && isOdd == type.oddDay) {
					type.collected = false;
					type.prevEgg = type.egg;
					type.egg = null;
				}
			}
		});
		SkyblockTime.SEASON_CHANGE.register(season -> {
			isSpring = season == SkyblockTime.Season.SPRING;
			if (!isSpring) clearEggs();
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("eggFinder").then(literal("shareLocation").then(argument("eggType", EggTypeArgumentType.eggType())
					.executes(context -> {
						EggType eggType = context.getArgument("eggType", EggType.class);
						if (eggType == null || eggType.egg == null) {
							context.getSource().sendError(Constants.PREFIX.get().append(Component.translatable("skyblocker.helpers.hoppitysHunt.unableToShareEgg").withStyle(style -> style.withColor(ChatFormatting.RED))));
							return Command.SINGLE_SUCCESS;
						}
						MessageScheduler.INSTANCE.sendMessageAfterCooldown("[Skyblocker] Chocolate %s Egg found at %s".formatted(eggType.name, eggType.egg.pos.toShortString()), false);
						return Command.SINGLE_SUCCESS;
					})))));

			if (!Debug.debugEnabled()) return;
			dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("eggFinder").then(literal("resetFoundStatus")
					.executes(context -> {
						for (EggType type : EggType.entries) {
							type.collected = false;
							if (type.egg != null) type.egg.setMissing();
						}
						return Command.SINGLE_SUCCESS;
					}))));

			dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("eggFinder").then(literal("clearWaypoints")
					.executes(context -> {
						clearEggs();
						return Command.SINGLE_SUCCESS;
					}))));
		});
	}

	public static void onWebsocketMessage(EggWaypointMessage message) {
		EggType eggType = message.eggType();
		eggType.egg = new Egg(message.coordinates(), eggType);
		eggType.onEggReceived();
	}

	private static void clearEggs() {
		for (EggType type : EggType.entries) {
			type.egg = null;
			type.prevEgg = null;
		}
	}

	private static void handleLocationChange(Location location) {
		clearEggs();
		if (!isSpring || !LOCATIONS.contains(location) || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		WsStateManager.subscribeIsland(Service.EGG_WAYPOINTS, Optional.empty());
	}

	public static boolean checkIfEgg(ArmorStand armorStand, EggType eggType) {
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || armorStand.showBasePlate()) return false;
		return handleArmorStand(armorStand, eggType);
	}

	private static boolean handleArmorStand(ArmorStand armorStand, EggType eggType) {
		for (ItemStack itemStack : ItemUtils.getArmor(armorStand)) {
			Optional<String> texture = ItemUtils.getHeadTextureOptional(itemStack);
			if (texture.isEmpty()) continue;
			if (texture.get().equals(eggType.texture)) {
				return true;
			}
		}
		return false;
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!isSpring || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			Egg egg = type.egg;
			if (egg != null) egg.extractRendering(collector);
		}
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onChatMessage(Component text, boolean overlay) {
		if (overlay || !isSpring || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return true;
		Matcher matcher = EGG_FOUND_PATTERN.matcher(text.getString());
		if (!matcher.find()) return true;

		try {
			EggType eggType = EggType.getTypeByName(matcher.group(1));
			if (eggType == null) return true;

			eggType.collected = true;
			if (eggType.egg != null) {
				eggType.egg.setFound();
				return true;
			}

			LOGGER.info("[Skyblocker Egg Finder] Discovered a new egg!");
			Minecraft client = Minecraft.getInstance();
			if (client.player == null || client.level == null) return true;
			List<ArmorStand> entities = client.level.getEntitiesOfClass(ArmorStand.class,
					AABB.ofSize(client.player.position(), 4f, 4f, 4f),
					(entity) -> EggFinder.checkIfEgg(entity, eggType)
			);

			if (entities.size() != 1) return true;
			eggType.egg = new Egg(entities.getFirst().blockPosition().above(2), eggType);
			eggType.egg.setFound();
			eggType.sendEggMessage();
			if (eggType.egg.equals(eggType.prevEgg)) {
				LOGGER.info("[Skyblocker Egg Finder] Not sharing this egg to the WebSocket - matches previous location");
				return true;
			}
			WsMessageHandler.sendLocationMessage(Service.EGG_WAYPOINTS, new EggWaypointMessage(eggType, eggType.egg.pos));
		} catch (IllegalArgumentException e) {
			LOGGER.error("[Skyblocker Egg Finder] Failed to process an egg!", e);
		}

		return true;
	}

	@SuppressWarnings("DataFlowIssue") //Removes that pesky "unboxing of Integer might cause NPE" warning when we already know it's not null
	public enum EggType implements StringRepresentable {
		BREAKFAST("Breakfast", ChatFormatting.GOLD.getColor(), 7, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0", true),
		LUNCH("Lunch", ChatFormatting.BLUE.getColor(), 14, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9", true),
		DINNER("Dinner", ChatFormatting.GREEN.getColor(), 21, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0", true),
		BRUNCH("Brunch", BREAKFAST.color, BREAKFAST.resetHour, BREAKFAST.texture, false),
		DEJEUNER("Déjeuner", LUNCH.color, LUNCH.resetHour, LUNCH.texture, false),
		SUPPER("Supper", DINNER.color, DINNER.resetHour, DINNER.texture, false);

		public static final Codec<EggType> CODEC = StringRepresentable.fromValues(EggType::values);

		//This is to not create an array each time we iterate over the values
		public static final ObjectImmutableList<EggType> entries = ObjectImmutableList.of(EggType.values());

		public final String name;
		public final int color;
		public final int resetHour;
		public final String texture;
		/**
		 * Whether the egg refreshes on an odd day.
		 */
		public final boolean oddDay;

		boolean collected = false;
		private @Nullable Egg egg = null;
		private @Nullable Egg prevEgg = null;

		EggType(String name, int color, int resetHour, String texture, Boolean oddDay) {
			this.name = name;
			this.color = color;
			this.resetHour = resetHour;
			this.texture = texture;
			this.oddDay = oddDay;
		}

		public void onEggReceived() {
			if (!SkyblockerConfigManager.get().helpers.chocolateFactory.sendEggFoundMessages) return;
			if (collected) {
				egg.setFound();
				return;
			}
			sendEggMessage();
		}

		public void sendEggMessage() {
			MutableComponent eggName = Component.translatable("skyblocker.helpers.hoppitysHunt.chocolateEgg", this.name).withColor(color);
			Minecraft.getInstance().player.displayClientMessage(
					Constants.PREFIX.get().append(Component.translatable("skyblocker.helpers.hoppitysHunt.newEggDiscovered", eggName, egg.pos.toShortString())
					).withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/skyblocker eggFinder shareLocation " + this))
							.withHoverEvent(new HoverEvent.ShowText(Component.translatable("skyblocker.helpers.hoppitysHunt.shareEggPrompt").withStyle(ChatFormatting.GREEN)))
					),
					false
			);
		}

		@Override
		public String toString() {
			return WordUtils.capitalizeFully(this.name());
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		public static @Nullable EggType getTypeByName(String eggType) {
			for (EggType type : EggType.entries) {
				if (type.name.equals(eggType)) {
					return type;
				}
			}

			LOGGER.info("[Skyblocker Egg Finder] Failed to get egg type for egg '{}'", eggType);
			return null;
		}
	}

	static class Egg extends Waypoint {
		Egg(BlockPos pos, EggType eggType) {
			super(pos, SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(eggType.color), SkyblockerConfigManager.get().helpers.chocolateFactory.showThroughWalls);
		}
	}
}
