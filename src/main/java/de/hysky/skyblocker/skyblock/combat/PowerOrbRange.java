package de.hysky.skyblocker.skyblock.combat;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.LevelRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.config.SkyblockerConfigManager;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public final class PowerOrbRange implements Renderable {

	public static final PowerOrbRange INSTANCE = new PowerOrbRange();

	private static final float RANGE = 18f;
	private static final float CIRCLE_HEIGHT = 2f;
	private static final int SEGMENTS = 128;
	private static final int COLOR = 0x5500FFFF;

	private Vec3 orbPosition;

	@Init
	public static void init() {

		LevelRenderExtractionCallback.EVENT.register(INSTANCE::extractRendering);


		ClientTickEvents.END_CLIENT_TICK.register(client -> {

			if (client.level == null || client.player == null) {

				INSTANCE.clearPosition();
				return;
			}


			INSTANCE.findOrb();
		});
	}

	private void updatePosition(Vec3 pos) {
		this.orbPosition = pos;
	}

	private void clearPosition() {
		this.orbPosition = null;
	}

	@Override
	public void extractRendering(PrimitiveCollector collector) {

		if (orbPosition == null) {
			return;
		}

		if (!SkyblockerConfigManager.get()
				.combat
				.powerOrbRange
				.showPowerOrbRange) {
			return;
		}

		collector.submitCylinder(
				orbPosition,
				RANGE,
				0.5f,
				SEGMENTS,
				COLOR
		);
	}

	private void findOrb() {

		Minecraft mc = Minecraft.getInstance();

		if (mc.level == null) return;


		for (Entity entity : mc.level.entitiesForRendering()) {

			if (!(entity instanceof ArmorStand armorStand)) continue;


			if (isOrb(armorStand)) {

				Vec3 pos = armorStand.position()
						.add(0, CIRCLE_HEIGHT, 0);

				updatePosition(pos);
				return;
			}
		}


		clearPosition();
	}

	private boolean isOrb(ArmorStand armorStand) {

		if (!armorStand.hasCustomName()) return false;


		String name = armorStand.getName()
				.getString()
				.replaceAll("§.", "")
				.toLowerCase(Locale.ENGLISH);


		return name.contains("radiant power orb")
				|| name.contains("mana flux")
				|| name.contains("manaflux")
				|| name.contains("overflux")
				|| name.contains("plasmaflux")
				|| name.contains("plasma flux");
	}
}
