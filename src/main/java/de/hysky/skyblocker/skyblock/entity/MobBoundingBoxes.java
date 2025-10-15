package de.hysky.skyblocker.skyblock.entity;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.SlayersConfig;
import de.hysky.skyblocker.skyblock.dungeon.LividColor;
import de.hysky.skyblocker.skyblock.entity.glow.adder.DungeonGlowAdder;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.FrustumUtils;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;

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
		Box box = entity.getBoundingBox();

		if (Utils.isInDungeons() && FrustumUtils.isVisible(box) && !entity.isInvisible()) {
			String name = entity.getName().getString();

			return switch (entity) {
				case PlayerEntity _p when name.equals("Lost Adventurer") || name.equals("Shadow Assassin") || name.equals("Diamond Guy") -> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes;
				case PlayerEntity p when entity.getId() == LividColor.getCorrectLividId() -> LividColor.shouldDrawBoundingBox(name);
				case ArmorStandEntity _armorStand -> false;

				// Regular Mobs
				default -> SkyblockerConfigManager.get().dungeons.starredMobBoundingBoxes && DungeonGlowAdder.isStarred(entity);
			};
		}

		if (SlayerManager.shouldGlow(entity, SlayersConfig.HighlightSlayerEntities.HITBOX)) {
			return true;
		}

		return false;
	}

	public static float[] getBoxColor(Entity entity) {
		int color = MobGlow.getMobGlow(entity);

		return new float[] { ((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, (color & 0xFF) / 255f };
	}

	public static void submitBox2BeRendered(Box box, float[] colorComponents) {
		BOXES_2_RENDER.add(new RenderableBox(box, colorComponents));
	}

	private static void extractRendering(PrimitiveCollector collector) {
		for (RenderableBox box : BOXES_2_RENDER) {
			box.extractRendering(collector);
		}

		BOXES_2_RENDER.clear();
	}

	private record RenderableBox(Box box, float[] colorComponents) implements Renderable {

		@Override
		public void extractRendering(PrimitiveCollector collector) {
			collector.submitOutlinedBox(box, colorComponents, 6, false);
		}
	}
}
