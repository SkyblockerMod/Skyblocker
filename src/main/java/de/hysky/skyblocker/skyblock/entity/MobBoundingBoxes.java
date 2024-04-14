package de.hysky.skyblocker.skyblock.entity;

import java.util.List;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

public class MobBoundingBoxes {
	/**
	 * These boxes will be rendered before the debug render phase which happens after entities are rendered;
	 */
	private static final ObjectOpenHashSet<RenderableBox> BOXES_2_RENDER = new ObjectOpenHashSet<>();

	public static void init() {
		WorldRenderEvents.BEFORE_DEBUG_RENDER.register(MobBoundingBoxes::render);
	}

	public static boolean shouldDrawMobBoundingBox(Entity entity) {
		Box box = entity.getBoundingBox();

		if (Utils.isInDungeons() && !MobGlow.STARRED_MOB_GLOW && FrustumUtils.isVisible(box) && !entity.isInvisible()) {
			String name = entity.getName().getString();

			// Minibosses
			if (entity instanceof PlayerEntity) {
				switch (name) {
					case "Lost Adventurer", "Shadow Assassin", "Diamond Guy": return SkyblockerConfigManager.get().locations.dungeons.starredMobBoundingBoxes;
				}
			}

			// Regular Mobs
			if (!(entity instanceof ArmorStandEntity)) {
				List<ArmorStandEntity> armorStands = MobGlow.getArmorStands(entity);

				if (!armorStands.isEmpty() && armorStands.get(0).getName().getString().contains("✯"))
					return SkyblockerConfigManager.get().locations.dungeons.starredMobBoundingBoxes;
			}
		}

		return false;
	}
	
	public static float[] getBoxColor(Entity entity) {
		String name = entity.getName().getString();
		int color = 0;

		if (entity instanceof PlayerEntity) {
			color = switch (name) {
				case "Lost Adventurer" -> 0xfee15c;
				case "Shadow Assassin" -> 0x5b2cb2;
				case "Diamond Guy" -> 0x57c2f7;
				default -> 0xf57738;
			};
		} else {
			color = 0xf57738;
		}

		return new float[] { ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f };
	}

	public static void submitBox2BeRendered(Box box, float[] colorComponents) {
		BOXES_2_RENDER.add(new RenderableBox(box, colorComponents));
	}

	private static void render(WorldRenderContext context) {
		for (RenderableBox box : BOXES_2_RENDER) {
			box.render(context);
		}

		BOXES_2_RENDER.clear();
	}

	private record RenderableBox(Box box, float[] colorComponents) implements Renderable {

		@Override
		public void render(WorldRenderContext context) {
			RenderHelper.renderOutline(context, box, colorComponents, 6, false);
		}
	}
}
