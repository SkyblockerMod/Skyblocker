package de.hysky.skyblocker.utils;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.math.BlockPos;

public final class PosUtils {
    public static final Codec<BlockPos> ALT_BLOCK_POS_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("x").forGetter(BlockPos::getX),
            Codec.INT.fieldOf("y").forGetter(BlockPos::getY),
            Codec.INT.fieldOf("z").forGetter(BlockPos::getZ))
            .apply(instance, BlockPos::new));

    public static BlockPos parsePosString(String posData) {
        String[] posArray = posData.split(",");
        return new BlockPos(Integer.parseInt(posArray[0]), Integer.parseInt(posArray[1]), Integer.parseInt(posArray[2]));
    }

    public static BlockPos parsePosJson(JsonObject posJson) {
        return new BlockPos(posJson.get("x").getAsInt(), posJson.get("y").getAsInt(), posJson.get("z").getAsInt());
    }

    public static String getPosString(BlockPos blockPos) {
        return blockPos.getX() + "," + blockPos.getY() + "," + blockPos.getZ();
    }

    public static String toSpaceSeparatedString(BlockPos blockPos) {
        return blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
    }
}
