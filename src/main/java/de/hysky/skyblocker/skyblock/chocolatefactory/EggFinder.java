package de.hysky.skyblocker.skyblock.chocolatefactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EggFinder {
	private static final Pattern eggFoundPattern = Pattern.compile("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner)");
	private static final Pattern newEggPattern = Pattern.compile("^HOPPITY'S HUNT A Chocolate (Breakfast|Lunch|Dinner) Egg has appeared!$");
	private static final Logger logger = LoggerFactory.getLogger("Skyblocker Egg Finder");
	private static final LinkedList<ArmorStandEntity> armorStandQueue = new LinkedList<>();
	private static final Location[] possibleLocations = {Location.CRIMSON_ISLE, Location.CRYSTAL_HOLLOWS, Location.DUNGEON_HUB, Location.DWARVEN_MINES, Location.HUB, Location.THE_END, Location.THE_PARK, Location.GOLD_MINE, Location.DEEP_CAVERNS, Location.SPIDERS_DEN, Location.THE_FARMING_ISLAND};
	private static boolean isLocationCorrect = false;

	private EggFinder() {
	}

	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> invalidateState());
		SkyblockEvents.LOCATION_CHANGE.register(EggFinder::handleLocationChange);
		ClientReceiveMessageEvents.GAME.register(EggFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggFinder::renderWaypoints);
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(ClientCommandManager.literal(SkyblockerMod.NAMESPACE)
		                                                                                                                         .then(ClientCommandManager.literal("eggFinder")
		                                                                                                                                                   .then(ClientCommandManager.literal("shareLocation")
		                                                                                                                                                                             .then(ClientCommandManager.argument("x", IntegerArgumentType.integer())
		                                                                                                                                                                                                       .then(ClientCommandManager.argument("y", IntegerArgumentType.integer())
		                                                                                                                                                                                                                                 .then(ClientCommandManager.argument("z", IntegerArgumentType.integer())
		                                                                                                                                                                                                                                                           .then(ClientCommandManager.argument("eggType", StringArgumentType.word())
		                                                                                                                                                                                                                                                                                     .executes(context -> {
			                                                                                                                                                                                                                                                                                     MessageScheduler.INSTANCE.sendMessageAfterCooldown("[Skyblocker] Chocolate " + context.getArgument("eggType", String.class) + " Egg found at " + context.getArgument("x", Integer.class) + " " + context.getArgument("y", Integer.class) + " " + context.getArgument("z", Integer.class) + "!");
			                                                                                                                                                                                                                                                                                     return Command.SINGLE_SUCCESS;
		                                                                                                                                                                                                                                                                                     })))))))));
	}

	private static void handleLocationChange(Location location) {
		for (Location possibleLocation : possibleLocations) {
			if (location == possibleLocation) {
				isLocationCorrect = true;
				break;
			}
		}
		if (!isLocationCorrect) {
			armorStandQueue.clear();
			return;
		}

		while (!armorStandQueue.isEmpty()) {
			handleArmorStand(armorStandQueue.poll());
		}
	}

	public static void checkIfEgg(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) checkIfEgg(armorStand);
	}

	public static void checkIfEgg(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		if (SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return;
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || !armorStand.shouldHideBasePlate()) return;
		if (Utils.getLocation() == Location.UNKNOWN) { //The location is unknown upon world change and will be changed via /locraw soon, so we can queue it for now
			armorStandQueue.add(armorStand);
			return;
		}
		if (isLocationCorrect) handleArmorStand(armorStand);
	}

	private static void handleArmorStand(ArmorStandEntity armorStand) {
		for (ItemStack itemStack : armorStand.getArmorItems()) {
			ItemUtils.getHeadTextureOptional(itemStack).ifPresent(texture -> {
				for (EggType type : EggType.entries) { //Compare blockPos rather than entity to avoid incorrect matches when the entity just moves rather than a new one being spawned elsewhere
					if (texture.equals(type.texture) && (type.egg == null || !type.egg.entity.getBlockPos().equals(armorStand.getBlockPos()))) {
						handleFoundEgg(armorStand, type);
						return;
					}
				}
			});
		}
	}

	private static void invalidateState() {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		isLocationCorrect = false;
		for (EggType type : EggType.entries) {
			type.egg = null;
		}
	}

	private static void handleFoundEgg(ArmorStandEntity entity, EggType eggType) {
		eggType.egg = new Egg(entity, new Waypoint(entity.getBlockPos().up(2), SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(eggType.color)));

		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.sendEggFoundMessages || System.currentTimeMillis() - eggType.messageLastSent < 1000) return;
		eggType.messageLastSent = System.currentTimeMillis();
		MinecraftClient.getInstance().player.sendMessage(
				Constants.PREFIX.get()
				                .append("Found a ")
				                .append(Text.literal("Chocolate " + eggType + " Egg")
				                            .withColor(eggType.color))
				                .append(" at " + entity.getBlockPos().up(2).toShortString() + "!")
				                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker eggFinder shareLocation " + entity.getBlockX() + " " + (entity.getBlockY() + 2) + " " + entity.getBlockZ() + " " + eggType))
				                                      .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to share the location in chat!").formatted(Formatting.GREEN)))));
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			Egg egg = type.egg;
			if (egg != null && egg.waypoint.shouldRender()) egg.waypoint.render(context);
		}
	}

	private static void onChatMessage(Text text, boolean overlay) {
		if (overlay || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		Matcher matcher = eggFoundPattern.matcher(text.getString());
		if (matcher.find()) {
			try {
				Egg egg = EggType.valueOf(matcher.group(1).toUpperCase()).egg;
				if (egg != null) egg.waypoint.setFound();
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg found message. Tried to match against: " + matcher.group(0), e);
			}
		}

		matcher.usePattern(newEggPattern);
		if (matcher.find()) {
			try {
				EggType.valueOf(matcher.group(1).toUpperCase()).egg = null;
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg spawn message. Tried to match against: " + matcher.group(0), e);
			}
		}
	}

	record Egg(ArmorStandEntity entity, Waypoint waypoint) {}

	@SuppressWarnings("DataFlowIssue") //Removes that pesky "unboxing of Integer might cause NPE" warning when we already know it's not null
	enum EggType {
		LUNCH(Formatting.BLUE.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9"),
		DINNER(Formatting.GREEN.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0"),
		BREAKFAST(Formatting.GOLD.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0");

		private Egg egg = null;
		public final int color;
		public final String texture;
		/*
			When a new egg spawns in the player's range, the order of packets/messages goes like this:
			set_equipment -> new egg message -> set_entity_data
			We have to set the egg to null to prevent the highlight from staying where it was before the new egg spawned,
			and doing so causes the found message to get sent twice. This is the reason for the existence of this field, so that we can not send the 2nd message.
			This doesn't fix the field being set twice, but that's not an issue anyway. It'd be much harder to fix the highlight issue mentioned above if it wasn't being set twice.
		 */
		private long messageLastSent = 0;

		//This is to not create an array each time we iterate over the values
		public static final ObjectImmutableList<EggType> entries = ObjectImmutableList.of(BREAKFAST, LUNCH, DINNER);

		EggType(int color, String texture) {
			this.color = color;
			this.texture = texture;
		}

		@Override
		public String toString() {
			return switch (this) {
				case LUNCH -> "Lunch";
				case DINNER -> "Dinner";
				case BREAKFAST -> "Breakfast";
			};
		}
	}
}
