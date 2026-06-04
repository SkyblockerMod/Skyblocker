package de.hysky.skyblocker.skyblock.crimson.dojo;

import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import java.awt.Color;
import java.text.DecimalFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.phys.Vec3;

public class ForceTestHelper {

	private static final DecimalFormat FORMATTER = new DecimalFormat("0.0");
	private static final int ZOMBIE_LIFE_TIME = 10100;

	private static final Object2LongOpenHashMap<Zombie> zombies = new Object2LongOpenHashMap<>();

	protected static void reset() {
		zombies.clear();
	}

	/**
	 * If a zombie value is negative make it glow
	 *
	 * @param name zombies value
	 * @return if the zombie should glow
	 */
	protected static boolean shouldGlow(String name) {
		return name.contains("-");
	}

	protected static int getColor() {
		return Color.RED.getRGB();
	}

	protected static void onEntitySpawn(Entity entity) {
		if (entity instanceof Zombie zombie) {
			zombies.put(zombie, System.currentTimeMillis() + ZOMBIE_LIFE_TIME);
		}
	}

	protected static void onEntityAttacked(Entity entity) {
		if (entity instanceof Zombie zombie) {
			if (zombies.containsKey(zombie)) {
				zombies.put(zombie, System.currentTimeMillis() + ZOMBIE_LIFE_TIME); //timer is reset when they are hit
			}
		}
	}

	protected static void onEntityDespawn(Entity entity) {
		if (entity instanceof Zombie zombie) {
			zombies.removeLong(zombie);
		}
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		//render times
		long currentTime = System.currentTimeMillis();
		for (Object2LongMap.Entry<Zombie> zombie : zombies.object2LongEntrySet()) {
			float secondsTime = Math.max((zombie.getLongValue() - currentTime) / 1000f, 0);

			MutableComponent text = Component.literal(FORMATTER.format(secondsTime));
			if (secondsTime > 1) {
				text = text.withStyle(ChatFormatting.GREEN);
			} else if (secondsTime > 0) {
				text = text.withStyle(ChatFormatting.YELLOW);
			} else {
				text = text.withStyle(ChatFormatting.RED);
			}

			Vec3 labelPos = zombie.getKey().getEyePosition(RenderHelper.getTickCounter().getGameTimeDeltaPartialTick(false));
			collector.submitText(text, labelPos, 1.5f, true);
		}
	}
}
