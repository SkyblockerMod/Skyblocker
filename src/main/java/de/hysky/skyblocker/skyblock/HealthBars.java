package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientEntityEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class HealthBars {
	private static final Logger LOGGER = LoggerFactory.getLogger(HealthBars.class);
	private static final Identifier HEALTH_BAR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/white_background.png");
	private static final Identifier HEALTH_BAR_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/white_progress.png");
	protected static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?[kKmMbBtT]?)/(\\d{1,3}(,\\d{3})*(\\.\\d+)?[kKmMbBtT]?)❤");
	protected static final Pattern HEALTH_ONLY_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?[kKmMbBtT]?)❤");

	private static final Object2FloatOpenHashMap<ArmorStandEntity> healthValues = new Object2FloatOpenHashMap<>();
	private static final Object2LongOpenHashMap<ArmorStandEntity> mobStartingHealth = new Object2LongOpenHashMap<>();

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(HealthBars::render);
		ClientEntityEvents.ENTITY_UNLOAD.register(HealthBars::onEntityDespawn);
	}

	private static void reset() {
		healthValues.clear();
		mobStartingHealth.clear();
	}

	/**
	 * remove dead armor stands from health bars
	 *
	 * @param entity dying entity
	 */
	public static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
		if (entity instanceof ArmorStandEntity armorStandEntity) {
			healthValues.removeFloat(armorStandEntity);
			mobStartingHealth.removeLong(armorStandEntity);
		}
	}

	/**
	 * Processes armorstand updates and if it's a mob with health get the value of its health and save it the hashmap
	 *
	 * @param armorStand updated armorstand
	 */
	public static void healthBar(ArmorStandEntity armorStand) {
		if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible() || !SkyblockerConfigManager.get().uiAndVisuals.healthBars.enabled) {
			return;
		}

		//check if armor stand is dead and remove it from list
		if (armorStand.isDead()) {
			healthValues.removeFloat(armorStand);
			mobStartingHealth.removeLong(armorStand);
			return;
		}

		//check to see if the armor stand is a mob label with health
		if (armorStand.getCustomName() == null) {
			return;
		}
		Matcher healthMatcher = HEALTH_PATTERN.matcher(armorStand.getCustomName().getString());
		//if a health ratio can not be found send onto health only pattern
		if (!healthMatcher.find()) {
			healthOnlyCheck(armorStand);
			return;
		}

		//work out health value and save to hashMap
		float firstValue = Formatters.parseNumber(healthMatcher.group(1).toUpperCase()).floatValue();
		float secondValue = Formatters.parseNumber(healthMatcher.group(4).toUpperCase()).floatValue();
		float health = firstValue / secondValue;
		healthValues.put(armorStand, health);

		//edit armor stand name to remove health
		boolean removeValue = SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeHealthFromName;
		boolean removeMax = SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeMaxHealthFromName;
		//if both disabled no need to edit name
		if (!removeValue && !removeMax) {
			return;
		}
		MutableText cleanedText = Text.empty();
		List<Text> parts = armorStand.getCustomName().getSiblings();
		//loop though name and add every part to a new text skipping over the hidden health values
		int healthStartIndex = -1;
		for (int i = 0; i < parts.size(); i++) {
			//remove value from name
			if (i < parts.size() - 4 && StringUtils.join(parts.subList(i + 1, i + 5).stream().map(Text::getString).toArray(), "").equals(healthMatcher.group(0))) {
				healthStartIndex = i;
			}
			if (healthStartIndex != -1) {
				//skip parts of the health offset form staring index
				switch (i - healthStartIndex) {
					case 0 -> { // space before health
						if (removeMax && removeValue) {
							continue;
						}
					}
					case 1 -> { // current health value
						if (removeValue) {
							continue;
						}
					}
					case 2 -> { // "/" separating health values
						if (removeMax) {
							continue;
						}
					}
					case 3 -> { // max health value
						if (removeMax) {
							continue;
						}
					}
					case 4 -> { // "❤" at end of health
						if (removeMax && removeValue) {
							continue;
						}
					}
				}
			}

			cleanedText.append(parts.get(i));
		}
		armorStand.setCustomName(cleanedText);
	}

	/**
	 * Processes armor stands that only have a health value and no max health
	 *
	 * @param armorStand armorstand to check the name of
	 */
	private static void healthOnlyCheck(ArmorStandEntity armorStand) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.healthBars.applyToHealthOnlyMobs || armorStand.getCustomName() == null) {
			return;
		}
		Matcher healthOnlyMatcher = HEALTH_ONLY_PATTERN.matcher(armorStand.getCustomName().getString());
		//if not found return
		if (!healthOnlyMatcher.find()) {
			return;
		}

		//get the current health of the mob
		long currentHealth = Formatters.parseNumber(healthOnlyMatcher.group(1).toUpperCase()).longValue();

		//if it's a new health only armor stand add to starting health lookup (not always full health if already damaged but best that can be done)
		if (!mobStartingHealth.containsKey(armorStand)) {
			mobStartingHealth.put(armorStand, currentHealth);
		}

		//add to health bar values
		float health = (float) currentHealth / mobStartingHealth.getLong(armorStand);
		healthValues.put(armorStand, health);

		//if enabled remove from name
		if (!SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeHealthFromName) {
			return;
		}
		MutableText cleanedText = Text.empty();
		List<Text> parts = armorStand.getCustomName().getSiblings();
		//loop though name and add every part to a new text skipping over the health value
		for (int i = 0; i < parts.size(); i++) {
			//skip space before value, value and heart from name
			if (i < parts.size() - 2 && parts.subList(i + 1, i + 3).stream().map(Text::getString).collect(Collectors.joining()).equals(healthOnlyMatcher.group(0))) {
				//skip the heart
				i += 2;
				continue;
			}
			cleanedText.append(parts.get(i));
		}
		armorStand.setCustomName(cleanedText);
	}

	/**
	 * Loops though armor stands with health bars and renders a bar for each of them just bellow the name label
	 *
	 * @param context render context
	 */
	private static void render(WorldRenderContext context) {
		if (!SkyblockerConfigManager.get().uiAndVisuals.healthBars.enabled || healthValues.isEmpty()) {
			return;
		}
		Color fullColor = SkyblockerConfigManager.get().uiAndVisuals.healthBars.fullBarColor;
		Color halfColor = SkyblockerConfigManager.get().uiAndVisuals.healthBars.halfBarColor;
		Color emptyColor = SkyblockerConfigManager.get().uiAndVisuals.healthBars.emptyBarColor;
		boolean hideFullHealth = SkyblockerConfigManager.get().uiAndVisuals.healthBars.hideFullHealth;
		float scale = SkyblockerConfigManager.get().uiAndVisuals.healthBars.scale;
		float tickDelta = context.tickCounter().getTickDelta(false);
		float width = scale;
		float height = scale * 0.1f;

		for (Object2FloatMap.Entry<ArmorStandEntity> healthValue : healthValues.object2FloatEntrySet()) {
			//if the health bar is full and the setting is enabled to hide it stop rendering it
			float health = healthValue.getFloatValue();
			if (hideFullHealth && health == 1) {
				continue;
			}

			ArmorStandEntity armorStand = healthValue.getKey();
			//only render health bar if name is visible
			if (!armorStand.shouldRenderName()) {
				return;
			}
			//gets the mixed color of the health bar
			int mixedColor = ColorUtils.interpolate(health, emptyColor.getRGB(), halfColor.getRGB(), fullColor.getRGB());
			float[] components = ColorUtils.getFloatComponents(mixedColor);
			// Render the health bar texture with scaling based on health percentage
			RenderHelper.renderTextureInWorld(context, armorStand.getCameraPosVec(tickDelta).add(0, 0.25 - height, 0), width * health, height, health, 1f, new Vec3d(width * -0.5f, 0, 0.003f), HEALTH_BAR_TEXTURE, components, 1f, true);
			RenderHelper.renderTextureInWorld(context, armorStand.getCameraPosVec(tickDelta).add(0, 0.25 - height, 0), width, height, 1f, 1f, new Vec3d(width * -0.5f, 0, 0), HEALTH_BAR_BACKGROUND_TEXTURE, components, 1f, true);

		}
	}
}
