package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.text.Style;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NucleusWaypoints {
	private static final Logger LOGGER = LoggerFactory.getLogger(NucleusWaypoints.class);

	private static class Waypoint {
		BlockPos position;
		String name;
		DyeColor color;

		Waypoint(BlockPos position, String name, DyeColor color) {
			this.position = position;
			this.name = name;
			this.color = color;
		}
	}

	private static final List<Waypoint> WAYPOINTS = List.of(
			new Waypoint(new BlockPos(551, 116, 551), "Precursor Remnants", DyeColor.LIGHT_BLUE),
			new Waypoint(new BlockPos(551, 116, 475), "Mithril Deposits", DyeColor.LIME),
			new Waypoint(new BlockPos(475, 116, 551), "Goblin Holdout", DyeColor.ORANGE),
			new Waypoint(new BlockPos(475, 116, 475), "Jungle", DyeColor.PURPLE),
			new Waypoint(new BlockPos(513, 106, 524), "Nucleus", DyeColor.RED)
	);

	public static void render(WorldRenderContext context) {
		try {
			boolean enabled = SkyblockerConfigManager.get().mining.crystalHollows.nucleusWaypoints;
			boolean inCrystalHollows = Utils.isInCrystalHollows();

			if (enabled && inCrystalHollows) {
				for (Waypoint waypoint : WAYPOINTS) {

					int rgb = waypoint.color.getFireworkColor();
					TextColor textColor = TextColor.fromRgb(rgb);

					MutableText text = Text.literal(waypoint.name).setStyle(Style.EMPTY.withColor(textColor));

					RenderHelper.renderText(context, text, waypoint.position.toCenterPos().add(0, 5, 0), 8, true);
				}
			}
		} catch (Exception e) {
			LOGGER.error("[{}] Error occurred while rendering Nucleus waypoints. {}", LOGGER.getName(), e);
		}
	}
}
