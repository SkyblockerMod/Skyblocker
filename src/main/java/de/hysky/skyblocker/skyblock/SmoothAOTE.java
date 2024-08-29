package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothAOTE {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final Pattern MANA_LORE = Pattern.compile("Mana Cost: (\\d+)");
    private static final long MAX_TELEPORT_TIME = 1000;

    private static long startTime;
    private static Vec3d startPos;
    private static Vec3d teleportVector;
    private static long lastPing;

    public static void init() {
        UseItemCallback.EVENT.register(SmoothAOTE::onItemInteract);
    }

    public static void reset() {
        //reset when player has reached the end of the teleport
        startPos = null;
        teleportVector = null;
    }

    /**
     * checks to see if a teleport device is using transmission tuner to increase the range
     *
     * @param customData the custom data of the teleport device
     * @param baseRange  the base range for the device without tuner
     * @return the range with tuner
     */
    private static int extractTunedCustomData(NbtCompound customData, int baseRange) {
        return customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getInt("tuned_transmission") : baseRange;
    }

    /**
     * Finds if a player uses a teleport and then saves the start position and time. then works out final position and saves that too
     *
     * @param playerEntity the player
     * @param world        the world
     * @param hand         what the player is holding
     * @return if the right click should go though
     */

    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity playerEntity, World world, Hand hand) {
        //stop checking if player does not exist
        if (CLIENT.player == null || CLIENT.world == null) {
            return null;
        }
        //get return item
        ItemStack stack = CLIENT.player.getStackInHand(hand);

        // make sure the camera is not in 3rd person
        if (CLIENT.options.getPerspective() != Perspective.FIRST_PERSON) {
            return TypedActionResult.pass(stack);
        }

        //make sure the player is in an area teleporting is allowed not allowed in glacite mineshafts and floor 7 boss
        if (Utils.getMap().equals("Glacite Mineshafts") || (Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR)) {
            return TypedActionResult.pass(stack);
        }

        //work out if the player is holding a teleporting item that is enabled and if so how far the item will take them
        ItemStack heldItem = CLIENT.player.getMainHandStack();
        String itemId = heldItem.getSkyblockId();
        NbtCompound customData = ItemUtils.getCustomData(heldItem);

        int distance;
        switch (itemId) {
            case "ASPECT_OF_THE_LEECH_1" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
                    distance = 3;
                    break;
                }
                return TypedActionResult.pass(stack);

            }
            case "ASPECT_OF_THE_LEECH_2" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
                    distance = 4;
                    break;
                }
                return TypedActionResult.pass(stack);
            }
            case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
                if (CLIENT.options.sneakKey.isPressed() && customData.getInt("ethermerge") == 1) {
                    if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
                        distance = extractTunedCustomData(customData, 57);
                        break;
                    }
                } else if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableInstantTransmission) {
                    distance = extractTunedCustomData(customData, 8);
                    break;
                }
                return TypedActionResult.pass(stack);
            }
            case "ETHERWARP_CONDUIT" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
                    distance = extractTunedCustomData(customData, 57);
                    break;
                }
                return TypedActionResult.pass(stack);
            }
            case "SINSEEKER_SCYTHE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableSinrecallTransmission) {
                    distance = extractTunedCustomData(customData, 4);
                    break;
                }
                return TypedActionResult.pass(stack);
            }
            case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWitherImpact) {
                    distance = 10;
                    break;
                }
                return TypedActionResult.pass(stack);
            }
            default -> {
                return TypedActionResult.pass(stack);
            }
        }
        //make sure the player has enough mana to do the teleport
        Matcher manaNeeded = ItemUtils.getLoreLineIfMatch(heldItem, MANA_LORE);
        if (manaNeeded != null && manaNeeded.matches()) {
            if (SkyblockerMod.getInstance().statusBarTracker.getMana().value() < Integer.parseInt(manaNeeded.group(1))) { // todo the players mana can lag behind as it is updated server side. client side mana calculations would help with this
                return TypedActionResult.pass(stack);
            }
        }

        //work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
        if (startPos == null || teleportVector == null) {
            startPos = CLIENT.player.getEyePos();
        } else {
            startPos = startPos.add(teleportVector);
        }
        startTime = System.currentTimeMillis();

        // calculate the vector the player will follow for the teleport
        //get direction
        float pitch = CLIENT.player.getPitch();
        float yaw = CLIENT.player.getYaw();
        Vec3d look = CLIENT.player.getRotationVector(pitch, yaw);
        //find target location depending on how far the item they are using takes them
        teleportVector = look.multiply(distance);
        //make sure there are no blocks in the way and if so account for this
        BlockHitResult hitResult = world.raycast(new RaycastContext(startPos, startPos.add(teleportVector), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, CLIENT.player));
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
            Vec3d offsetEndPos;
            if (hitResult.getSide().equals(Direction.UP) || hitResult.getSide().equals(Direction.DOWN)) {
                offsetEndPos = hitResult.getPos().offset(hitResult.getSide(), 1);
            } else {
                offsetEndPos = hitResult.getPos().offset(hitResult.getSide(), 0.5);
            }
            teleportVector = offsetEndPos.subtract(startPos);
        }
        //compensate for pixel rounding the end position to x.5 y.62 z.5
        Vec3d predictedEnd = startPos.add(teleportVector);
        Vec3d offsetVec = new Vec3d(predictedEnd.x - (Math.floor(predictedEnd.x) + 0.5), predictedEnd.y - (Math.ceil(predictedEnd.y) + 0.62), predictedEnd.z - (Math.floor(predictedEnd.z) + 0.5));
        teleportVector = teleportVector.subtract(offsetVec);

        return TypedActionResult.pass(stack);
    }

    /**
     * works out where they player should be based on how far though the predicted teleport time.
     *
     * @return the camera position for the interpolated pos
     */

    public static Vec3d getInterpolatedPos() {
        if (CLIENT.player == null || teleportVector == null || startPos == null) {
            return null;
        }
        long gap = System.currentTimeMillis() - startTime;
        //if teleport has taken over max time reset and return null
        if (gap > MAX_TELEPORT_TIME) {
            reset();
            return null;
        }
        double percentage = Math.min((double) (gap) / Math.min(lastPing, MAX_TELEPORT_TIME), 1);

        return startPos.add(teleportVector.multiply(percentage));
    }

    public static void updatePing(long ping) {
        lastPing = ping;
    }
}
