package de.hysky.skyblocker.skyblock.chocolatefactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.SkyblockTime;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.mutable.MutableObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EggFinder {
	private static final Pattern eggFoundPattern = Pattern.compile("^(?:HOPPITY'S HUNT You found a Chocolate|You have already collected this Chocolate) (Breakfast|Lunch|Dinner)");
	private static final Pattern newEggPattern = Pattern.compile("^HOPPITY'S HUNT A Chocolate (Breakfast|Lunch|Dinner) Egg has appeared!$");
	private static final Logger logger = LoggerFactory.getLogger("Skyblocker Egg Finder");

	private EggFinder() {
	}

	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> clearEggs());
		ClientReceiveMessageEvents.GAME.register(EggFinder::onChatMessage);
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggFinder::renderWaypoints);
	}

	public static void checkIfEgg(Entity entity) {
		if (entity instanceof ArmorStandEntity armorStand) checkIfEgg(armorStand);
	}

	public static void checkIfEgg(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		if (SkyblockTime.skyblockSeason.get() != SkyblockTime.Season.SPRING) return;
		if (armorStand.hasCustomName() || !armorStand.isInvisible() || !armorStand.shouldHideBasePlate()) return;
		for (ItemStack itemStack : armorStand.getArmorItems()) {
			ItemUtils.getHeadTextureOptional(itemStack).ifPresent(texture -> {
				for (EggType type : EggType.entries) {
					if (texture.equals(type.texture) && type.egg.getValue() == null) {
						handleFoundEgg(armorStand, type);
						return;
					}
				}
			});
		}
	}

	private static void clearEggs() {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			type.egg.setValue(null);
		}
	}

	private static void handleFoundEgg(ArmorStandEntity entity, EggType eggType) {
		eggType.egg.setValue(new Egg(entity, new Waypoint(entity.getBlockPos().up(2), SkyblockerConfigManager.get().helpers.chocolateFactory.waypointType, ColorUtils.getFloatComponents(eggType.color))));

		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.sendEggFoundMessages) return;
		MinecraftClient.getInstance().player.sendMessage(Constants.PREFIX.get()
		                                                                 .append("Found a ")
		                                                                 .append(Text.literal("Chocolate " + eggType + " Egg")
		                                                                             .withColor(eggType.color))
		                                                                 .append(" at " + entity.getBlockPos().up(2).toShortString() + "!"));
	}

	private static void renderWaypoints(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		for (EggType type : EggType.entries) {
			Egg egg = type.egg.getValue();
			if (egg != null && egg.waypoint.shouldRender()) egg.waypoint.render(context);
		}
	}

	private static void onChatMessage(Text text, boolean overlay) {
		if (overlay || !SkyblockerConfigManager.get().helpers.chocolateFactory.enableEggFinder) return;
		Matcher matcher = eggFoundPattern.matcher(text.getString());
		if (matcher.find()) {
			try {
				Egg egg = EggType.valueOf(matcher.group(1).toUpperCase()).egg.getValue();
				if (egg != null) egg.waypoint.setFound();
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg found message. Tried to match against: " + matcher.group(0), e);
			}
		}

		//There's only one egg of the same type at any given time, so we can set the changed egg to null
		matcher = newEggPattern.matcher(text.getString());
		if (matcher.find()) {
			try {
				EggType.valueOf(matcher.group(1).toUpperCase()).egg.setValue(null);
			} catch (IllegalArgumentException e) {
				logger.error("[Skyblocker Egg Finder] Failed to find egg type for egg spawn message. Tried to match against: " + matcher.group(0), e);
			}
		}
	}

	record Egg(ArmorStandEntity entity, Waypoint waypoint) { }

	enum EggType {
		LUNCH(new MutableObject<>(), Formatting.BLUE.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjU2ODExMiwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdhZTZkMmQzMWQ4MTY3YmNhZjk1MjkzYjY4YTRhY2Q4NzJkNjZlNzUxZGI1YTM0ZjJjYmM2NzY2YTAzNTZkMGEiCiAgICB9CiAgfQp9"),
		DINNER(new MutableObject<>(), Formatting.GREEN.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY0OTcwMSwKICAicHJvZmlsZUlkIiA6ICI3NGEwMzQxNWY1OTI0ZTA4YjMyMGM2MmU1NGE3ZjJhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZXp6aXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTVlMzYxNjU4MTlmZDI4NTBmOTg1NTJlZGNkNzYzZmY5ODYzMTMxMTkyODNjMTI2YWNlMGM0Y2M0OTVlNzZhOCIKICAgIH0KICB9Cn0"),
		BREAKFAST(new MutableObject<>(), Formatting.GOLD.getColorValue(), "ewogICJ0aW1lc3RhbXAiIDogMTcxMTQ2MjY3MzE0OSwKICAicHJvZmlsZUlkIiA6ICJiN2I4ZTlhZjEwZGE0NjFmOTY2YTQxM2RmOWJiM2U4OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJBbmFiYW5hbmFZZzciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYTQ5MzMzZDg1YjhhMzE1ZDAzMzZlYjJkZjM3ZDhhNzE0Y2EyNGM1MWI4YzYwNzRmMWI1YjkyN2RlYjUxNmMyNCIKICAgIH0KICB9Cn0");

		public final MutableObject<Egg> egg;
		public final int color;
		public final String texture;

		//This is to not create an array each time we iterate over the values
		public static final ObjectImmutableList<EggType> entries = ObjectImmutableList.of(BREAKFAST, LUNCH, DINNER);

		EggType(MutableObject<Egg> egg, int color, String texture) {
			this.egg = egg;
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
