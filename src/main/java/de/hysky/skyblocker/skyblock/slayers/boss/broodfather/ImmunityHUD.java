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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ImmunityHUD {

	private static final Pattern EGG_REGEX = Pattern.compile("(\\d+)s (\\d)/3");
	private static final Pattern EGG_HITS_REGEX = Pattern.compile("(\\d)/3");
	private static final Text IMMUNITY_INDICATOR_TEXT = Text.literal("IMMUNE").formatted(Formatting.WHITE)
				.formatted(Formatting.BOLD);
	private static Map<ArmorStandEntity, String> eggMap = new HashMap<>();
	private static final int EGG_TO_BOSS_MAX_DISTANCE = 15;
	private static int maxAmountOfEggs;

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(ImmunityHUD::render);
	}


	public static void handleEgg(ArmorStandEntity entity) {
		Matcher matcher = EGG_REGEX.matcher(entity.getName().getString());
		if (!matcher.matches()) return;
		if (matcher.group(1).equals("0")){
			eggMap.clear();
			return;
		}
		if (matcher.group(2).equals("3")){
			addEgg(entity);
			return;
		}
		hitEgg(entity, matcher);
	}

	private static void addEgg(ArmorStandEntity entity) {
		if (eggMap.size() == maxAmountOfEggs) return;
		Entity boss = SlayerManager.getSlayerBoss();
		if (boss == null || entity.getPos().distanceTo(boss.getPos()) > EGG_TO_BOSS_MAX_DISTANCE) return;
		eggMap.put(entity, entity.getName().getString());

	}

	private static boolean checkHitValidity(Matcher matcher, ArmorStandEntity entity) {
		 String hp = eggMap.get(entity);
		 return matcher.group(2).equals(hp);
	}

	private static void hitEgg(ArmorStandEntity entity, Matcher matcher){
		if (!eggMap.containsKey(entity)) return;
		if (checkHitValidity(matcher, entity)) return;
		eggMap.put(entity, eggMap.get(entity) - 1);
		if (eggMap.get(entity).equals("0")) {
			eggMap.remove(entity);
		}
	}
	private static void render(WorldRenderContext ctx){
		if (!SlayerManager.isInSlayerType(SlayerType.TARANTULA)) return;
		Entity boss = SlayerManager.getSlayerBoss();
		if (boss == null) return;
		if (SlayerManager.getSlayerTier() == null) return;
		maxAmountOfEggs = SlayerManager.getSlayerTier().name.equals("V") ? 3 : 2;
		if (eggMap.isEmpty()) return;
		RenderHelper.renderText(ctx, IMMUNITY_INDICATOR_TEXT, boss.getPos().add(0, 3, 0), 2f, true);
		AtomicInteger i = new AtomicInteger();
		eggMap.forEach((entity, integer) -> {
			System.out.printf(integer.toString());
			RenderHelper.renderText(ctx,
					Text.literal("HITS : " + integer + " / " + 3)
							.formatted(Formatting.WHITE)
							.formatted(Formatting.BOLD),
					boss.getPos().add(0, 3.4 + (i.getAndIncrement() * 0.4), 0), 2f, true);

		});
		System.out.println("immune");

	}



}
