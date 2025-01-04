package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CorpseFinder {
	private static boolean isLocationCorrect = false;
	private static final Pattern CORPSE_FOUND_PATTERN = Pattern.compile("([A-Z]+) CORPSE LOOT!");
	private static final Pattern COORDS_PATTERN = Pattern.compile("x: (?<x>-?\\d+), y: (?<y>\\d+), z: (?<z>-?\\d+)");
	private static final String PREFIX = "[Skyblocker Corpse Finder] ";
	private static final Logger LOGGER = LoggerFactory.getLogger(CorpseFinder.class);
	private static final Map<CorpseType, List<Corpse>> corpsesByType = new EnumMap<>(CorpseType.class);
	private static final String LAPIS_HELMET = "LAPIS_ARMOR_HELMET";
	private static final String UMBER_HELMET = "ARMOR_OF_YOG_HELMET";
	private static final String TUNGSTEN_HELMET = "MINERAL_HELMET";
	private static final String VANGUARD_HELMET = "VANGUARD_HELMET";

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> {
			isLocationCorrect = false;
			corpsesByType.clear();
		});
		SkyblockEvents.LOCATION_CHANGE.register(CorpseFinder::handleLocationChange);
		ClientReceiveMessageEvents.GAME.register(CorpseFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(CorpseFinder::renderWaypoints);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || client.player == null) return;
			if (!isLocationCorrect) return;
			for (List<Corpse> corpses : corpsesByType.values()) {
				for (Corpse corpse : corpses) {
					if (!corpse.seen && client.player.canSee(corpse.entity)) {
						setSeen(corpse);
					}
				}
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("corpseHelper")
						.then(literal("shareLocation")
								.then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
										.then(argument("corpseType", CorpseType.CorpseTypeArgumentType.corpseType())
												.executes(context -> {
													shareLocation(ClientBlockPosArgumentType.getBlockPos(context, "blockPos"), CorpseType.CorpseTypeArgumentType.getCorpseType(context, "corpseType"));
													return Command.SINGLE_SUCCESS;
												})
										)
								)
						)
				)
		));
	}

	private static boolean seenDebugWarning = false;

	private static void handleLocationChange(Location location) {
		isLocationCorrect = location == Location.GLACITE_MINESHAFT;
	}

	public static void checkIfCorpse(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) checkIfCorpse(armorStand);
	}

	public static void checkIfCorpse(ArmorStandEntity armorStand) {
		if (!isLocationCorrect || !SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder) return;
		if (armorStand.hasCustomName() || armorStand.isInvisible() || armorStand.shouldShowBasePlate()) return;
		handleArmorStand(armorStand);
	}

	private static void handleArmorStand(ArmorStandEntity armorStand) {
		String helmetItemId = ItemUtils.getItemId(armorStand.getEquippedStack(EquipmentSlot.HEAD));
		CorpseType corpseType = CorpseType.fromHelmetItemId(helmetItemId);
		if (corpseType == CorpseType.UNKNOWN) return;

		LOGGER.debug(PREFIX + "Triggered code for handleArmorStand and matched with ITEM_IDS");
		List<Corpse> corpses = corpsesByType.computeIfAbsent(corpseType, k -> new ArrayList<>());
		if (corpses.stream().noneMatch(c -> c.entity.getBlockPos().equals(armorStand.getBlockPos()))) {
			Waypoint corpseWaypoint;
			float[] color = getColors(corpseType.color);
			corpseWaypoint = new Waypoint(armorStand.getBlockPos().up(), Waypoint.Type.OUTLINED_WAYPOINT, color);
			if (Debug.debugEnabled() && SkyblockerConfigManager.get().debug.corpseFinderDebug && !seenDebugWarning && (seenDebugWarning = true)) {
				MinecraftClient.getInstance().player.sendMessage(
						Constants.PREFIX.get().append(
								Text.literal("Corpse finder debug mode is active! Please use it only for the sake of debugging corpse detection!")
										.formatted(Formatting.GOLD, Formatting.BOLD)
						), false);
			}
			Corpse newCorpse = new Corpse(armorStand, corpseWaypoint, corpseType);
			corpses.add(newCorpse);
		}
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || !isLocationCorrect) return;
		for (List<Corpse> corpses : corpsesByType.values()) {
			for (Corpse corpse : corpses) {
				if (corpse.waypoint.shouldRender() && (corpse.seen || (Debug.debugEnabled() && SkyblockerConfigManager.get().debug.corpseFinderDebug))) {
					corpse.waypoint.render(context);
				}
			}
		}
	}

	private static void onChatMessage(Text text, boolean overlay) {
		if (overlay || !isLocationCorrect || !SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || MinecraftClient.getInstance().player == null) return;
		String string = text.getString();
		if (string.contains(MinecraftClient.getInstance().getSession().getUsername())) return; // Ignore your own messages
		if (SkyblockerConfigManager.get().mining.glacite.enableParsingChatCorpseFinder) parseCords(text);  // parsing cords from chat

		Matcher matcherCorpse = CORPSE_FOUND_PATTERN.matcher(string);
		if (!matcherCorpse.find()) return;

		LOGGER.debug(PREFIX + "Triggered code for onChatMessage");
		LOGGER.debug(PREFIX + "State of corpsesByType: {}", corpsesByType);
		String corpseTypeString = matcherCorpse.group(1).toUpperCase(Locale.ENGLISH);
		CorpseType corpseType = EnumUtils.getEnum(CorpseType.class, corpseTypeString, CorpseType.UNKNOWN);

		List<Corpse> corpses = corpsesByType.get(corpseType);
		if (corpses == null) {
			LOGGER.warn(PREFIX + "Couldn't get corpses! corpse type string: {}, parsed corpse type: {}", corpseTypeString, corpseType);
			return;
		}
		corpses.stream() // Since squared distance comparison will yield the same result as normal distance comparison, we can use squared distance to avoid square root calculation
				.min(Comparator.comparingDouble(corpse -> corpse.entity.squaredDistanceTo(MinecraftClient.getInstance().player)))
				.ifPresentOrElse(
						corpse -> {
							LOGGER.info(PREFIX + "Found corpse, marking as found! {}: {}", corpse.entity.getType(), corpse.entity.getBlockPos().toShortString());
							corpse.waypoint.setFound();
						},
						() -> LOGGER.warn(PREFIX + "Couldn't find the closest corpse despite triggering onChatMessage!")
				);
	}

	@SuppressWarnings("DataFlowIssue")
	private static void setSeen(Corpse corpse) {
		corpse.seen = true;
		if (SkyblockerConfigManager.get().mining.glacite.autoShareCorpses) {
			shareLocation(corpse.entity.getBlockPos().up(), corpse.corpseType);
			return; // There's no need to send the message twice, so we return here.
		}
		if (Util.getMeasuringTimeMs() - corpse.messageLastSent < 300) return;

		corpse.messageLastSent = Util.getMeasuringTimeMs();

		MinecraftClient.getInstance().player.sendMessage(
				Constants.PREFIX.get()
						.append("Found a ")
						.append(Text.literal(WordUtils.capitalizeFully(corpse.corpseType.asString()) + " Corpse")
								.withColor(corpse.corpseType.color.getColorValue()))
						.append(" at " + corpse.entity.getBlockPos().up().toShortString() + "!")
						.styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker corpseHelper shareLocation " + PosUtils.toSpaceSeparatedString(corpse.waypoint.pos) + " " + corpse.corpseType))
								.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to share the location in chat!").formatted(Formatting.GREEN)))), false);
	}

	private static void shareLocation(BlockPos pos, CorpseType corpseType) {
		MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + toSkyhanniFormat(pos) + " | (" + WordUtils.capitalizeFully(corpseType.asString()) + " Corpse)", true);
	}

	@SuppressWarnings("DataFlowIssue")
	private static float[] getColors(Formatting color) {
		return ColorUtils.getFloatComponents(color.getColorValue());
	}

	// Since read in their format, might as well send in their format too.
	// Some other mods seem to send in this same format, so it'll help any other mods that might be listening for this format.
	private static String toSkyhanniFormat(BlockPos pos) {
		return String.format("x: %d, y: %d, z: %d", pos.getX() + 1, pos.getY(), pos.getZ() + 1);
	}

	private static void parseCords(Text text) {
		String message = text.getString();
		Matcher matcher = COORDS_PATTERN.matcher(message);
		if (!matcher.find()) return;

		int x, y, z;
		try {
			x = Integer.parseInt(matcher.group("x"));
			y = Integer.parseInt(matcher.group("y"));
			z = Integer.parseInt(matcher.group("z"));
		} catch (NumberFormatException e) {
			LOGGER.warn(PREFIX + "Failed to parse message: `{}`, reason: {}", message, e.getMessage());
			return;
		}

		LOGGER.debug(PREFIX + "Parsed message! X:{}, Y:{}, Z:{}", x, y, z);
		boolean foundCorpse = false;
		BlockPos parsedPos = new BlockPos(x - 1, y, z - 1); // skyhanni cords format difference is -1, 0, -1

		for (List<Corpse> corpses : corpsesByType.values()) {
			for (Corpse corpse : corpses) {
				if (corpse.waypoint.pos.equals(parsedPos)) {
					corpse.seen = true;
					foundCorpse = true;
					LOGGER.info(PREFIX + "Setting corpse {} as seen!", corpse.entity);
					MinecraftClient.getInstance().player.sendMessage(
							Constants.PREFIX.get()
									.append("Parsed message from chat, adding corpse at ")
									.append(corpse.entity.getBlockPos().toShortString()), false);
					break;
				}
			}
		}
		if (!foundCorpse) {
			LOGGER.warn(PREFIX + "Did NOT find any match for corpses! corpsesByType.values(): {}", corpsesByType.values());
			LOGGER.info(PREFIX + "Proceeding to iterate over all corpses!");
			for (List<Corpse> corpses : corpsesByType.values()) {
				for (Corpse corpse : corpses) {
					LOGGER.info(PREFIX + "Corpse: {}, BlockPos: {}", corpse.entity, corpse.entity.getBlockPos());
				}
			}
		}
	}

	enum CorpseType implements StringIdentifiable {
		LAPIS(LAPIS_HELMET, Formatting.BLUE), // dark blue looks bad and these two never exist in same shaft
		UMBER(UMBER_HELMET, Formatting.RED),
		TUNGSTEN(TUNGSTEN_HELMET, Formatting.GRAY),
		VANGUARD(VANGUARD_HELMET, Formatting.BLUE),
		UNKNOWN("UNKNOWN", Formatting.YELLOW);
		private static final Codec<CorpseType> CODEC = StringIdentifiable.createCodec(CorpseType::values);
		private final String helmetItemId;
		private final Formatting color;

		CorpseType(String helmetItemId, Formatting color) {
			this.helmetItemId = helmetItemId;
			this.color = color;
		}

		static CorpseType fromHelmetItemId(String helmetItemId) {
			for (CorpseType value : values()) {
				if (value.helmetItemId.equals(helmetItemId)) {
					return value;
				}
			}
			return UNKNOWN;
		}

		@Override
		public String asString() {
			return name().toLowerCase();
		}

		static class CorpseTypeArgumentType extends EnumArgumentType<CorpseType> {
			protected CorpseTypeArgumentType() {
				super(CODEC, CorpseType::values);
			}

			static CorpseTypeArgumentType corpseType() {
				return new CorpseTypeArgumentType();
			}

			static <S> CorpseType getCorpseType(CommandContext<S> context, String name) {
				return context.getArgument(name, CorpseType.class);
			}
		}
	}

	static class Corpse {
		private final ArmorStandEntity entity;
		/**
		 * Waypoint position is always 1 above entity position
		 */
		private final Waypoint waypoint;
		/**
		 * Type of the corpse, fully uppercased.
		 */
		private final CorpseType corpseType;
		// TODO: migrate to seen waypoint #1108
		private boolean seen;
		private long messageLastSent = 0;

		Corpse(ArmorStandEntity entity, Waypoint waypoint, CorpseType corpseType) {
			this.entity = entity;
			this.waypoint = waypoint;
			this.seen = false;
			this.corpseType = corpseType;
		}
	}
}
