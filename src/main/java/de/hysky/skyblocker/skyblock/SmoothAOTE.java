package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.DungeonBoss;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.Perspective;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmoothAOTE {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final Pattern MANA_LORE = Pattern.compile("Mana Cost: (\\d+)");
    private static final long MAX_TELEPORT_TIME = 2500; //2.5 seconds

    private static long startTime;
    private static Vec3d startPos;
    private static Vec3d cameraStartPos;
    private static Vec3d teleportVector;
    private static long lastPing;
    private static int teleportsAhead;
    private static long lastTeleportTime;
    private static boolean teleportDisabled;

    @Init
    public static void init() {
        UseItemCallback.EVENT.register(SmoothAOTE::onItemInteract);
        UseBlockCallback.EVENT.register(SmoothAOTE::onBlockInteract);
    }

    public static void playerTeleported() {
        //the player has been teleported so 1 less teleport ahead
        teleportsAhead = Math.max(0, teleportsAhead - 1);
        //re-enable the animation if the player is teleported as this means they can teleport again. and reset timer for last teleport update
        lastTeleportTime = System.currentTimeMillis();
        teleportDisabled = false;

        //if the server is in sync in number of teleports
        if (teleportsAhead == 0) {
            //reset when player has reached the end of the teleports
            startPos = null;
            teleportVector = null;
        }
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


    private static TypedActionResult<ItemStack> onItemInteract(PlayerEntity playerEntity, World world, Hand hand) {
        if (CLIENT.player == null) {
            return null;
        }
        calculateTeleportUse(hand);
        return TypedActionResult.pass(CLIENT.player.getStackInHand(hand));
    }

    private static ActionResult onBlockInteract(PlayerEntity playerEntity, World world, Hand hand, BlockHitResult blockHitResult) {
        calculateTeleportUse(hand);
        return ActionResult.PASS;
    }

    /**
     * Finds if a player uses a teleport and then saves the start position and time. then works out final position and saves that too
     *
     * @param hand what the player is holding
     */

    private static void calculateTeleportUse(Hand hand) {
        //stop checking if player does not exist
        if (CLIENT.player == null || CLIENT.world == null) {
            return;
        }
        //get return item
        ItemStack stack = CLIENT.player.getStackInHand(hand);

        //make sure it's not disabled
        if (teleportDisabled) {
            return;
        }

        // make sure the camera is not in 3rd person
        if (CLIENT.options.getPerspective() != Perspective.FIRST_PERSON) {
            return;
        }

        //make sure the player is in an area teleporting is allowed not allowed in glacite mineshafts and floor 7 boss
        if (!isAllowedLocation()) {
            return;
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
                return;

            }
            case "ASPECT_OF_THE_LEECH_2" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWeirdTransmission) {
                    distance = 4;
                    break;
                }
                return;
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
                return;
            }
            case "ETHERWARP_CONDUIT" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableEtherTransmission) {
                    distance = extractTunedCustomData(customData, 57);
                    break;
                }
                return;
            }
            case "SINSEEKER_SCYTHE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableSinrecallTransmission) {
                    distance = extractTunedCustomData(customData, 4);
                    break;
                }
                return;
            }
            case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
                if (SkyblockerConfigManager.get().uiAndVisuals.smoothAOTE.enableWitherImpact) {
                    distance = 10;
                    break;
                }
                return;
            }
            default -> {
                return;
            }
        }
        //make sure the player has enough mana to do the teleport
        Matcher manaNeeded = ItemUtils.getLoreLineIfMatch(heldItem, MANA_LORE);
        if (manaNeeded != null && manaNeeded.matches()) {
            if (SkyblockerMod.getInstance().statusBarTracker.getMana().value() < Integer.parseInt(manaNeeded.group(1))) { // todo the players mana can lag behind as it is updated server side. client side mana calculations would help with this
                return;
            }
        }

        //work out start pos of warp and set start time. if there is an active warp going on make the end of that the start of the next one
        if (teleportsAhead == 0 || startPos == null || teleportVector == null) {
            //start of teleport sequence
            startPos = CLIENT.player.getPos().add(0, 1.62, 0); // the eye poss should not be affected by crouching
            cameraStartPos = CLIENT.player.getEyePos();
            lastTeleportTime = System.currentTimeMillis();
        } else {
            //add to the end of the teleport sequence
            startPos = startPos.add(teleportVector);
            //set the camera start pos to how far though the teleport the player is to make is smoother
            cameraStartPos = getInterpolatedPos();
        }

        startTime = System.currentTimeMillis();


        // calculate the vector the player will follow for the teleport
        //get direction
        float pitch = CLIENT.player.getPitch();
        float yaw = CLIENT.player.getYaw();
        Vec3d look = CLIENT.player.getRotationVector(pitch, yaw);

        //find target location depending on how far the item they are using takes them
        teleportVector = raycast(distance, look, startPos);
        if (teleportVector == null) {
            startPos = null;
            return;
        }
        //round the vector values to 1dp

        //compensate for hypixel rounding the end position to x.5 y.62 z.5
        Vec3d predictedEnd = startPos.add(teleportVector);
        Vec3d offsetVec = new Vec3d(predictedEnd.x - roundToCenter(predictedEnd.x), predictedEnd.y - (Math.ceil(predictedEnd.y) + 0.62), predictedEnd.z - roundToCenter(predictedEnd.z));
        teleportVector = teleportVector.subtract(offsetVec);
        //add 1 to teleports ahead
        teleportsAhead += 1;
    }

    private static double roundToCenter(double input) {
        return Math.round(input - 0.5) + 0.5;
    }

    /**
     * Works out if the players location lets them use teleportation or not
     *
     * @return if the player should be allowed to teleport
     */
    private static boolean isAllowedLocation() {
        //check mines shafts
        if (Utils.getMap().equals("Mineshaft")) {
            return false;
        } else if (Utils.getIslandArea().equals("⏣ Jungle Temple")) { //do not allow in jungle temple
            return false;
        } else if (Utils.getLocation() == Location.PRIVATE_ISLAND && !Utils.getIslandArea().equals("⏣ Your Island")) { //do not allow it when visiting
            return false;
        } else if (Utils.isInDungeons()) { //check places in dungeons where you can't teleport
            if (DungeonManager.isInBoss() && DungeonManager.getBoss() == DungeonBoss.MAXOR) {
                return false;
            }
            //make sure the player is in a room then check for disallowed rooms
            if (!DungeonManager.isCurrentRoomMatched()) {
                return true;
            }
            //does not work in boulder room
            if (DungeonManager.getCurrentRoom().getName().equals("boxes-room")) {
                return false;
            }
            //does not work in teleport maze room
            if (DungeonManager.getCurrentRoom().getName().equals("teleport-pad-room")) {
                return false;
            }
            //does not work in trap room
            if (DungeonManager.getCurrentRoom().getName().startsWith("trap")) {
                return false;
            }
        }

        return true;
    }

    /**
     * custom raycast hopefully more like hypxiels checks the player can be at every block of the raycast then when one is hit set pos to block before
     *
     * @param distance maximum distance
     * @return teleport vector
     */
    private static Vec3d raycast(int distance, Vec3d direction, Vec3d startPos) {
        if (CLIENT.world == null) {
            return null;
        }
        for (double offset = 0; offset <= distance; offset ++) {
            BlockPos checkPos = BlockPos.ofFloored(startPos.add(direction.multiply(offset)));

            //there are block in the way return the last location
            if (!CLIENT.world.getBlockState(checkPos).isAir() || !CLIENT.world.getBlockState(checkPos.up()).isAir()) { //todo some transparent blocks can be teleported in (Buttons could be more)
                if (offset == 0) {
                    // no teleport can happen
                    return null;
                }
                return direction.multiply(offset - 1);
            }

        }
        return direction.multiply(distance);
    }

    /**
     * works out where they player should be based on how far though the predicted teleport time.
     *
     * @return the camera position for the interpolated pos
     */

    public static Vec3d getInterpolatedPos() {
        if (CLIENT.player == null || teleportVector == null || startPos == null || teleportDisabled) {
            return null;
        }
        long gap = System.currentTimeMillis() - startTime;
        //make sure the player is actually getting teleported if not disable teleporting until they are teleported again
        if (System.currentTimeMillis() - lastTeleportTime > 1000) {
            teleportDisabled = true;
            startPos = null;
            teleportVector = null;
            teleportsAhead = 0;
            return null;
        }
        double percentage = Math.min((double) (gap) / Math.min(lastPing, MAX_TELEPORT_TIME), 1);

        return cameraStartPos.add(teleportVector.multiply(percentage));
    }

    public static void updatePing(long ping) {
        lastPing = ping;
    }


}
