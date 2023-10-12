package de.hysky.skyblocker.utils;

import net.minecraft.util.math.BlockPos;

public final class PosUtils {
    public static BlockPos parsePosString(String posData) {
        String[] posArray = posData.split(",");
        return new BlockPos(Integer.parseInt(posArray[0]), Integer.parseInt(posArray[1]), Integer.parseInt(posArray[2]));
    }

    public static String getPosString(BlockPos blockPos) {
        return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }
}
