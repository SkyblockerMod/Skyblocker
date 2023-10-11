package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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

    protected static void updateEffigies() {
        if (!SkyblockerConfigManager.get().slayer.vampireSlayer.enableEffigyWaypoints || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getIslandArea().contains("Stillgore Château")) return;

        UNBROKEN_EFFIGIES.clear();
        
        try {
            for (int i = 0; i < Utils.STRING_SCOREBOARD.size(); i++) {
                String line = Utils.STRING_SCOREBOARD.get(i);
                
                if (line.contains("Effigies")) {
                    List<Text> effigiesText = new ArrayList<>();
                    List<Text> prefixAndSuffix = Utils.TEXT_SCOREBOARD.get(i).getSiblings();
                    
                    //Add contents of prefix and suffix to list
                    effigiesText.addAll(prefixAndSuffix.get(0).getSiblings());
                    effigiesText.addAll(prefixAndSuffix.get(1).getSiblings());

                    for (int i2 = 1; i2 < effigiesText.size(); i2++) {
                        if (effigiesText.get(i2).getStyle().getColor() == TextColor.parse("gray")) UNBROKEN_EFFIGIES.add(EFFIGIES.get(i2 - 1));
                    }
                }
            }
        } catch (NullPointerException e) {
            LOGGER.error("[Skyblocker] Error while updating effigies.", e);
        }
    }

    protected static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().slayer.vampireSlayer.enableEffigyWaypoints && Utils.getIslandArea().contains("Stillgore Château")) {
            for (BlockPos effigy : UNBROKEN_EFFIGIES) {
                float[] colorComponents = DyeColor.RED.getColorComponents();
                if (SkyblockerConfigManager.get().slayer.vampireSlayer.compactEffigyWaypoints) {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, effigy.down(6), colorComponents, 0.5F);
                } else {
                    RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, effigy, colorComponents, 0.5F);
                    for (int i = 1; i < 6; i++) {
                        RenderHelper.renderFilledThroughWalls(context, effigy.down(i), colorComponents, 0.5F - (0.075F * i));
                    }
                }
            }
        }
    }
}