package de.hysky.skyblocker.skyblock.entity;

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

		if (Utils.isInDungeons() && FrustumUtils.isVisible(box) && !entity.isInvisible()) {
			String name = entity.getName().getString();

			return switch (entity) {
				case PlayerEntity _p when name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy") -> SkyblockerConfigManager.get().locations.dungeons.starredMobBoundingBoxes;
				case ArmorStandEntity _armorStand -> false;

				// Regular Mobs
				default -> SkyblockerConfigManager.get().locations.dungeons.starredMobBoundingBoxes && MobGlow.isStarred(entity);
			};
		}

		return false;
	}
	
	public static float[] getBoxColor(Entity entity) {
		int color = MobGlow.getGlowColor(entity);

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
