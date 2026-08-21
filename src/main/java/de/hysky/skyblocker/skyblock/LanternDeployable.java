package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class LanternDeployable implements Renderable {

	public static final LanternDeployable INSTANCE = new LanternDeployable();

	private static final float RANGE = 30f;
	private static final int SEGMENTS = 128;
	private static final int COLOR = 0x5500FFFF;
	private static final float CIRCLE_HEIGHT = 3f;

	private final List<Vec3> lanternPositions = new ArrayList<>();

	@Init
	public static void init() {

		LevelRenderExtractionCallback.EVENT.register(INSTANCE::extractRendering);
		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client.level == null || client.player == null) {
				INSTANCE.lanternPositions.clear();
				return;
			}
			INSTANCE.findLantern();
		});
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {
		if (lanternPositions.isEmpty()) return;

		if (!SkyblockerConfigManager.get().mining.lanternDeployable.showDeployableRange) return;

		for (Vec3 position : lanternPositions) {

			Vec3 circlePosition = position.add(0, CIRCLE_HEIGHT, 0);

			collector.submitCylinder(circlePosition, RANGE, 0.5f, SEGMENTS, COLOR);
		}
	}

	private void findLantern() {
		Minecraft mc = Minecraft.getInstance();
		lanternPositions.clear();

		for (Entity entity : mc.level.entitiesForRendering()) {
			if (!(entity instanceof ArmorStand armorStand)) continue;

			if (isLantern(armorStand)) {
				lanternPositions.add(Vec3.atCenterOf(armorStand.blockPosition().below()));
			}
		}
	}

	private boolean isLantern(ArmorStand armorStand) {
		if (!armorStand.hasCustomName()) return false;

		String name = armorStand.getName().getString().replaceAll("§.", "").toLowerCase(Locale.ENGLISH);

		return name.contains("lantern") || name.contains("wisp");
	}
}
