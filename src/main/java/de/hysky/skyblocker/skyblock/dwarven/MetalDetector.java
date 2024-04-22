package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetalDetector {
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final float[] LIGHT_GRAY = { 192 / 255f, 192 / 255f, 192 / 255f };
    private static final Pattern TREASURE_PATTERN = Pattern.compile("(§3§lTREASURE: §b)(\\d+\\.?\\d?)m");
    private static final Pattern KEEPER_PATTERN = Pattern.compile("Keeper of (\\w+)");
    private static final HashMap<String, Vec3i> keeperOffsets = Util.make(new HashMap<>(), map -> {
        map.put("Diamond", new Vec3i(33, 0, 3));
        map.put("Lapis", new Vec3i(-33, 0, -3));
        map.put("Emerald", new Vec3i(-3, 0, 33));
        map.put("Gold", new Vec3i(3, 0, -33));
    });
    private static final HashSet<Vec3i> knownChestOffsets = Util.make(new HashSet<>(), set -> {
        set.add(new Vec3i(-38, -22, 26));  // -38, -22, 26
        set.add(new Vec3i(38, -22, -26)); // 38, -22, -26
        set.add(new Vec3i(-40, -22, 18)); // -40, -22, 18
        set.add(new Vec3i(-41, -20, 22)); // -41, -20, 22
        set.add(new Vec3i(-5, -21, 16)); // -5, -21, 16
        set.add(new Vec3i(40, -22, -30)); // 40, -22, -30
        set.add(new Vec3i(-42, -20, -28));  // -42, -20, -28
        set.add(new Vec3i(-43, -22, -40));  // -43, -22, -40
        set.add(new Vec3i(42, -19, -41)); // 42, -19, -41
        set.add(new Vec3i(43, -21, -16)); // 43, -21, -16
        set.add(new Vec3i(-1, -22, -20)); // -1, -22, -20
        set.add(new Vec3i(6, -21, 28));   // 6, -21, 28
        set.add(new Vec3i(7, -21, 11));   // 7, -21, 11
        set.add(new Vec3i(7, -21, 22));   // 7, -21, 22
        set.add(new Vec3i(-12, -21, -44));  // -12, -21, -44
        set.add(new Vec3i(12, -22, 31));   // 12, -22, 31
        set.add(new Vec3i(12, -22, -22));   // 12, -22, -22
        set.add(new Vec3i(12, -21, 7));   // 12, -21, 7
        set.add(new Vec3i(12, -21, -43));   // 12, -21, -43
        set.add(new Vec3i(-14, -21, 43));  // -14, -21, 43
        set.add(new Vec3i(-14, -21, 22));  // -14, -21, 22
        set.add(new Vec3i(-17, -21, 20));  // -17, -21, 20
        set.add(new Vec3i(-20, -22, 0));  // -20, -22, 0
        set.add(new Vec3i(1, -21, 20));   // 1, -21, 20
        set.add(new Vec3i(19, -22, 29));   // 19, -22, 29
        set.add(new Vec3i(20, -22, 0));   // 20, -22, 0
        set.add(new Vec3i(20, -21, -26));   // 20, -21, -26
        set.add(new Vec3i(-23, -22, 40));  // -23, -22, 40
        set.add(new Vec3i(22, -21, -14));   // 22, -21, -14
        set.add(new Vec3i(-24, -22, 12));  // -24, -22, 12
        set.add(new Vec3i(23, -22, 26));   // 23, -22, 26
        set.add(new Vec3i(23, -22, -39));   // 23, -22, -39
        set.add(new Vec3i(24, -22, 27));   // 24, -22, 27
        set.add(new Vec3i(25, -22, 17));   // 25, -22, 17
        set.add(new Vec3i(29, -21, -44));   // 29, -21, -44
        set.add(new Vec3i(-31, -21, -12));  // -31, -21, -12
        set.add(new Vec3i(-31, -21, -40));  // -31, -21, -40
        set.add(new Vec3i(30, -21, -25));   // 30, -21, -25
        set.add(new Vec3i(-32, -21, -40));  // -32, -21, -40
        set.add(new Vec3i(-36, -20, 42));  // -36, -20, 42
        set.add(new Vec3i(-37, -21, -14));  // -37, -21, -14
        set.add(new Vec3i(-37, -21, -22));   // -37, -21, -22
    });

    protected static Vec3i minesCenter = null;
    private static double previousDistance;
    private static Vec3d previousPlayerPos;
    protected static boolean newTreasure = true;
    private static boolean startedLooking = false;
    protected static List<Vec3i> possibleBlocks = new ArrayList<>();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(MetalDetector::getDistanceMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MetalDetector::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

    /**
     * Processes the message with the distance to the treasure, updates the helper, and works out possible locations using that message.
     *
     * @param text    the message sent to the player
     * @param overlay if the message is an overlay message
     */
    private static void getDistanceMessage(Text text, boolean overlay) {
        if (!overlay || !SkyblockerConfigManager.get().locations.dwarvenMines.metalDetectorHelper || !Utils.isInCrystalHollows() || !(Utils.getIslandArea().substring(2).equals("Mines of Divan")) || CLIENT.player == null) {
            checkChestFound(text);
            return;
        }
        //in the mines of divan
        Matcher treasureDistanceMature = TREASURE_PATTERN.matcher(text.getString());
        if (!treasureDistanceMature.matches()) {
            return;
        }
        //find new values
        double distance = Double.parseDouble(treasureDistanceMature.group(2));
        Vec3d playerPos = CLIENT.player.getPos();
        int previousPossibleBlockCount = possibleBlocks.size();

        //send message when starting looking about how to use mod
        if (!startedLooking) {
            startedLooking = true;
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.startTip")), false);
        }

        //find the center of the mines if possible to speed up search
        if (minesCenter == null) {
            findCenterOfMines();
        }

        //find the possible locations the treasure could be
        if (distance == previousDistance && playerPos.equals(previousPlayerPos)) {
            updatePossibleBlocks(distance, playerPos);
        }

        //if the amount of possible blocks has changed output that to the user
        if (possibleBlocks.size() != previousPossibleBlockCount) {
            if (possibleBlocks.size() == 1) {
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.foundTreasureMessage").formatted(Formatting.GREEN)), false);
            } else {
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.possibleTreasureLocationsMessage").append(Text.of(String.valueOf(possibleBlocks.size())))), false);
            }
        }

        //update previous positions
        previousDistance = distance;
        previousPlayerPos = playerPos;
    }

    /**
     * Processes the found treasure message and resets the helper
     *
     * @param text the message sent to the player
     */
    private static void checkChestFound(Text text) {
        if (!Utils.isInCrystalHollows() || !(Utils.getIslandArea().substring(2).equals("Mines of Divan")) || CLIENT.player == null) {
            return;
        }
        if (text.getString().startsWith("You found")) {
            newTreasure = true;
            possibleBlocks = new ArrayList<>();
        }
    }

    /**
     * Works out the possible locations the treasure could be using the distance the treasure is from the player and
     * narrows down possible locations until there is one left.
     *
     * @param distance the distance the treasure is from the player squared
     * @param playerPos the position of the player
     */
    protected static void updatePossibleBlocks(double distance, Vec3d playerPos) {
        if (newTreasure) {
            possibleBlocks = new ArrayList<>();
            newTreasure = false;
            if (minesCenter != null) { //if center of the mines is known use the predefined offsets to filter the locations
                for (Vec3i knownOffset : knownChestOffsets) {
                    Vec3i checkPos = minesCenter.add(knownOffset).add(0, 1, 0);
                    if (Math.abs(playerPos.distanceTo(Vec3d.of(checkPos)) - distance) < 0.25) {
                        possibleBlocks.add(checkPos);
                    }
                }
            } else {
                for (int x = (int) -distance; x <= distance; x++) {
                    for (int z = (int) -distance; z <= distance; z++) {
                        Vec3i checkPos = new Vec3i((int) playerPos.x + x, (int) playerPos.y, (int) playerPos.z + z);
                        if (Math.abs(playerPos.distanceTo(Vec3d.of(checkPos)) - distance) < 0.25) {
                            possibleBlocks.add(checkPos);
                        }
                    }
                }
            }

        } else {
            possibleBlocks.removeIf(location -> Math.abs(playerPos.distanceTo(Vec3d.of(location)) - distance) >= 0.25);
        }

        //if possible blocks is of length 0 something has failed reset and try again
        if (possibleBlocks.isEmpty()) {
            newTreasure = true;
            if (CLIENT.player != null) {
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.somethingWentWrongMessage").formatted(Formatting.RED)), false);
            }
        }
    }

    /**
     * Uses the labels for the keepers names to find the central point of the mines of divan so the known offsets can be used.
     */
    private static void findCenterOfMines() {
        if (CLIENT.player == null || CLIENT.world == null) {
            return;
        }
        Box searchBox = CLIENT.player.getBoundingBox().expand(500d);
        List<ArmorStandEntity> armorStands = CLIENT.world.getEntitiesByClass(ArmorStandEntity.class, searchBox, ArmorStandEntity::hasCustomName);

        for (ArmorStandEntity armorStand : armorStands) {
            String name = armorStand.getName().getString();
            Matcher nameMatcher = KEEPER_PATTERN.matcher(name);

            if (nameMatcher.matches()) {
                Vec3i offset = keeperOffsets.get(nameMatcher.group(1));
                minesCenter = armorStand.getBlockPos().add(offset);
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.foundCenter").formatted(Formatting.GREEN)), false);
                return;
            }
        }
    }

    private static void reset() {
        minesCenter = null;
        possibleBlocks = new ArrayList<>();
    }

    /**
     * Renders waypoints for the location of treasure or possible treasure.
     * @param context world render context
     */
    private static void render(WorldRenderContext context) {
        //only render enabled and if there is a few location options and in the mines of divan
        if (!SkyblockerConfigManager.get().locations.dwarvenMines.metalDetectorHelper || !Utils.isInCrystalHollows() || possibleBlocks.isEmpty() || possibleBlocks.size() > 8 || !(Utils.getIslandArea().substring(2).equals("Mines of Divan"))) {
            return;
        }
        //only one location render just that and guiding line to it
        if (possibleBlocks.size() == 1) {
            Vec3i block = possibleBlocks.get(0).add(0, -1, 0); //the block you are taken to is one block above the chest
            CrystalsWaypoint waypoint = new CrystalsWaypoint(CrystalsWaypoint.Category.CORLEONE, Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.treasure"), new BlockPos(block.getX(), block.getY(), block.getZ()));
            waypoint.render(context);
            RenderHelper.renderLineFromCursor(context, Vec3d.ofCenter(block), LIGHT_GRAY, 1f, 5f);
            return;
        }

        for (Vec3i block : possibleBlocks) {
            CrystalsWaypoint waypoint = new CrystalsWaypoint(CrystalsWaypoint.Category.CORLEONE, Text.translatable("skyblocker.dwarvenMines.metalDetectorHelper.possible"), new BlockPos(block.getX(), block.getY(), block.getZ()));
            waypoint.render(context);
        }
    }
}
