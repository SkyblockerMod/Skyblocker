package de.hysky.skyblocker.skyblock.slayers.boss.broodfather;
import com.google.common.collect.Maps;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.entity.passive.TurtleEntity;
import java.util.Map;
import java.util.UUID;

public class EggHighlighter {
	private static Map<UUID, TurtleEntity> turtleEntities = Maps.newHashMap();
	private static final float[] RED = {1f, 1f, 1f};

	@Init
	public static void init() {
		WorldRenderEvents.AFTER_TRANSLUCENT.register(EggHighlighter::render);
	}

	public static void addToMap(TurtleEntity turtleEntity) {
		turtleEntities.put(turtleEntity.getUuid(), turtleEntity);
	}

	private static void render(WorldRenderContext ctx){
		for (Map.Entry<UUID, TurtleEntity> entry : turtleEntities.entrySet()) {
			RenderHelper.renderOutline(ctx, entry.getValue().getBoundingBox(), RED, 5f, true);
		}
	}
}
