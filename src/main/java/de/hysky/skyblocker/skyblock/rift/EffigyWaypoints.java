package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Area;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.DyeColor;

public class EffigyWaypoints {
	private static final Logger LOGGER = LoggerFactory.getLogger(EffigyWaypoints.class);
	private static final List<BlockPos> EFFIGIES = List.of(
			new BlockPos(150, 79, 95), //Effigy 1
			new BlockPos(193, 93, 119), //Effigy 2
			new BlockPos(235, 110, 147), //Effigy 3
			new BlockPos(293, 96, 134), //Effigy 4
			new BlockPos(262, 99, 94), //Effigy 5
			new BlockPos(240, 129, 118) //Effigy 6
	);
	private static final List<BlockPos> UNBROKEN_EFFIGIES = new ArrayList<>();
	private static final float[] RED = ColorUtils.getFloatComponents(DyeColor.RED);

	protected static void updateEffigies() {
		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableEffigyWaypoints || !Utils.isOnSkyblock() || !Utils.isInTheRift() || Utils.getArea() != Area.CHATEAU) return;

		UNBROKEN_EFFIGIES.clear();

		try {
			for (int i = 0; i < Utils.STRING_SCOREBOARD.size(); i++) {
				String line = Utils.STRING_SCOREBOARD.get(i);

				if (line.contains("Effigies")) {
					List<Component> effigiesText = new ArrayList<>();
					List<Component> prefixAndSuffix = Utils.TEXT_SCOREBOARD.get(i).getSiblings();

					//Add contents of prefix and suffix to list
					effigiesText.addAll(prefixAndSuffix.getFirst().getSiblings());
					effigiesText.addAll(prefixAndSuffix.get(1).getSiblings());

					for (int i2 = 1; i2 < effigiesText.size(); i2++) {
						if (effigiesText.get(i2).getStyle().getColor().equals(TextColor.fromLegacyFormat(ChatFormatting.GRAY))) UNBROKEN_EFFIGIES.add(EFFIGIES.get(i2 - 1));
					}
				}
			}
		} catch (NullPointerException e) {
			LOGGER.error("[Skyblocker] Error while updating effigies.", e);
		}
	}

	protected static void extractRendering(PrimitiveCollector collector) {
		if (SkyblockerConfigManager.get().slayers.vampireSlayer.enableEffigyWaypoints && Utils.getArea() == Area.CHATEAU) {
			for (BlockPos effigy : UNBROKEN_EFFIGIES) {
				if (SkyblockerConfigManager.get().slayers.vampireSlayer.compactEffigyWaypoints) {
					collector.submitFilledBoxWithBeaconBeam(effigy.below(6), RED, 0.5F, true);
				} else {
					collector.submitFilledBoxWithBeaconBeam(effigy, RED, 0.5F, true);
					for (int i = 1; i < 6; i++) {
						collector.submitFilledBox(effigy.below(i), RED, 0.5F - (0.075F * i), true);
					}
				}
			}
		}
	}
}
