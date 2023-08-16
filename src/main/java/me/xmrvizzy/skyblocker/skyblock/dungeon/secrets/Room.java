package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import me.xmrvizzy.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Room {
    private static final Pattern SECRETS = Pattern.compile("§7(\\d{1,2})/(\\d{1,2}) Secrets");
    @NotNull
    private final Type type;
    @NotNull
    private final Set<Vector2ic> segments;
    @NotNull
    private final Shape shape;
    private HashMap<String, int[]> roomsData;
    private List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms;
    private Set<BlockPos> checkedBlocks = new HashSet<>();
    private CompletableFuture<Void> findRoom;
    /**
     * Represents the matching state of the room with the following possible values:
     * <li>{@link TriState#DEFAULT} means that the room has not been checked, is being processed, or does not {@link Type#needsScanning() need to be processed}.
     * <li>{@link TriState#FALSE} means that the room has been checked and there is no match.
     * <li>{@link TriState#TRUE} means that the room has been checked and there is a match.
     */
    private TriState matched = TriState.DEFAULT;
    private Table<Integer, BlockPos, SecretWaypoint> secretWaypoints;

    public Room(@NotNull Type type, @NotNull Vector2ic... physicalPositions) {
        this.type = type;
        segments = Set.of(physicalPositions);
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        shape = getShape(segmentsX, segmentsY);
        roomsData = DungeonSecrets.ROOMS_DATA.get("catacombs").get(shape.shape);
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public boolean isMatched() {
        return matched == TriState.TRUE;
    }

    @Override
    public String toString() {
        return "Room{type=" + type + ", shape=" + shape + ", matched=" + matched + ", segments=" + Arrays.toString(segments.toArray()) + "}";
    }

    @NotNull
    private Shape getShape(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        return switch (segments.size()) {
            case 1 -> Shape.ONE_BY_ONE;
            case 2 -> Shape.ONE_BY_TWO;
            case 3 -> segmentsX.size() == 2 && segmentsY.size() == 2 ? Shape.L_SHAPE : Shape.ONE_BY_THREE;
            case 4 -> segmentsX.size() == 2 && segmentsY.size() == 2 ? Shape.TWO_BY_TWO : Shape.ONE_BY_FOUR;
            default -> throw new IllegalArgumentException("There are no matching room shapes with this set of physical positions: " + Arrays.toString(segments.toArray()));
        };
    }

    private List<MutableTriple<Direction, Vector2ic, List<String>>> getPossibleRooms(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        List<String> possibleDirectionRooms = new ArrayList<>(roomsData.keySet());
        List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms = new ArrayList<>();
        for (Direction direction : getPossibleDirections(segmentsX, segmentsY)) {
            possibleRooms.add(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), possibleDirectionRooms));
        }
        return possibleRooms;
    }

    @NotNull
    private Direction[] getPossibleDirections(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        return switch (shape) {
            case ONE_BY_ONE, TWO_BY_TWO -> Direction.values();
            case ONE_BY_TWO, ONE_BY_THREE, ONE_BY_FOUR -> {
                if (segmentsX.size() > 1 && segmentsY.size() == 1) {
                    yield new Direction[]{Direction.NW, Direction.SE};
                } else if (segmentsX.size() == 1 && segmentsY.size() > 1) {
                    yield new Direction[]{Direction.NE, Direction.SW};
                }
                throw new IllegalArgumentException("Shape " + shape.shape + " does not match segments: " + Arrays.toString(segments.toArray()));
            }
            case L_SHAPE -> {
                if (!segments.contains(new Vector2i(segmentsX.firstInt(), segmentsY.firstInt()))) {
                    yield new Direction[]{Direction.SW};
                } else if (!segments.contains(new Vector2i(segmentsX.firstInt(), segmentsY.lastInt()))) {
                    yield new Direction[]{Direction.SE};
                } else if (!segments.contains(new Vector2i(segmentsX.lastInt(), segmentsY.firstInt()))) {
                    yield new Direction[]{Direction.NW};
                } else if (!segments.contains(new Vector2i(segmentsX.lastInt(), segmentsY.lastInt()))) {
                    yield new Direction[]{Direction.NE};
                }
                throw new IllegalArgumentException("Shape " + shape.shape + " does not match segments: " + Arrays.toString(segments.toArray()));
            }
        };
    }

    protected void update() {
        // Logical AND has higher precedence than logical OR
        if (!type.needsScanning() || matched != TriState.DEFAULT || !DungeonSecrets.isRoomsLoaded() || findRoom != null && !findRoom.isDone()) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        ClientWorld world = client.world;
        if (player == null || world == null) {
            return;
        }
        findRoom = CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            for (BlockPos pos : BlockPos.iterate(player.getBlockPos().add(-5, -5, -5), player.getBlockPos().add(5, 5, 5))) {
                if (segments.contains(DungeonMapUtils.getPhysicalRoomPos(pos)) && notInDoorway(pos) && checkedBlocks.add(pos) && checkBlock(world, pos)) {
                    break;
                }
            }
            long endTime = System.currentTimeMillis();
            DungeonSecrets.LOGGER.info("[Skyblocker] Processed room in {} ms", endTime - startTime); // TODO change to debug
        });
    }

    private static boolean notInDoorway(BlockPos pos) {
        if (pos.getY() < 66 || pos.getY() > 73) {
            return true;
        }
        int x = MathHelper.floorMod(pos.getX() - 8, 32);
        int z = MathHelper.floorMod(pos.getZ() - 8, 32);
        return (x < 13 || x > 17 || z > 2 && z < 28) && (z < 13 || z > 17 || x > 2 && x < 28);
    }

    private boolean checkBlock(ClientWorld world, BlockPos pos) {
        byte id = DungeonSecrets.NUMERIC_ID.getByte(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString());
        if (id == 0) {
            return false;
        }
        for (MutableTriple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
            int block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getMiddle(), directionRooms.getLeft(), pos), id);
            List<String> possibleDirectionRooms = new ArrayList<>();
            for (String room : directionRooms.getRight()) {
                if (Arrays.binarySearch(roomsData.get(room), block) >= 0) {
                    possibleDirectionRooms.add(room);
                }
            }
            directionRooms.setRight(possibleDirectionRooms);
        }

        int matchingRoomsSize = possibleRooms.stream().map(Triple::getRight).mapToInt(Collection::size).sum();
        if (matchingRoomsSize == 0) {
            // If no rooms match, reset the fields and scan again after 50 ticks.
            matched = TriState.FALSE;
            DungeonSecrets.LOGGER.warn("[Skyblocker] No dungeon room matches after checking {} block(s)", checkedBlocks.size());
            SkyblockerMod.getInstance().scheduler.schedule(() -> matched = TriState.DEFAULT, 50);
            reset();
            return true;
        } else if (matchingRoomsSize == 1) {
            // If one room matches, load the secrets for that room and discard the no longer needed fields.
            for (Triple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
                if (directionRooms.getRight().size() == 1) {
                    roomMatched(directionRooms);
                    discard();
                    return true;
                }
            }
            return false; // This should never happen, we just checked that there is one possible room, and the return true in the loop should activate
        } else {
            DungeonSecrets.LOGGER.info("[Skyblocker] {} rooms remaining after checking {} block(s)", matchingRoomsSize, checkedBlocks.size()); // TODO change to debug
            return false;
        }
    }

    private int posIdToInt(BlockPos pos, byte id) {
        return pos.getX() << 24 | pos.getY() << 16 | pos.getZ() << 8 | id;
    }

    private void roomMatched(Triple<Direction, Vector2ic, List<String>> directionRooms) {
        Table<Integer, BlockPos, SecretWaypoint> secretWaypointsMutable = HashBasedTable.create();
        String name = directionRooms.getRight().get(0);
        for (JsonElement waypointElement : DungeonSecrets.getWaypointsJson().get(name).getAsJsonArray()) {
            JsonObject waypoint = waypointElement.getAsJsonObject();
            String secretName = waypoint.get("secretName").getAsString();
            int secretIndex = Integer.parseInt(secretName.substring(0, Character.isDigit(secretName.charAt(1)) ? 2 : 1));
            BlockPos pos = DungeonMapUtils.relativeToActual(directionRooms.getMiddle(), directionRooms.getLeft(), waypoint);
            secretWaypointsMutable.put(secretIndex, pos, new SecretWaypoint(secretIndex, waypoint, secretName, pos));
        }
        secretWaypoints = ImmutableTable.copyOf(secretWaypointsMutable);
        matched = TriState.TRUE;
        DungeonSecrets.LOGGER.info("[Skyblocker] Room {} matched after checking {} block(s)", name, checkedBlocks.size()); // TODO change to debug
    }

    /**
     * Resets fields for another round of matching after room matching fails.
     */
    private void reset() {
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
        checkedBlocks = new HashSet<>();
    }

    /**
     * Discards fields after room matching completes when a room is found.
     * These fields are no longer needed and are discarded to save memory.
     */
    private void discard() {
        roomsData = null;
        possibleRooms = null;
        checkedBlocks = null;
    }

    protected void render(WorldRenderContext context) {
        for (SecretWaypoint secretWaypoint : secretWaypoints.values()) {
            if (secretWaypoint.isMissing()) {
                secretWaypoint.render(context);
            }
        }
    }

    protected void onChatMessage(String message) {
        if (isAllSecretsFound(message)) {
            secretWaypoints.values().forEach(SecretWaypoint::setFound);
        }
    }

    protected static boolean isAllSecretsFound(String message) {
        Matcher matcher = SECRETS.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)) >= Integer.parseInt(matcher.group(2));
        }
        return false;
    }

    protected void onUseBlock(World world, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (!state.isOf(Blocks.CHEST) && !state.isOf(Blocks.PLAYER_HEAD) && !state.isOf(Blocks.PLAYER_WALL_HEAD)) {
            return;
        }
        secretWaypoints.column(hitResult.getBlockPos()).values().stream().filter(secretWaypoint -> secretWaypoint.category.needsInteraction()).findAny()
                .ifPresent(secretWaypoint -> onSecretFound(secretWaypoint, "[Skyblocker] Detected {} interaction, setting secret #{} as found", secretWaypoint.category, secretWaypoint.secretIndex));
    }

    protected void onItemPickup(ItemEntity itemEntity, LivingEntity collector) {
        if (SecretWaypoint.SECRET_ITEMS.stream().noneMatch(itemEntity.getStack().getName().getString()::contains)) {
            return;
        }
        secretWaypoints.values().stream().filter(secretWaypoint -> secretWaypoint.category.needsItemPickup()).min(Comparator.comparingDouble(secretWaypoint -> collector.squaredDistanceTo(secretWaypoint.centerPos))).filter(secretWaypoint -> collector.squaredDistanceTo(secretWaypoint.centerPos) <= 36D)
                .ifPresent(secretWaypoint -> onSecretFound(secretWaypoint, "[Skyblocker] Detected {} picked up a {} from a {} secret, setting secret #{} as found", collector.getName().getString(), itemEntity.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
    }

    private void onSecretFound(SecretWaypoint secretWaypoint, String msg, Object... args) {
        secretWaypoints.row(secretWaypoint.secretIndex).values().forEach(SecretWaypoint::setFound);
        DungeonSecrets.LOGGER.info(msg, args);
    }

    public enum Type {
        ENTRANCE(MapColor.DARK_GREEN.getRenderColorByte(MapColor.Brightness.HIGH)),
        ROOM(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.LOWEST)),
        PUZZLE(MapColor.MAGENTA.getRenderColorByte(MapColor.Brightness.HIGH)),
        TRAP(MapColor.ORANGE.getRenderColorByte(MapColor.Brightness.HIGH)),
        MINIBOSS(MapColor.YELLOW.getRenderColorByte(MapColor.Brightness.HIGH)),
        FAIRY(MapColor.PINK.getRenderColorByte(MapColor.Brightness.HIGH)),
        BLOOD(MapColor.BRIGHT_RED.getRenderColorByte(MapColor.Brightness.HIGH)),
        UNKNOWN(MapColor.GRAY.getRenderColorByte(MapColor.Brightness.NORMAL));
        final byte color;

        Type(byte color) {
            this.color = color;
        }

        /**
         * @return whether this room type has secrets and needs to be scanned and matched.
         */
        private boolean needsScanning() {
            return switch (this) {
                case ROOM, PUZZLE, TRAP -> true;
                default -> false;
            };
        }
    }

    private enum Shape {
        ONE_BY_ONE("1x1"),
        ONE_BY_TWO("1x2"),
        ONE_BY_THREE("1x3"),
        ONE_BY_FOUR("1x4"),
        L_SHAPE("L-shape"),
        TWO_BY_TWO("2x2");
        final String shape;

        Shape(String shape) {
            this.shape = shape;
        }

        @Override
        public String toString() {
            return shape;
        }
    }

    public enum Direction {
        NW, NE, SW, SE
    }
}
