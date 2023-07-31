package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import net.minecraft.util.math.BlockPos;

public record SecretWaypoint(int secretIndex, Category category, BlockPos pos, boolean missing) {
    enum Category {
        ENTRANCE(0, 255, 0),
        SUPERBOOM(255, 0, 0),
        CHEST(2, 213, 250),
        ITEM(2, 64, 250),
        BAT(142, 66, 0),
        WITHER(30, 30, 30),
        LEVER(250, 217, 2),
        FAIRYSOUL(255, 85, 255),
        STONK(146, 52, 235),
        DEFAULT(190, 255, 252);
        final float[] colorComponents;

        Category(int... intColorComponents) {
            colorComponents = new float[intColorComponents.length];
            for (int i = 0; i < intColorComponents.length; i++) {
                colorComponents[i] = intColorComponents[i] / 255F;
            }
        }
    }
}
