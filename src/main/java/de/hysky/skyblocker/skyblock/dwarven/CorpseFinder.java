package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.skyblock.dwarven.CorpseType.CorpseTypeArgumentType;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.PosUtils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> {
			isLocationCorrect = false;
			corpsesByType.clear();
		});
		SkyblockEvents.LOCATION_CHANGE.register(CorpseFinder::handleLocationChange);
		ClientReceiveMessageEvents.ALLOW_GAME.register(CorpseFinder::onChatMessage);
		WorldRenderExtractionCallback.EVENT.register(CorpseFinder::extractRendering);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || client.player == null) return;
			if (!isLocationCorrect) return;
			for (List<Corpse> corpses : corpsesByType.values()) {
				for (Corpse corpse : corpses) {
					if (!corpse.seen && client.player.hasLineOfSight(corpse.entity)) {
						setSeen(corpse);
					}
				}
			}
		});
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("corpseHelper")
						.then(literal("shareLocation")
								.then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
										.then(argument("corpseType", CorpseTypeArgumentType.corpseType())
												.executes(context -> {
													shareLocation(ClientBlockPosArgumentType.getBlockPos(context, "blockPos"), CorpseTypeArgumentType.getCorpseType(context, "corpseType"));
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
		isLocationCorrect = location == Location.GLACITE_MINESHAFTS;
	}

	public static void checkIfCorpse(Entity entity) {
		if (entity instanceof ArmorStand armorStand) checkIfCorpse(armorStand);
	}

	public static void checkIfCorpse(ArmorStand armorStand) {
		if (!isLocationCorrect || !SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder) return;
		if (armorStand.hasCustomName() || armorStand.isInvisible() || armorStand.showBasePlate()) return;
		handleArmorStand(armorStand);
	}

	private static void handleArmorStand(ArmorStand armorStand) {
		String helmetItemId = armorStand.getItemBySlot(EquipmentSlot.HEAD).getSkyblockId();
		CorpseType corpseType = CorpseType.fromHelmetItemId(helmetItemId);
		if (corpseType == CorpseType.UNKNOWN) return;

		LOGGER.debug(PREFIX + "Triggered code for handleArmorStand and matched with ITEM_IDS");
		List<Corpse> corpses = corpsesByType.computeIfAbsent(corpseType, k -> new ArrayList<>());
		if (corpses.stream().noneMatch(c -> c.entity.blockPosition().equals(armorStand.blockPosition()))) {
			Waypoint corpseWaypoint;
			float[] color = getColors(corpseType.color);
			corpseWaypoint = new Waypoint(armorStand.blockPosition().above(), Waypoint.Type.OUTLINED_WAYPOINT, color);
			if (Debug.debugEnabled() && SkyblockerConfigManager.get().debug.corpseFinderDebug && !seenDebugWarning && (seenDebugWarning = true)) {
				Minecraft.getInstance().player.displayClientMessage(
						Constants.PREFIX.get().append(
								Component.literal("Corpse finder debug mode is active! Please use it only for the sake of debugging corpse detection!")
										.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
						), false);
			}
			Corpse newCorpse = new Corpse(armorStand, corpseWaypoint, corpseType);
			corpses.add(newCorpse);
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || !isLocationCorrect) return;
		for (List<Corpse> corpses : corpsesByType.values()) {
			for (Corpse corpse : corpses) {
				if (corpse.waypoint.shouldRender() && (corpse.seen || (Debug.debugEnabled() && SkyblockerConfigManager.get().debug.corpseFinderDebug))) {
					corpse.waypoint.extractRendering(collector);
				}
			}
		}
	}

	private static boolean onChatMessage(Component text, boolean overlay) {
		if (overlay || !isLocationCorrect || !SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || Minecraft.getInstance().player == null) return true;
		String string = text.getString();
		if (string.contains(Minecraft.getInstance().getUser().getName())) return true; // Ignore your own messages
		if (SkyblockerConfigManager.get().mining.glacite.enableParsingChatCorpseFinder) parseCords(text);  // parsing cords from chat

		Matcher matcherCorpse = CORPSE_FOUND_PATTERN.matcher(string);
		if (!matcherCorpse.find()) return true;

		LOGGER.debug(PREFIX + "Triggered code for onChatMessage");
		LOGGER.debug(PREFIX + "State of corpsesByType: {}", corpsesByType);
		String corpseTypeString = matcherCorpse.group(1).toUpperCase(Locale.ENGLISH);
		CorpseType corpseType = EnumUtils.getEnum(CorpseType.class, corpseTypeString, CorpseType.UNKNOWN);

		List<Corpse> corpses = corpsesByType.get(corpseType);
		if (corpses == null) {
			LOGGER.warn(PREFIX + "Couldn't get corpses! corpse type string: {}, parsed corpse type: {}", corpseTypeString, corpseType);
			return true;
		}
		corpses.stream() // Since squared distance comparison will yield the same result as normal distance comparison, we can use squared distance to avoid square root calculation
				.min(Comparator.comparingDouble(corpse -> corpse.entity.distanceToSqr(Minecraft.getInstance().player)))
				.ifPresentOrElse(
						corpse -> {
							LOGGER.info(PREFIX + "Found corpse, marking as found! {}: {}", corpse.entity.getType(), corpse.entity.blockPosition().toShortString());
							corpse.waypoint.setFound();
						},
						() -> LOGGER.warn(PREFIX + "Couldn't find the closest corpse despite triggering onChatMessage!")
				);

		return true;
	}

	@SuppressWarnings("DataFlowIssue")
	private static void setSeen(Corpse corpse) {
		corpse.seen = true;
		if (SkyblockerConfigManager.get().mining.glacite.autoShareCorpses) {
			shareLocation(corpse.entity.blockPosition().above(), corpse.corpseType);
			return; // There's no need to send the message twice, so we return here.
		}
		if (Util.getMillis() - corpse.messageLastSent < 300) return;

		corpse.messageLastSent = Util.getMillis();

		Minecraft.getInstance().player.displayClientMessage(
				Constants.PREFIX.get()
						.append("Found a ")
						.append(Component.literal(WordUtils.capitalizeFully(corpse.corpseType.getSerializedName()) + " Corpse")
								.withColor(corpse.corpseType.color.getColor()))
						.append(" at " + corpse.entity.blockPosition().above().toShortString() + "!")
						.withStyle(style -> style.withClickEvent(new ClickEvent.RunCommand("/skyblocker corpseHelper shareLocation " + PosUtils.toSpaceSeparatedString(corpse.waypoint.pos) + " " + corpse.corpseType.toString().toLowerCase(Locale.ENGLISH)))
								.withHoverEvent(new HoverEvent.ShowText(Component.literal("Click to share the location in chat!").withStyle(ChatFormatting.GREEN)))), false);
	}

	private static void shareLocation(BlockPos pos, CorpseType corpseType) {
		MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + toSkyhanniFormat(pos) + " | (" + WordUtils.capitalizeFully(corpseType.getSerializedName()) + " Corpse)", true);
	}

	@SuppressWarnings("DataFlowIssue")
	private static float[] getColors(ChatFormatting color) {
		return ColorUtils.getFloatComponents(color.getColor());
	}

	// Since read in their format, might as well send in their format too.
	// Some other mods seem to send in this same format, so it'll help any other mods that might be listening for this format.
	private static String toSkyhanniFormat(BlockPos pos) {
		return String.format("x: %d, y: %d, z: %d", pos.getX() + 1, pos.getY(), pos.getZ() + 1);
	}

	private static void parseCords(Component text) {
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
					Minecraft.getInstance().player.displayClientMessage(
							Constants.PREFIX.get()
									.append("Parsed message from chat, adding corpse at ")
									.append(corpse.entity.blockPosition().toShortString()), false);
					break;
				}
			}
		}
		if (!foundCorpse) {
			LOGGER.warn(PREFIX + "Did NOT find any match for corpses! corpsesByType.values(): {}", corpsesByType.values());
			LOGGER.info(PREFIX + "Proceeding to iterate over all corpses!");
			for (List<Corpse> corpses : corpsesByType.values()) {
				for (Corpse corpse : corpses) {
					LOGGER.info(PREFIX + "Corpse: {}, BlockPos: {}", corpse.entity, corpse.entity.blockPosition());
				}
			}
		}
	}

	static class Corpse {
		private final ArmorStand entity;
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

		Corpse(ArmorStand entity, Waypoint waypoint, CorpseType corpseType) {
			this.entity = entity;
			this.waypoint = waypoint;
			this.seen = false;
			this.corpseType = corpseType;
		}
	}
}
