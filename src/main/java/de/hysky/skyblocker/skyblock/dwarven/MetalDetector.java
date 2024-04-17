package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MetalDetector {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    private static final float[] LIGHT_GRAY = { 192 / 255f, 192 / 255f, 192 / 255f };

    private static final Pattern TREASURE_PATTERN = Pattern.compile("(§3§lTREASURE: §b)(\\d+\\.\\d)m");

    private static final Pattern KEEPER_PATTERN = Pattern.compile("Keeper of (\\w+)");
    private static final HashMap<String, Vec3i> keeperOffsets = new HashMap<>() {{
        put("Diamond", new Vec3i(33, 0, 3));
        put("Lapis", new Vec3i(-33, 0, -3));
        put("Emerald", new Vec3i(-3, 0, 33));
        put("Gold", new Vec3i(3, 0, -33));
    }};

    private static final HashSet<Vec3i> knownChestOffsets = new HashSet<>(Arrays.asList(
            new Vec3i(-38, -22, 26),  // -38, -22, 26
            new Vec3i(38, -22, -26),  // 38, -22, -26
            new Vec3i(-40, -22, 18),  // -40, -22, 18
            new Vec3i(-41, -20, 22),  // -41, -20, 22
            new Vec3i(-5, -21, 16),  // -5, -21, 16
            new Vec3i(40, -22, -30), // 40, -22, -30
            new Vec3i(-42, -20, -28),  // -42, -20, -28
            new Vec3i(-43, -22, -40),  // -43, -22, -40
            new Vec3i(42, -19, -41), // 42, -19, -41
            new Vec3i(43, -21, -16), // 43, -21, -16
            new Vec3i(-1, -22, -20),      // -1, -22, -20
            new Vec3i(6, -21, 28),   // 6, -21, 28
            new Vec3i(7, -21, 11),   // 7, -21, 11
            new Vec3i(7, -21, 22),   // 7, -21, 22
            new Vec3i(-12, -21, -44),  // -12, -21, -44
            new Vec3i(12, -22, 31),   // 12, -22, 31
            new Vec3i(12, -22, -22),   // 12, -22, -22
            new Vec3i(12, -21, 7),   // 12, -21, 7
            new Vec3i(12, -21, -43),   // 12, -21, -43
            new Vec3i(-14, -21, 43),  // -14, -21, 43
            new Vec3i(-14, -21, 22),  // -14, -21, 22
            new Vec3i(-17, -21, 20),  // -17, -21, 20
            new Vec3i(-20, -22, 0),  // -20, -22, 0
            new Vec3i(1, -21, 20),   // 1, -21, 20
            new Vec3i(19, -22, 29),   // 19, -22, 29
            new Vec3i(20, -22, 0),   // 20, -22, 0
            new Vec3i(20, -21, -26),   // 20, -21, -26
            new Vec3i(-23, -22, 40),  // -23, -22, 40
            new Vec3i(22, -21, -14),   // 22, -21, -14
            new Vec3i(-24, -22, 12),  // -24, -22, 12
            new Vec3i(23, -22, 26),   // 23, -22, 26
            new Vec3i(23, -22, -39),   // 23, -22, -39
            new Vec3i(24, -22, 27),   // 24, -22, 27
            new Vec3i(25, -22, 17),   // 25, -22, 17
            new Vec3i(29, -21, -44),   // 29, -21, -44
            new Vec3i(-31, -21, -12),  // -31, -21, -12
            new Vec3i(-31, -21, -40),  // -31, -21, -40
            new Vec3i(30, -21, -25),   // 30, -21, -25
            new Vec3i(-32, -21, -40),  // -32, -21, -40
            new Vec3i(-36, -20, 42),  // -36, -20, 42
            new Vec3i(-37, -21, -14),  // -37, -21, -14
            new Vec3i(-37, -21, -22)   // -37, -21, -22
    ));

    private static Vec3i minesCenter = null;
    private static double previousDistance;
    private static Vec3d previousPlayerPos;
    private static boolean newTreasure = true;

    private static boolean startedLooking = false;

    private static List<Vec3i> possibleBlocks = new ArrayList<>();

    public static void init() {
        ClientReceiveMessageEvents.GAME.register(MetalDetector::getDistanceMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(MetalDetector::render);
    }

    private static void getDistanceMessage(Text text, boolean overlay) {
        if (!overlay || !SkyblockerConfigManager.get().locations.dwarvenMines.MetalDetectorHelper || !Utils.isInCrystalHollows() || !(Utils.getIslandArea().substring(2).equals("Mines of Divan")) || CLIENT.player == null) {
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
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.startTip")),false);
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
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.foundTreasureMessage").formatted(Formatting.GREEN)),false);
            }
            else {
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.possibleTreasureLocationsMessage").append(Text.of(String.valueOf(possibleBlocks.size())))),false);
            }
        }

        //update previous positions
        previousDistance = distance;
        previousPlayerPos = playerPos;
    }

    private static void checkChestFound(Text text) {
        if (!Utils.isInCrystalHollows() || !(Utils.getIslandArea().substring(2).equals("Mines of Divan")) || CLIENT.player == null) {
            return;
        }
        if (text.getString().startsWith("You found")) {
            newTreasure = true;
            possibleBlocks = new ArrayList<>();
        }
    }

    private static void updatePossibleBlocks(Double distance, Vec3d playerPos) {
        if (newTreasure) {
            possibleBlocks = new ArrayList<>();
            newTreasure = false;
            if (minesCenter != null) {//if center of the mines is known use the predefined offsets to filter the locations
                for (Vec3i knownOffset : knownChestOffsets) {
                    Vec3i checkPos = minesCenter.add(knownOffset).add(0,1,0);
                    if (Math.abs(playerPos.distanceTo(Vec3d.of(checkPos)) - distance) < 0.25) {
                        possibleBlocks.add(checkPos);
                    }
                }
            }
            else {
                for (int x = -distance.intValue(); x < distance; x++) {
                    for (int z = -distance.intValue(); z < distance; z++) {
                        Vec3i checkPos =new Vec3i((int)(playerPos.x) + x, (int)playerPos.y, (int)playerPos.z + z);
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
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.somethingWentWrongMessage").formatted(Formatting.RED)),false);
            }
        }
    }

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
                CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.foundCenter").formatted(Formatting.GREEN)),false);
                return;
            }
        }
    }

    private static void render(WorldRenderContext context) {
        //only render enabled and if there is a few location options and in the mines of divan
        if (!SkyblockerConfigManager.get().locations.dwarvenMines.MetalDetectorHelper || possibleBlocks.isEmpty() || possibleBlocks.size() > 8 || !(Utils.getIslandArea().substring(2).equals("Mines of Divan"))) {
            return;
        }
        //only one location render just that and guiding line to it
        if (possibleBlocks.size() == 1) {
            Vec3i block = possibleBlocks.get(0).add(0, -1, 0); //the block you are taken to is one block above the chest
            CrystalsWaypoint waypoint = new CrystalsWaypoint(CrystalsWaypoint.Category.MINES_OF_DIVAN, Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.treasure"), new BlockPos(block.getX(), block.getY(), block.getZ()));
            waypoint.render(context);
            RenderHelper.renderLineFromCursor(context, Vec3d.ofCenter(block), LIGHT_GRAY, 1f, 5f);
            return;
        }

        for (Vec3i block : possibleBlocks) {
            CrystalsWaypoint waypoint = new CrystalsWaypoint(CrystalsWaypoint.Category.MINES_OF_DIVAN, Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.MetalDetectorHelper.possible"), new BlockPos(block.getX(), block.getY(), block.getZ()));
            waypoint.render(context);
        }
    }
}
