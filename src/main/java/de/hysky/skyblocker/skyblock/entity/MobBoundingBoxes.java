package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.glow.adder.DungeonGlowAdder;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

public class MobBoundingBoxes {
	/**
	 * These boxes will be rendered before the debug render phase which happens after entities are rendered;
	 */
	private static final ObjectOpenHashSet<RenderableBox> BOXES_2_RENDER = new ObjectOpenHashSet<>();

	@Init
	public static void init() {
		WorldRenderExtractionCallback.EVENT.register(MobBoundingBoxes::extractRendering);
	}

	public static boolean shouldDrawMobBoundingBox(Entity entity) {
		if (Utils.isInDungeons() && !entity.isInvisible()) {
			String name = entity.getName().getString();

			return switch (entity) {
				case Player _p when name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy") -> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes;
				case Player _p when entity.getId() == LividColor.getCorrectLividId() -> LividColor.shouldDrawBoundingBox(name);
				case ArmorStand _armorStand -> false;

				// Regular Mobs
				default -> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes && DungeonGlowAdder.isStarred(entity);
			};
		}

		return false;
	}

	public static float[] getBoxColor(Entity entity) {
		int color = MobGlow.getMobGlow(entity);
		return ColorUtils.getFloatComponents(color);
	}

	public static void submitBox2BeRendered(AABB box, float[] colorComponents) {
		BOXES_2_RENDER.add(new RenderableBox(box, colorComponents));
	}

	private static void extractRendering(PrimitiveCollector collector) {
		for (RenderableBox box : BOXES_2_RENDER) {
			box.extractRendering(collector);
		}

		BOXES_2_RENDER.clear();
	}

	private record RenderableBox(AABB box, float[] colorComponents) implements Renderable {

		@Override
		public void extractRendering(PrimitiveCollector collector) {
			collector.submitOutlinedBox(box, colorComponents, 6, false);
		}
	}
}
