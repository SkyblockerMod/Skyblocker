package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HealthBars {

	private static final Identifier HEALTH_BAR_BACKGROUND_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/purple_background.png");
	private static final Identifier HEALTH_BAR_TEXTURE = Identifier.ofVanilla("textures/gui/sprites/boss_bar/purple_progress.png");
	private static final Pattern HEALTH_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})*(\\.\\d+)?)/(\\d{1,3}(,\\d{3})*(\\.\\d+)?)❤");

	private static Object2FloatOpenHashMap<ArmorStandEntity> healthValues = new Object2FloatOpenHashMap<>();

	@Init
	public static void init() {
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
		WorldRenderEvents.AFTER_TRANSLUCENT.register(HealthBars::render);
		ClientEntityEvents.ENTITY_UNLOAD.register(HealthBars::onEntityDespawn);
	}

	private static void reset() {
		healthValues.clear();
	}

	/**
	 * remove dead armour stands from health bars
	 *
	 * @param entity dying entity
	 */
	public static void onEntityDespawn(Entity entity, ClientWorld clientWorld) {
		if (entity instanceof ArmorStandEntity armorStandEntity) {
			healthValues.removeFloat(armorStandEntity);
		}
	}

	public static void HeathBar(ArmorStandEntity armorStand) {
		//todo return based on enabled
		if (!armorStand.isInvisible() || !armorStand.hasCustomName() || !armorStand.isCustomNameVisible()) {
			return;
		}

		//check if armour stand is dead and remove it from list
		if (armorStand.isDead()) {
			healthValues.removeFloat(armorStand);
			return;
		}

		//check to see if the armour stand is a mob label with health

		Matcher healthMatcher = HEALTH_PATTERN.matcher(armorStand.getCustomName().getString());
		if (!healthMatcher.find()) {
			return;
		}

		//work out health value and save to hashMap
		System.out.println(healthMatcher.group(1));
		int firstValue = Integer.parseInt(healthMatcher.group(1).replace(",",""));
		int secondValue = Integer.parseInt(healthMatcher.group(4).replace(",",""));
		float health = (float) firstValue / secondValue;
		healthValues.put(armorStand, health);

		//edit armour stand name to remove health todo if enabled or only show total and not max
		MutableText cleanedText = Text.empty();
		List<Text> parts = armorStand.getCustomName().getSiblings();
		for (int i = 0; i < parts.size() - 3; i++) { //todo is health always at the end or do i need to add after that
			//found health remove stop adding
			if (parts.get(i).getString().equals(healthMatcher.group(1)) && parts.get(i + 1).getString().equals("/") && parts.get(i + 2).getString().equals(healthMatcher.group(4)) && parts.get(i + 3).getString().equals("❤")) {
				break;
			}
			cleanedText.append(parts.get(i));
		}
		armorStand.setCustomName(cleanedText);

	}

	/**
	 * Loops though armor stands with health bars and renders a bar for each of them just bellow
	 *
	 * @param context render context
	 */
	private static void render(WorldRenderContext context) {

		for (Object2FloatMap.Entry<ArmorStandEntity> healthValue : healthValues.object2FloatEntrySet()) {
			ArmorStandEntity armorStand = healthValue.getKey();

			// Render the health bar texture with scaling based on health percentage
			RenderHelper.renderTextureQuad(context, armorStand.getCameraPosVec(context.tickCounter().getTickDelta(false)).add(0, 0.15, 0), 1f, 0.1f, 1f, 1f, new Vec3d(-0.5f, 0, 0), HEALTH_BAR_BACKGROUND_TEXTURE, false);
			RenderHelper.renderTextureQuad(context, armorStand.getCameraPosVec(context.tickCounter().getTickDelta(false)).add(0, 0.15, 0), healthValue.getFloatValue(), 0.1f, healthValue.getFloatValue(), 1f, new Vec3d(-0.5f, 0, 0.003f), HEALTH_BAR_TEXTURE, false);

		}

	}

}
