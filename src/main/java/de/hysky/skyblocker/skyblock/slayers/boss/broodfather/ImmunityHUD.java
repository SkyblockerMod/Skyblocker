package de.hysky.skyblocker.skyblock.slayers.boss.broodfather;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;

import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImmunityHUD {
	private static final Pattern EGG_REGEX = Pattern.compile("\\d+s \\d/\\d");
	private static boolean isImmune = false;
	private static final Text IMMUNITY_INDICATOR_TEXT = Text.literal("IMMUNE").formatted(Formatting.WHITE)
				.formatted(Formatting.BOLD);;
	private static Map<ArmorStandEntity, Integer> eggMap = new HashMap<>();
	private static final int EGG_TO_BOSS_MAX_DISTANCE = 15;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ImmunityHUD::render);
	}

	public static void addEgg(ArmorStandEntity entity) {
		if(eggMap.size() == 3) return;
		if(entity.getPos().distanceTo(SlayerManager.getSlayerBoss().getPos()) > EGG_TO_BOSS_MAX_DISTANCE) return;
		eggMap.put(entity, 3);
	}

	public static void hitEgg(ArmorStandEntity entity){
		Matcher matcher = EGG_REGEX.matcher(entity.getName().getString());
		if(!matcher.matches())return;
		if(!eggMap.containsKey(entity)) return;
		eggMap.put(entity, eggMap.get(entity) - 1);
		if(!eggMap.get(entity).equals(0)) {
			eggMap.remove(entity);
		}
	}

	private void setImmunity(boolean bool){
		isImmune = bool;
	}

	private static void render(WorldRenderContext ctx){
		if (!SlayerManager.isInSlayerType(SlayerType.TARANTULA) || !SlayerManager.isBossSpawned() || !isImmune) return;
		Entity boss = SlayerManager.getSlayerBoss();
		if (boss == null) return;
		RenderHelper.renderText(ctx, IMMUNITY_INDICATOR_TEXT, boss.getPos().add(0, 3, 0), 2f, true);
	}
}
