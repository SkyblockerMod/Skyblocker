package de.hysky.skyblocker.skyblock;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class SmoothAOTE {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final int smoothTime = 100; // todo i would like this to be able to be your ping value

    private static long startTime;
    private static Vec3d startPos;
    private static Vec3d teleportVector;

    public static void init() {
        UseItemCallback.EVENT.register(SmoothAOTE::onItemInteract);
    }

    public static void reset() {
        //reset when player has reached the end of the teleport
        startPos = null;
        teleportVector = null;
    }


    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity playerEntity, World world, Hand hand) {
        //todo add manna check
        if (CLIENT.player == null) {
            return null;
        }
        ItemStack stack = CLIENT.player.getStackInHand(hand);
        //make sure the user is in the crystal hollows and holding the wishing compass
        if (!stack.getSkyblockId().equals("ASPECT_OF_THE_END") && !stack.getSkyblockId().equals("ASPECT_OF_THE_VOID")) {
            return TypedActionResult.pass(stack);
        }
        //work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
        if (startPos == null || teleportVector == null) {
            startPos = CLIENT.player.getEyePos();
        } else {
            startPos = startPos.add(teleportVector);
            System.out.println("this is doing somthing");
        }
        startTime = System.currentTimeMillis();

        // calculate the vector the player will follow for the teleport
        //get direction
        float pitch = CLIENT.player.getPitch();
        float yaw = CLIENT.player.getYaw();
        Vec3d look = CLIENT.player.getRotationVector(pitch, yaw);
        //find target location depending on how far the item they are using takes them
        teleportVector = look.multiply(12);
        //compensate for pixel rounding the end position to x.5 y.62 z.5
        Vec3d predictedEnd = startPos.add(teleportVector);
        Vec3d offset = new Vec3d(predictedEnd.x - (Math.floor(predictedEnd.x) + 0.5), predictedEnd.y - (Math.ceil(predictedEnd.y) + 0.62), predictedEnd.z - (Math.floor(predictedEnd.z) + 0.5));
        teleportVector = teleportVector.subtract(offset);

        return TypedActionResult.pass(stack);
    }

    public static Vec3d getInterpolatedPos() {
        double percentage = Math.min((double) (System.currentTimeMillis() - startTime) / smoothTime, 1);
        if (CLIENT.player == null || teleportVector == null || startPos == null) {
            return null;
        }

        return startPos.add(teleportVector.multiply(percentage));
    }
}
