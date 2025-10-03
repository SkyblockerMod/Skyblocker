package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.mojang.brigadier.Command;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.EggTypeArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.SeenWaypoint;
import de.hysky.skyblocker.utils.ws.Service;
import de.hysky.skyblocker.utils.ws.WsMessageHandler;
import de.hysky.skyblocker.utils.ws.WsStateManager;
import de.hysky.skyblocker.utils.ws.message.EggWaypointMessage;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Nullable;
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
	private static final Pattern eggFoundPattern = Pattern.compile("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper) Egg");
	private static final Pattern newEggPattern = Pattern.compile("^HOPPITY'S HUNT A Chocolate (Breakfast|Lunch|Dinner|Brunch|Déjeuner|Supper) Egg has appeared!$");
	private static final Logger logger = LoggerFactory.getLogger("Skyblocker Egg Finder");
	/**
	 * The locations that the egg finder should work while the player is in.
	 */
	private static final Set<Location> possibleLocations = Set.of(Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.HUB, Location.THE_END, Location.THE_PARK, Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.SPIDERS_DEN, Location.THE_FARMING_ISLAND);

	private EggFinder() {}

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> clearEggs());
		SkyblockEvents.LOCATION_CHANGE.register(EggFinder::handleLocationChange);
		ClientReceiveMessageEvents.ALLOW_GAME.register(EggFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggFinder::renderWaypoints);

		SkyblockTime.HOUR_CHANGE.register(hour -> {
			int dayNumber = SkyblockTime.skyblockMonth.get().ordinal() * 31 + SkyblockTime.skyblockDay.get();
			boolean isOdd = dayNumber % 2 == 1;

			for (EggType type : EggType.entries) {
				if (hour == type.resetHour && isOdd == type.oddDay) {
					type.collected = false;
					type.egg = null;
					if (MinecraftClient.getInstance().player == null) return;
					MinecraftClient.getInstance().player.sendMessage(Text.literal("Resetting " + type.name() + "..."), false);
				}
			}
		});
		SkyblockTime.SEASON_CHANGE.register(season -> {
			if (season != SkyblockTime.Season.SPRING) clearEggs();
		});

		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("eggFinder")
						.then(literal("shareLocation")
								.then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
										.then(argument("eggType", EggTypeArgumentType.eggType())
												.executes(context -> {
													MessageScheduler.INSTANCE.sendMessageAfterCooldown("[Skyblocker] Chocolate " + context.getArgument("eggType", EggType.class) + " Egg found at " + context.getArgument("blockPos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource()).toShortString() + "!", false);
													return Command.SINGLE_SUCCESS;
												})))))));
	}

	public static void onWebsocketMessage(EggWaypointMessage message) {
		EggType eggType = message.eggType();
		eggType.egg = new Egg(message.coordinates(), eggType);
		eggType.setSeen();
	}

	private static void clearEggs() {
		for (EggType type : EggType.entries) {
			type.egg = null;
		}
	}

	private static void handleLocationChange(Location location) {
		clearEggs();
		if (!possibleLocations.contains(location)) return;

		WsStateManager.subscribeIsland(Service.EGG_WAYPOINTS, Optional.empty());
	}

	public static boolean checkIfEgg(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return false;
		if (SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return false;
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || armorStand.shouldShowBasePlate()) return false;
		return handleArmorStand(armorStand);
	}

	private static boolean handleArmorStand(ArmorStandEntity armorStand) {
		for (ItemStack itemStack : ItemUtils.getArmor(armorStand)) {
			Optional<String> texture = ItemUtils.getHeadTextureOptional(itemStack);
			if (texture.isEmpty()) continue;
			for (EggType type : EggType.entries) {
				if (type.egg == null && texture.get().equals(type.texture)) {
					return true;
				}
			}
		}
		return false;
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			Egg egg = type.egg;
			if (egg != null) egg.render(context);
		}
	}

	@SuppressWarnings("SameReturnValue")
	private static boolean onChatMessage(Text text, boolean overlay) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return true;
		Matcher matcher = eggFoundPattern.matcher(text.getString());
		if (matcher.find()) {
			try {
				EggType eggType = EggType.getTypeByName(matcher.group(1));
				if (eggType == null) return true;

				eggType.collected = true;
				if (eggType.egg != null) {
					eggType.egg.setFound();
					return true;
				}

				logger.info("[Skyblocker Egg Finder] Discovered a new egg!");
				MinecraftClient client = MinecraftClient.getInstance();
				if (client.player == null || client.world == null) return true;
				List<ArmorStandEntity> entities = client.world.getEntitiesByClass(ArmorStandEntity.class,
						Box.of(client.player.getPos(), 4f, 4f, 4f),
						EggFinder::checkIfEgg
				);

				if (entities.size() != 1) return true;
				eggType.egg = new Egg(entities.getFirst().getBlockPos().up(2), eggType);
				client.player.sendMessage(Text.literal("Sending waypoint over websocket..."), false);
				WsMessageHandler.sendLocationMessage(Service.EGG_WAYPOINTS, new EggWaypointMessage(eggType, 0, eggType.egg.pos));
				eggType.setSeen();
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg found message. Tried to match against: {}", matcher.group(0), e);
			}
		}

		matcher.usePattern(newEggPattern);
		if (matcher.find()) {
			try {
				EggType eggType = EggType.getTypeByName(matcher.group(1));
				if (eggType == null) return true;
				eggType.egg = null;
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg spawn message. Tried to match against: {}", matcher.group(0), e);
			}
		}

		return true;
	}

	@SuppressWarnings("DataFlowIssue") //Removes that pesky "unboxing of Integer might cause NPE" warning when we already know it's not null
	public enum EggType implements StringIdentifiable {
		BREAKFAST("Breakfast", Formatting.GOLD.getColorValue(), 7, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0", true),
		LUNCH("Lunch", Formatting.BLUE.getColorValue(), 14, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9", true),
		DINNER("Dinner", Formatting.GREEN.getColorValue(), 21, "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0", true),
		BRUNCH("Brunch", BREAKFAST.color, BREAKFAST.resetHour, BREAKFAST.texture, false),
		DEJEUNER("Déjeuner", LUNCH.color, LUNCH.resetHour, LUNCH.texture, false),
		SUPPER("Supper", DINNER.color, DINNER.resetHour, DINNER.texture, false);

		public static final Codec<EggType> CODEC = StringIdentifiable.createBasicCodec(EggType::values);

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

		EggType(String name, int color, int resetHour, String texture, Boolean oddDay) {
			logger.info("Creating new egg type: {}...", name);
			this.name = name;
			this.color = color;
			this.resetHour = resetHour;
			this.texture = texture;
			this.oddDay = oddDay;
		}

		public void setSeen() {
			if (!SkyblockerConfigManager.get().helpers.chocolateFactory.sendEggFoundMessages) return;
			if (collected) {
				egg.setFound();
				return;
			}
			MinecraftClient.getInstance().player.sendMessage(
					Constants.PREFIX.get()
							.append("Found a ")
							.append(Text.literal("Chocolate " + this.name + " Egg")
									.withColor(color))
							.append(" at " + egg.pos.toShortString() + "!")
							.styled(style -> style.withClickEvent(new ClickEvent.RunCommand("/skyblocker eggFinder shareLocation " + PosUtils.toSpaceSeparatedString(egg.pos) + " " + this))
									.withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to share the location in chat!").formatted(Formatting.GREEN)))), false);
		}

		@Override
		public String toString() {
			return WordUtils.capitalizeFully(this.name());
		}

		@Override
		public String asString() {
			return name;
		}

		@Nullable
		public static EggType getTypeByName(String eggType) {
			for (EggType type : EggType.entries) {
				if (type.name.equals(eggType)) {
					return type;
				}
			}

			logger.info("[Skyblocker Egg Finder] Failed to get egg type for egg '{}'", eggType);
			return null;
		}
	}

	static class Egg extends SeenWaypoint {
		Egg(BlockPos pos, EggType eggType) {
			super(pos, SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(eggType.color));
		}
	}
}
