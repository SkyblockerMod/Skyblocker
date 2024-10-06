package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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

import java.awt.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthBars {

	private static final Identifier HEALTH_BAR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/white_background.png");
	private static final Identifier HEALTH_BAR_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/white_progress.png");
	private static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?)/(\\d{1,3}(,\\d{3})*(\\.\\d+)?)❤");
	private static final Pattern HEALTH_ONLY_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?)❤");

	private static final Object2FloatOpenHashMap<ArmorStandEntity> healthValues = new Object2FloatOpenHashMap<>();
	private static final Object2IntOpenHashMap<ArmorStandEntity> mobStartingHealth = new Object2IntOpenHashMap<>();

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
	 * remove dead armour stands from health bars
	 *
	 * @param entity dying entity
	 */
	public static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
		if (entity instanceof ArmorStandEntity armorStandEntity) {
			healthValues.removeFloat(armorStandEntity);
			mobStartingHealth.removeInt(armorStandEntity);
		}
	}

	public static void HeathBar(ArmorStandEntity armorStand) {
		if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible() || !SkyblockerConfigManager.get().uiAndVisuals.healthBars.enabled) {
			return;
		}

		//check if armour stand is dead and remove it from list
		if (armorStand.isDead()) {
			System.out.println("somthing");
			healthValues.removeFloat(armorStand);
			mobStartingHealth.removeInt(armorStand);

			return;
		}

		//check to see if the armour stand is a mob label with health
		if (armorStand.getCustomName() == null) {
			return;
		}
		Matcher healthMatcher = HEALTH_PATTERN.matcher(armorStand.getCustomName().getString());
		//if a health ratio can not be found send onto health only pattern
		if (!healthMatcher.find()) {
			HealthOnlyCheck(armorStand);
			return;
		}

		//work out health value and save to hashMap
		int firstValue = Integer.parseInt(healthMatcher.group(1).replace(",", ""));
		int secondValue = Integer.parseInt(healthMatcher.group(4).replace(",", ""));
		float health = (float) firstValue / secondValue;
		healthValues.put(armorStand, health);

		//edit armour stand name to remove health
		boolean removeValue = SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeHealthFromName;
		boolean removeMax = SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeMaxHealthFromName;
		//if both disabled no need to edit name
		if (!removeValue && !removeMax) {
			return;
		}
		MutableText cleanedText = Text.empty();
		List<Text> parts = armorStand.getCustomName().getSiblings();
		for (int i = 0; i < parts.size(); i++) {
			//remove value from name
			if (removeValue && i < parts.size() - 3 && parts.get(i).getString().equals(healthMatcher.group(1)) && parts.get(i + 1).getString().equals("/") && parts.get(i + 2).getString().equals(healthMatcher.group(4)) && parts.get(i + 3).getString().equals("❤")) {
				continue;
			}
			//remove slash from max
			if (removeMax && i < parts.size() - 2 && parts.get(i).getString().equals("/") && parts.get(i + 1).getString().equals(healthMatcher.group(4)) && parts.get(i + 2).getString().equals("❤")) {
				continue;
			}
			//remove max
			if (removeMax && i < parts.size() - 1 && parts.get(i).getString().equals(healthMatcher.group(4)) && parts.get(i + 1).getString().equals("❤")) {
				continue;
			}
			//if both enabled remove "❤"
			if (removeValue && removeMax && parts.get(i).getString().equals("❤")) {
				continue;
			}
			cleanedText.append(parts.get(i));
		}
		armorStand.setCustomName(cleanedText);
	}

	private static void HealthOnlyCheck(ArmorStandEntity armorStand) {
		//todo setting for this
		if (!SkyblockerConfigManager.get().uiAndVisuals.healthBars.applyToHealthOnlyMobs	 || armorStand.getCustomName() == null) {
			return;
		}
		Matcher healthOnlyMatcher = HEALTH_ONLY_PATTERN.matcher(armorStand.getCustomName().getString());
		//if not found return
		if (!healthOnlyMatcher.find()) {
			return;
		}

		//get the current health of the mob
		int currentHealth = Integer.parseInt(healthOnlyMatcher.group(1).replace(",", ""));

		//if it's a new health only armor stand add to starting health lookup (not always full health if already damaged but best that can be done)
		if (!mobStartingHealth.containsKey(armorStand)) {
			mobStartingHealth.put(armorStand,currentHealth);
		}

		//add to health bar values
		float health = (float) currentHealth / mobStartingHealth.getInt(armorStand);
		healthValues.put(armorStand, health);

		//if enabled remove from name
		if (!SkyblockerConfigManager.get().uiAndVisuals.healthBars.removeHealthFromName) {
			return;
		}
		MutableText cleanedText = Text.empty();
		List<Text> parts = armorStand.getCustomName().getSiblings();
		for (int i = 0; i < parts.size(); i++) {
			//remove value from name
			if (i < parts.size() - 1 && parts.get(i).getString().equals(healthOnlyMatcher.group(1)) && parts.get(i + 1).getString().equals("❤")) {
				continue;
			}

			//remove "❤"
			if (parts.get(i).getString().equals("❤")) {
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
		Color barColor = SkyblockerConfigManager.get().uiAndVisuals.healthBars.barColor;
		boolean hideFullHealth = SkyblockerConfigManager.get().uiAndVisuals.healthBars.hideFullHealth;
		float scale = SkyblockerConfigManager.get().uiAndVisuals.healthBars.scale;
		for (Object2FloatMap.Entry<ArmorStandEntity> healthValue : healthValues.object2FloatEntrySet()) {
			//if the health bar is full and the setting is enabled to hide it stop rendering it
			if (hideFullHealth && healthValue.getFloatValue() == 1) {
				continue;
			}

			ArmorStandEntity armorStand = healthValue.getKey();
			// Render the health bar texture with scaling based on health percentage
			RenderHelper.renderTextureQuad(context, armorStand.getCameraPosVec(context.tickCounter().getTickDelta(false)).add(0, 0.25 - (0.1f * scale), 0), scale, 0.1f * scale, 1f, 1f, new Vec3d(-0.5f * scale, 0, 0), HEALTH_BAR_BACKGROUND_TEXTURE, barColor, false);
			RenderHelper.renderTextureQuad(context, armorStand.getCameraPosVec(context.tickCounter().getTickDelta(false)).add(0, 0.25 - (0.1f * scale), 0), healthValue.getFloatValue() * scale, 0.1f * scale, healthValue.getFloatValue(), 1f, new Vec3d(-0.5f * scale, 0, 0.003f), HEALTH_BAR_TEXTURE, barColor, false);
		}
	}
}
