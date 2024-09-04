package de.hysky.skyblocker.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class LocationUtils {

    /**
     * Checks if a given location is within a specified radius of the client's location.
     *
     * @param targetLocation The location to check.
     * @param radius The radius within which to check.
     * @return true if the target location is within the specified radius, false otherwise.
     */
    public static boolean isWithinRadius(BlockPos targetLocation, double radius) {
        // Get the player's current position
        MinecraftClient client = MinecraftClient.getInstance();
        BlockPos playerPos = client.player.getBlockPos();

        // Calculate the squared distance between the player's location and the target location
        double distanceSquared = playerPos.getSquaredDistance(targetLocation);

        // Compare the squared distance with the squared radius to avoid using square root (more efficient)
        return distanceSquared <= radius * radius;
    }

}
