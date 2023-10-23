package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
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
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
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
    /**
     * The shape of the room. See {@link #getShape(IntSortedSet, IntSortedSet)}.
     */
    @NotNull
    private final Shape shape;
    /**
     * The room data containing all rooms for a specific dungeon and {@link #shape}.
     */
    private Map<String, int[]> roomsData;
    /**
     * Contains all possible dungeon rooms for this room. The list is gradually shrunk by checking blocks until only one room is left.
     */
    private List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms;
    /**
     * Contains all blocks that have been checked to prevent checking the same block multiple times.
     */
    private Set<BlockPos> checkedBlocks = new HashSet<>();
    /**
     * The task that is used to check blocks. This is used to ensure only one such task can run at a time.
     */
    private CompletableFuture<Void> findRoom;
    private int doubleCheckBlocks;
    /**
     * Represents the matching state of the room with the following possible values:
     * <li>{@link TriState#DEFAULT} means that the room has not been checked, is being processed, or does not {@link Type#needsScanning() need to be processed}.
     * <li>{@link TriState#FALSE} means that the room has been checked and there is no match.
     * <li>{@link TriState#TRUE} means that the room has been checked and there is a match.
     */
    private TriState matched = TriState.DEFAULT;
    private Table<Integer, BlockPos, SecretWaypoint> secretWaypoints;
    private String name;
    private Direction direction;
    private Vector2ic physicalCornerPos;

    public Room(@NotNull Type type, @NotNull Vector2ic... physicalPositions) {
        this.type = type;
        segments = Set.of(physicalPositions);
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        shape = getShape(segmentsX, segmentsY);
        roomsData = DungeonSecrets.ROOMS_DATA.getOrDefault("catacombs", Collections.emptyMap()).getOrDefault(shape.shape.toLowerCase(), Collections.emptyMap());
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
    }

    @NotNull
    public Type getType() {
        return type;
    }

    public boolean isMatched() {
        return matched == TriState.TRUE;
    }

    /**
     * Not null if {@link #isMatched()}.
     */
    public String getName() {
        return name;
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

    /**
     * Updates the room.
     * <p></p>
     * This method returns immediately if any of the following conditions are met:
     * <ul>
     *     <li> The room does not need to be scanned and matched. (When the room is not of type {@link Type.ROOM}, {@link Type.PUZZLE}, or {@link Type.TRAP}. See {@link Type#needsScanning()}) </li>
     *     <li> The room has been matched or failed to match and is on cooldown. See {@link #matched}. </li>
     *     <li> {@link #findRoom The previous update} has not completed. </li>
     * </ul>
     * Then this method tries to match this room through:
     * <ul>
     *     <li> Iterate over a 11 by 11 by 11 box around the player. </li>
     *     <li> Check it the block is part of this room and not part of a doorway. See {@link #segments} and {@link #notInDoorway(BlockPos)}. </li>
     *     <li> Checks if the position has been checked and adds it to {@link #checkedBlocks}. </li>
     *     <li> Calls {@link #checkBlock(ClientWorld, BlockPos)} </li>
     * </ul>
     */
    @SuppressWarnings("JavadocReference")
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
            for (BlockPos pos : BlockPos.iterate(player.getBlockPos().add(-5, -5, -5), player.getBlockPos().add(5, 5, 5))) {
                if (segments.contains(DungeonMapUtils.getPhysicalRoomPos(pos)) && notInDoorway(pos) && checkedBlocks.add(pos) && checkBlock(world, pos)) {
                    break;
                }
            }
        });
    }

    private static boolean notInDoorway(BlockPos pos) {
        if (pos.getY() < 66 || pos.getY() > 73) {
            return true;
        }
        int x = Math.floorMod(pos.getX() - 8, 32);
        int z = Math.floorMod(pos.getZ() - 8, 32);
        return (x < 13 || x > 17 || z > 2 && z < 28) && (z < 13 || z > 17 || x > 2 && x < 28);
    }

    /**
     * Filters out dungeon rooms which does not contain the block at the given position.
     * <p></p>
     * This method:
     * <ul>
     *     <li> Checks if the block type is included in the dungeon rooms data. See {@link DungeonSecrets#NUMERIC_ID}. </li>
     *     <li> For each possible direction: </li>
     *     <ul>
     *         <li> Rotate and convert the position to a relative position. See {@link DungeonMapUtils#actualToRelative(Direction, Vector2ic, BlockPos)}. </li>
     *         <li> Encode the block based on the relative position and the custom numeric block id. See {@link #posIdToInt(BlockPos, byte)}. </li>
     *         <li> For each possible room in the current direction: </li>
     *         <ul>
     *             <li> Check if {@link #roomsData} contains the encoded block. </li>
     *             <li> If so, add the room to the new list of possible rooms for this direction. </li>
     *         </ul>
     *         <li> Replace the old possible room list for the current direction with the new one. </li>
     *     </ul>
     *     <li> If there are no matching rooms left: </li>
     *     <ul>
     *         <li> Terminate matching by setting {@link #matched} to {@link TriState#FALSE}. </li>
     *         <li> Schedule another matching attempt in 50 ticks (2.5 seconds). </li>
     *         <li> Reset {@link #possibleRooms} and {@link #checkedBlocks} with {@link #reset()}. </li>
     *         <li> Return {@code true} </li>
     *     </ul>
     *     <li> If there are exactly one room matching: </li>
     *     <ul>
     *         <li> Call {@link #roomMatched()}. </li>
     *         <li> Discard the no longer needed fields to save memory. </li>
     *         <li> Return {@code true} </li>
     *     </ul>
     *     <li> Return {@code false} </li>
     * </ul>
     *
     * @param world the world to get the block from
     * @param pos   the position of the block to check
     * @return whether room matching should end. Either a match is found or there are no valid rooms left
     */
    private boolean checkBlock(ClientWorld world, BlockPos pos) {
        byte id = DungeonSecrets.NUMERIC_ID.getByte(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString());
        if (id == 0) {
            return false;
        }
        for (MutableTriple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
            int block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getLeft(), directionRooms.getMiddle(), pos), id);
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
            Scheduler.INSTANCE.schedule(() -> matched = TriState.DEFAULT, 50);
            reset();
            return true;
        } else if (matchingRoomsSize == 1 && ++doubleCheckBlocks >= 10) {
            // If one room matches, load the secrets for that room and discard the no longer needed fields.
            for (Triple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
                if (directionRooms.getRight().size() == 1) {
                    name = directionRooms.getRight().get(0);
                    direction = directionRooms.getLeft();
                    physicalCornerPos = directionRooms.getMiddle();
                    roomMatched();
                    discard();
                    return true;
                }
            }
            return false; // This should never happen, we just checked that there is one possible room, and the return true in the loop should activate
        } else {
            DungeonSecrets.LOGGER.debug("[Skyblocker] {} room(s) remaining after checking {} block(s)", matchingRoomsSize, checkedBlocks.size());
            return false;
        }
    }

    /**
     * Encodes a {@link BlockPos} and the custom numeric block id into an integer.
     *
     * @param pos the position of the block
     * @param id  the custom numeric block id
     * @return the encoded integer
     */
    private int posIdToInt(BlockPos pos, byte id) {
        return pos.getX() << 24 | pos.getY() << 16 | pos.getZ() << 8 | id;
    }

    /**
     * Loads the secret waypoints for the room from {@link DungeonSecrets#waypointsJson} once it has been matched
     * and sets {@link #matched} to {@link TriState#TRUE}.
     *
     * @param directionRooms the direction, position, and name of the room
     */
    @SuppressWarnings("JavadocReference")
    private void roomMatched() {
        Table<Integer, BlockPos, SecretWaypoint> secretWaypointsMutable = HashBasedTable.create();
        for (JsonElement waypointElement : DungeonSecrets.getRoomWaypoints(name)) {
            JsonObject waypoint = waypointElement.getAsJsonObject();
            String secretName = waypoint.get("secretName").getAsString();
            int secretIndex = Integer.parseInt(secretName.substring(0, Character.isDigit(secretName.charAt(1)) ? 2 : 1));
            BlockPos pos = DungeonMapUtils.relativeToActual(direction, physicalCornerPos, waypoint);
            secretWaypointsMutable.put(secretIndex, pos, new SecretWaypoint(secretIndex, waypoint, secretName, pos));
        }
        secretWaypoints = ImmutableTable.copyOf(secretWaypointsMutable);
        matched = TriState.TRUE;

        DungeonSecrets.LOGGER.info("[Skyblocker] Room {} matched after checking {} block(s)", name, checkedBlocks.size());
    }

    /**
     * Resets fields for another round of matching after room matching fails.
     */
    private void reset() {
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
        checkedBlocks = new HashSet<>();
        doubleCheckBlocks = 0;
    }

    /**
     * Discards fields after room matching completes when a room is found.
     * These fields are no longer needed and are discarded to save memory.
     */
    private void discard() {
        roomsData = null;
        possibleRooms = null;
        checkedBlocks = null;
        doubleCheckBlocks = 0;
    }

    /**
     * Fails if !{@link #isMatched()}
     */
    protected BlockPos actualToRelative(BlockPos pos) {
        return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos);
    }

    /**
     * Fails if !{@link #isMatched()}
     */
    protected BlockPos relativeToActual(BlockPos pos) {
        return DungeonMapUtils.relativeToActual(direction, physicalCornerPos, pos);
    }

    /**
     * Calls {@link SecretWaypoint#render(WorldRenderContext)} on {@link #secretWaypoints all secret waypoints}.
     */
    protected void render(WorldRenderContext context) {
        for (SecretWaypoint secretWaypoint : secretWaypoints.values()) {
            if (secretWaypoint.shouldRender()) {
                secretWaypoint.render(context);
            }
        }
    }

    /**
     * Sets all secrets as found if {@link #isAllSecretsFound(String)}.
     */
    protected void onChatMessage(String message) {
        if (isAllSecretsFound(message)) {
            secretWaypoints.values().forEach(SecretWaypoint::setFound);
        }
    }

    /**
     * Checks if the number of found secrets is equals or greater than the total number of secrets in the room.
     *
     * @param message the message to check in
     * @return whether the number of found secrets is equals or greater than the total number of secrets in the room
     */
    protected static boolean isAllSecretsFound(String message) {
        Matcher matcher = SECRETS.matcher(message);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1)) >= Integer.parseInt(matcher.group(2));
        }
        return false;
    }

    /**
     * Marks the secret at the interaction position as found when the player interacts with a chest or a player head,
     * if there is a secret at the interaction position.
     *
     * @param world     the world to get the block from
     * @param hitResult the block being interacted with
     * @see #onSecretFound(SecretWaypoint, String, Object...)
     */
    protected void onUseBlock(World world, BlockHitResult hitResult) {
        BlockState state = world.getBlockState(hitResult.getBlockPos());
        if (state.isOf(Blocks.CHEST) || state.isOf(Blocks.PLAYER_HEAD) || state.isOf(Blocks.PLAYER_WALL_HEAD)) {
            secretWaypoints.column(hitResult.getBlockPos()).values().stream().filter(SecretWaypoint::needsInteraction).findAny()
                    .ifPresent(secretWaypoint -> onSecretFound(secretWaypoint, "[Skyblocker] Detected {} interaction, setting secret #{} as found", secretWaypoint.category, secretWaypoint.secretIndex));
        } else if (state.isOf(Blocks.LEVER)) {
            secretWaypoints.column(hitResult.getBlockPos()).values().stream().filter(SecretWaypoint::isLever).forEach(SecretWaypoint::setFound);
        }
    }

    /**
     * Marks the closest secret that requires item pickup no greater than 6 blocks away as found when the player picks up a secret item.
     *
     * @param itemEntity the item entity being picked up
     * @param collector  the collector of the item
     * @see #onSecretFound(SecretWaypoint, String, Object...)
     */
    protected void onItemPickup(ItemEntity itemEntity, LivingEntity collector) {
        if (SecretWaypoint.SECRET_ITEMS.stream().noneMatch(itemEntity.getStack().getName().getString()::contains)) {
            return;
        }
        secretWaypoints.values().stream().filter(SecretWaypoint::needsItemPickup).min(Comparator.comparingDouble(SecretWaypoint.getSquaredDistanceToFunction(collector))).filter(SecretWaypoint.getRangePredicate(collector))
                .ifPresent(secretWaypoint -> onSecretFound(secretWaypoint, "[Skyblocker] Detected {} picked up a {} from a {} secret, setting secret #{} as found", collector.getName().getString(), itemEntity.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
    }

    /**
     * Marks the closest bat secret as found when a bat is killed.
     *
     * @param bat the bat being killed
     * @see #onSecretFound(SecretWaypoint, String, Object...)
     */
    protected void onBatRemoved(AmbientEntity bat) {
        secretWaypoints.values().stream().filter(SecretWaypoint::isBat).min(Comparator.comparingDouble(SecretWaypoint.getSquaredDistanceToFunction(bat)))
                .ifPresent(secretWaypoint -> onSecretFound(secretWaypoint, "[Skyblocker] Detected {} killed for a {} secret, setting secret #{} as found", bat.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
    }

    /**
     * Marks all secret waypoints with the same index as the given {@link SecretWaypoint} as found.
     *
     * @param secretWaypoint the secret waypoint to read the index from.
     * @param msg            the message to log
     * @param args           the args for the {@link org.slf4j.Logger#info(String, Object...) Logger#info(String, Object...)} call
     */
    private void onSecretFound(SecretWaypoint secretWaypoint, String msg, Object... args) {
        secretWaypoints.row(secretWaypoint.secretIndex).values().forEach(SecretWaypoint::setFound);
        DungeonSecrets.LOGGER.info(msg, args);
    }

    protected boolean markSecrets(int secretIndex, boolean found) {
        Map<BlockPos, SecretWaypoint> secret = secretWaypoints.row(secretIndex);
        if (secret.isEmpty()) {
            return false;
        } else {
            secret.values().forEach(found ? SecretWaypoint::setFound : SecretWaypoint::setMissing);
            return true;
        }
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
