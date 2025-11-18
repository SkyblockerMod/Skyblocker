package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.command.argument.EnumArgumentType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Room implements Tickable, Renderable {
    private static final Pattern SECRET_INDEX = Pattern.compile("^(\\d+)");
    private static final Pattern SECRETS = Pattern.compile("§7(\\d{1,2})/(\\d{1,2}) Secrets");
    private static final String LOCKED_CHEST = "That chest is locked!";
    protected static final float[] RED_COLOR_COMPONENTS = {1, 0, 0};
    protected static final float[] GREEN_COLOR_COMPONENTS = {0, 1, 0};
    @NotNull
    private final Type type;
    @NotNull
    final Set<Vector2ic> segments;
    /**
     * Used to allow rooms to have their secrets unmarked after the map detects the green checkmark.
     * This should not be used for rendering as it would break the above case and having the prince waypoints show until a prince is killed.
     */
    public boolean greenChecked = false;

	public boolean whiteChecked = false;

    /**
     * The shape of the room. See {@link #determineShape(IntSortedSet, IntSortedSet)}.
     */
    @NotNull
    private final Shape shape;
    /**
     * The room data containing all rooms for a specific dungeon and {@link #shape}.
     */
    protected Map<String, int[]> roomsData;
    /**
     * Contains all possible dungeon rooms for this room. The list is gradually shrunk by checking blocks until only one room is left.
     */
    protected List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms;
    /**
     * Contains all blocks that have been checked to prevent checking the same block multiple times.
     */
    private Set<BlockPos> checkedBlocks = new HashSet<>();
    /**
     * The task that is used to check blocks. This is used to ensure only one such task can run at a time.
     */
    protected CompletableFuture<Void> findRoom;
    private int doubleCheckBlocks;
    /**
     * Represents the matching state of the room with the following possible values:
     * <li>{@link MatchState#MATCHING} means that the room has not been checked, is being processed, or does not {@link Type#needsScanning() need to be processed}.</li>
     * <li>{@link MatchState#DOUBLE_CHECKING} means that the room has a unique match and is being double-checked.</li>
     * <li>{@link MatchState#MATCHED} means that the room has a unique match and has been double-checked.</li>
     * <li>{@link MatchState#FAILED} means that the room has been checked and there is no match.</li>
     */
    protected MatchState matchState = MatchState.MATCHING;
    private Table<Integer, BlockPos, SecretWaypoint> secretWaypoints;
    private String name;
    private Direction direction;
    private Vector2ic physicalCornerPos;

    protected List<Tickable> tickables = new ArrayList<>();
    protected List<Renderable> renderables = new ArrayList<>();
    private BlockPos lastChestSecret;
    private long lastChestSecretTime;

    public Room(@NotNull Type type, @NotNull Vector2ic... physicalPositions) {
        this.type = type;
        segments = Set.of(physicalPositions);
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        shape = determineShape(segmentsX, segmentsY);
        roomsData = DungeonManager.ROOMS_DATA.getOrDefault("catacombs", Collections.emptyMap()).getOrDefault(shape.shape.toLowerCase(Locale.ENGLISH), Collections.emptyMap());
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
    }

    @NotNull
    public Type getType() {
        return type;
    }

	@NotNull
	public Set<Vector2ic> getSegments() {
		return segments;
	}

	@NotNull
	public Shape getShape() {
		return shape;
	}

	public Vector2ic getPhysicalCornerPos() {
		return physicalCornerPos;
	}

    public boolean isMatched() {
        return matchState == MatchState.DOUBLE_CHECKING || matchState == MatchState.MATCHED;
    }

    /**
     * Not null if {@link #isMatched()}.
     */
    public String getName() {
        return name;
    }

    /**
     * Not null if {@link #isMatched()}.
     */
    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return "Room{type=%s, segments=%s, shape=%s, matchState=%s, name=%s, direction=%s, physicalCornerPos=%s}".formatted(type, Arrays.toString(segments.toArray()), shape, matchState, name, direction, physicalCornerPos);
    }

    @NotNull
    private Shape determineShape(IntSortedSet segmentsX, IntSortedSet segmentsY) {
        return switch (type) {
            case PUZZLE -> Shape.PUZZLE;
            case TRAP -> Shape.TRAP;
			case MINIBOSS -> Shape.MINIBOSS;
            default -> switch (segments.size()) {
                case 1 -> Shape.ONE_BY_ONE;
                case 2 -> Shape.ONE_BY_TWO;
                case 3 -> segmentsX.size() == 2 && segmentsY.size() == 2 ? Shape.L_SHAPE : Shape.ONE_BY_THREE;
                case 4 -> segmentsX.size() == 2 && segmentsY.size() == 2 ? Shape.TWO_BY_TWO : Shape.ONE_BY_FOUR;
                default -> throw new IllegalArgumentException("There are no matching room shapes with this set of physical positions: " + Arrays.toString(segments.toArray()));
            };
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
			case ONE_BY_ONE, TWO_BY_TWO, PUZZLE, TRAP, MINIBOSS -> Direction.values();
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
     * @see #addCustomWaypoint(int, SecretWaypoint.Category, Text, BlockPos)
     */
    protected void addCustomWaypoint(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
        int secretIndex = IntegerArgumentType.getInteger(context, "secretIndex");
        SecretWaypoint.Category category = SecretWaypoint.Category.CategoryArgumentType.getCategory(context, "category");
        Text waypointName = context.getArgument("name", Text.class);
        addCustomWaypoint(secretIndex, category, waypointName, pos);
        context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.stringifiedTranslatable("skyblocker.dungeons.secrets.customWaypointAdded", pos.getX(), pos.getY(), pos.getZ(), name, secretIndex, category, waypointName)));
    }

    /**
     * Adds a custom waypoint relative to this room to {@link DungeonManager#customWaypoints} and all existing instances of this room.
     *
     * @param secretIndex  the index of the secret waypoint
     * @param category     the category of the secret waypoint
     * @param waypointName the name of the secret waypoint
     * @param pos          the position of the secret waypoint relative to this room
     */
    @SuppressWarnings("JavadocReference")
    private void addCustomWaypoint(int secretIndex, SecretWaypoint.Category category, Text waypointName, BlockPos pos) {
        SecretWaypoint waypoint = new SecretWaypoint(secretIndex, category, waypointName, pos);
        DungeonManager.addCustomWaypoint(name, waypoint);
        DungeonManager.getRoomsStream().filter(r -> name.equals(r.getName())).forEach(r -> r.addCustomWaypoint(waypoint));
    }

    /**
     * Adds a custom waypoint relative to this room to this instance of the room.
     *
     * @param relativeWaypoint the secret waypoint relative to this room to add
     */
    private void addCustomWaypoint(SecretWaypoint relativeWaypoint) {
        SecretWaypoint actualWaypoint = relativeWaypoint.relativeToActual(this);
        secretWaypoints.put(actualWaypoint.secretIndex, actualWaypoint.pos, actualWaypoint);
    }

    /**
     * @see #removeCustomWaypoint(BlockPos)
     */
    protected void removeCustomWaypoint(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
        SecretWaypoint waypoint = removeCustomWaypoint(pos);
        if (waypoint != null) {
            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.customWaypointRemoved", pos.getX(), pos.getY(), pos.getZ(), name, waypoint.secretIndex, waypoint.category.asString(), waypoint.getName())));
        } else {
            context.getSource().sendFeedback(Constants.PREFIX.get().append(Text.translatable("skyblocker.dungeons.secrets.customWaypointNotFound", pos.getX(), pos.getY(), pos.getZ(), name)));
        }
    }

    /**
     * Removes a custom waypoint relative to this room from {@link DungeonManager#customWaypoints} and all existing instances of this room.
     *
     * @param pos the position of the secret waypoint relative to this room
     * @return the removed secret waypoint or {@code null} if there was no secret waypoint at the given position
     */
    @SuppressWarnings("JavadocReference")
    @Nullable
    private SecretWaypoint removeCustomWaypoint(BlockPos pos) {
        SecretWaypoint waypoint = DungeonManager.removeCustomWaypoint(name, pos);
        if (waypoint != null) {
            DungeonManager.getRoomsStream().filter(r -> name.equals(r.getName())).forEach(r -> r.removeCustomWaypoint(waypoint.secretIndex, pos));
        }
        return waypoint;
    }

    /**
     * Removes a custom waypoint relative to this room from this instance of the room.
     *
     * @param secretIndex the index of the secret waypoint
     * @param relativePos the position of the secret waypoint relative to this room
     */
    private void removeCustomWaypoint(int secretIndex, BlockPos relativePos) {
        BlockPos actualPos = relativeToActual(relativePos);
        secretWaypoints.remove(secretIndex, actualPos);
    }

    public <T extends Tickable & Renderable> void addSubProcess(T process) {
        tickables.add(process);
        renderables.add(process);
    }

    /**
     * Updates the room.
     * <p></p>
     * First, this method tries to find a wither door and blood door.
     * Then, this method returns immediately if any of the following conditions are met:
     * <ul>
     *     <li> The room does not need to be scanned and matched. (When the room is not of type {@link Type.ROOM}, {@link Type.PUZZLE}, or {@link Type.TRAP}. See {@link Type#needsScanning()}) </li>
     *     <li> The room has been matched or failed to match and is on cooldown. See {@link #matchState}. </li>
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
    @Override
    public void tick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        for (Tickable tickable : tickables) {
            tickable.tick(client);
        }

        // Room scanning and matching
        // Logical AND has higher precedence than logical OR
        if (!type.needsScanning() || matchState != MatchState.MATCHING && matchState != MatchState.DOUBLE_CHECKING || !DungeonManager.isRoomsLoaded() || findRoom != null && !findRoom.isDone()) {
            return;
        }
        ClientPlayerEntity player = client.player;
        if (player == null) {
            return;
        }
        findRoom = CompletableFuture.runAsync(() -> {
            for (BlockPos pos : BlockPos.iterate(player.getBlockPos().add(-5, -5, -5), player.getBlockPos().add(5, 5, 5))) {
                if (segments.contains(DungeonMapUtils.getPhysicalRoomPos(pos)) && notInDoorway(pos) && checkedBlocks.add(pos) && checkBlock(client.world, pos)) {
                    break;
                }
            }
        }).exceptionally(e -> {
            DungeonManager.LOGGER.error("[Skyblocker Dungeon Secrets] Encountered an unknown exception while matching room {}", this, e);
            return null;
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
     *     <li> Checks if the block type is included in the dungeon rooms data. See {@link DungeonManager#NUMERIC_ID}. </li>
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
     *         <li> Terminate matching by setting {@link #matchState} to {@link TriState#FALSE}. </li>
     *         <li> Schedule another matching attempt in 50 ticks (2.5 seconds). </li>
     *         <li> Reset {@link #possibleRooms} and {@link #checkedBlocks} with {@link #reset()}. </li>
     *         <li> Return {@code true} </li>
     *     </ul>
     *     <li> If there are exactly one room matching: </li>
     *     <ul>
     *         <li> If {@link #matchState} is {@link MatchState#MATCHING}: </li>
     *         <ul>
     *             <li> Call {@link #roomMatched()}. </li>
     *             <li> Return {@code false}. </li>
     *         </ul>
     *         <li> If {@link #matchState} is {@link MatchState#DOUBLE_CHECKING}: </li>
     *         <ul>
     *             <li> Set the match state to {@link MatchState#MATCHED}. </li>
     *             <li> Discard the no longer needed fields to save memory. </li>
     *             <li> Return {@code true}. </li>
     *         </ul>
     *     </ul>
     *     <li> Return {@code false} </li>
     * </ul>
     *
     * @param world the world to get the block from
     * @param pos   the position of the block to check
     * @return whether room matching should end. Either a match is found or there are no valid rooms left
     */
    protected boolean checkBlock(ClientWorld world, BlockPos pos) {
        byte id = DungeonManager.NUMERIC_ID.getByte(Registries.BLOCK.getId(world.getBlockState(pos).getBlock()).toString());
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
        if (matchingRoomsSize == 0) synchronized (this) {
            // If no rooms match, reset the fields and scan again after 50 ticks.
            matchState = MatchState.FAILED;
            DungeonManager.LOGGER.warn("[Skyblocker Dungeon Secrets] No dungeon room matched after checking {} block(s) including double checking {} block(s)", checkedBlocks.size(), doubleCheckBlocks);
            Scheduler.INSTANCE.schedule(() -> matchState = MatchState.MATCHING, 50);
            reset();
            return true;
        }
        else if (matchingRoomsSize == 1) {
            if (matchState == MatchState.MATCHING) {
                // If one room matches, load the secrets for that room and set state to double-checking.
                Triple<Direction, Vector2ic, List<String>> directionRoom = possibleRooms.stream().filter(directionRooms -> directionRooms.getRight().size() == 1).findAny().orElseThrow();
                name = directionRoom.getRight().getFirst();
                direction = directionRoom.getLeft();
                physicalCornerPos = directionRoom.getMiddle();
                DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} matched after checking {} block(s), starting double checking", name, checkedBlocks.size());
                roomMatched();
                return false;
            } else if (matchState == MatchState.DOUBLE_CHECKING && ++doubleCheckBlocks >= 10) {
                // If double-checked, set state to matched and discard the no longer needed fields.
                matchState = MatchState.MATCHED;
                DungeonEvents.ROOM_MATCHED.invoker().onRoomMatched(this);
                DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} confirmed after checking {} block(s) including double checking {} block(s)", name, checkedBlocks.size(), doubleCheckBlocks);
                discard();
                return true;
            }
            return false;
        } else {
            DungeonManager.LOGGER.debug("[Skyblocker Dungeon Secrets] {} room(s) remaining after checking {} block(s)", matchingRoomsSize, checkedBlocks.size());
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
    protected int posIdToInt(BlockPos pos, byte id) {
        return pos.getX() << 24 | pos.getY() << 16 | pos.getZ() << 8 | id;
    }

    /**
     * Loads the secret waypoints for the room from {@link DungeonManager#waypointsJson} once it has been matched
     * and sets {@link #matchState} to {@link MatchState#DOUBLE_CHECKING}.
     *
     * @param directionRooms the direction, position, and name of the room
     */
    @SuppressWarnings("JavadocReference")
    private void roomMatched() {
        secretWaypoints = HashBasedTable.create();
        List<DungeonManager.RoomWaypoint> roomWaypoints = DungeonManager.getRoomWaypoints(name);
        if (roomWaypoints != null) {
            for (DungeonManager.RoomWaypoint waypoint : roomWaypoints) {
                String secretName = waypoint.secretName();
                Matcher secretIndexMatcher = SECRET_INDEX.matcher(secretName);
                int secretIndex = secretIndexMatcher.find() ? Integer.parseInt(secretIndexMatcher.group(1)) : 0;
                BlockPos pos = DungeonMapUtils.relativeToActual(direction, physicalCornerPos, waypoint);
                secretWaypoints.put(secretIndex, pos, new SecretWaypoint(secretIndex, waypoint.category(), secretName, pos));
            }
        }
        DungeonManager.getCustomWaypoints(name).values().forEach(this::addCustomWaypoint);
        matchState = MatchState.DOUBLE_CHECKING;
    }

    /**
     * Resets fields for another round of matching after room matching fails.
     */
    protected void reset() {
        IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
        IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
        possibleRooms = getPossibleRooms(segmentsX, segmentsY);
        checkedBlocks = new HashSet<>();
        doubleCheckBlocks = 0;
        secretWaypoints = null;
        name = null;
        direction = null;
        physicalCornerPos = null;
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
    public BlockPos actualToRelative(BlockPos pos) {
        return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos);
    }

	/**
	 * Fails if !{@link #isMatched()}
	 */
	public Vec3d actualToRelative(Vec3d pos) {
		return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos);
	}

    /**
     * Fails if !{@link #isMatched()}
     */
    public BlockPos relativeToActual(BlockPos pos) {
        return DungeonMapUtils.relativeToActual(direction, physicalCornerPos, pos);
    }

	/**
	 * Fails if !{@link #isMatched()}
	 */
	public Vec3d relativeToActual(Vec3d pos) {
		return DungeonMapUtils.relativeToActual(direction, physicalCornerPos, pos);
	}

    /**
     * Calls {@link SecretWaypoint#extractRendering(PrimitiveCollector)} on {@link #secretWaypoints all secret waypoints} and renders a highlight around the wither or blood door, if it exists.
     */
    @Override
    public void extractRendering(PrimitiveCollector collector) {
        for (Renderable renderable : renderables) {
            renderable.extractRendering(collector);
        }

        synchronized (this) {
            if (SkyblockerConfigManager.get().dungeons.secretWaypoints.enableSecretWaypoints && isMatched()) {
                for (SecretWaypoint secretWaypoint : secretWaypoints.values()) {
                    if (secretWaypoint.shouldRender()) {
                        secretWaypoint.extractRendering(collector);
                    }
                }
            }
        }
    }

    /**
     * Sets {@link #lastChestSecret} as missing if message equals {@link #LOCKED_CHEST}.
     */
    protected void onChatMessage(String message) {
        if (LOCKED_CHEST.equals(message) && lastChestSecretTime + 1000 > System.currentTimeMillis() && lastChestSecret != null) {
            secretWaypoints.column(lastChestSecret).values().stream().filter(SecretWaypoint::needsInteraction).findAny()
                    .ifPresent(secretWaypoint -> markSecretsAndLogInfo(secretWaypoint, false, "[Skyblocker Dungeon Secrets] Detected locked chest interaction, setting secret #{} as missing", secretWaypoint.secretIndex));
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
     * Marks the secret at the interaction position as found when the player interacts with a chest, player head, or lever
     * if there is a secret at the interaction position and saves the position to {@link #lastChestSecret} if the block is a chest.
     *
     * @param world the world to get the block from
     * @param pos   the position of the block being interacted with
     * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
     */
    protected void onUseBlock(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if ((state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) && lastChestSecretTime + 1000 < System.currentTimeMillis() || state.isOf(Blocks.PLAYER_HEAD) || state.isOf(Blocks.PLAYER_WALL_HEAD)) {
            secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::needsInteraction).filter(SecretWaypoint::isEnabled).findAny()
                    .ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} interaction, setting secret #{} as found", secretWaypoint.category, secretWaypoint.secretIndex));
            if (state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST)) {
                lastChestSecret = pos;
                lastChestSecretTime = System.currentTimeMillis();
            }
        } else if (state.isOf(Blocks.LEVER)) {
            secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::isLever).forEach(SecretWaypoint::setFound);
        }
    }

    /**
     * Marks the closest secret that requires item pickup no greater than 6 blocks away as found when a secret item is removed from the world.
     *
     * @param itemEntity the item entity being picked up
     * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
     */
    protected void onItemPickup(ItemEntity itemEntity) {
        if (SecretWaypoint.SECRET_ITEMS.stream().noneMatch(itemEntity.getStack().getName().getString()::contains)) {
            return;
        }
        secretWaypoints.values().stream().filter(SecretWaypoint::needsItemPickup).min(Comparator.comparingDouble(SecretWaypoint.getSquaredDistanceToFunction(itemEntity))).filter(SecretWaypoint.getRangePredicate(itemEntity))
                .ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected item {} removed from a {} secret, setting secret #{} as found", itemEntity.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
    }

    /**
     * Marks the closest bat secret as found when a bat is killed.
     *
     * @param bat the bat being killed
     * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
     */
    protected void onBatRemoved(AmbientEntity bat) {
        secretWaypoints.values().stream().filter(SecretWaypoint::isBat).min(Comparator.comparingDouble(SecretWaypoint.getSquaredDistanceToFunction(bat))).filter(SecretWaypoint.getRangePredicate(bat))
                .ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} killed for a {} secret, setting secret #{} as found", bat.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
    }

    /**
     * Marks all secret waypoints with the same index as the given {@link SecretWaypoint} as found and logs the given message.
     *
     * @param secretWaypoint the secret waypoint to read the index from.
     * @param msg            the message to log
     * @param args           the args for the {@link org.slf4j.Logger#info(String, Object...) Logger#info(String, Object...)} call
     */
    private void markSecretsFoundAndLogInfo(SecretWaypoint secretWaypoint, String msg, Object... args) {
        markSecretsAndLogInfo(secretWaypoint, true, msg, args);
    }

    /**
     * Marks all secret waypoints with the same index as the given {@link SecretWaypoint} as found or missing and logs the given message.
     *
     * @param secretWaypoint the secret waypoint to read the index from.
     * @param found          whether to mark the secret as found or missing
     * @param msg            the message to log
     * @param args           the args for the {@link org.slf4j.Logger#info(String, Object...) Logger#info(String, Object...)} call
     */
    private void markSecretsAndLogInfo(SecretWaypoint secretWaypoint, boolean found, String msg, Object... args) {
        markSecrets(secretWaypoint.secretIndex, found);
        DungeonManager.LOGGER.info(msg, args);
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

    protected void markAllSecrets(boolean found) {
    	//Prevent a crash if this runs before the room is matched or something
    	if (secretWaypoints == null) return;
        secretWaypoints.values().forEach(found ? SecretWaypoint::setFound : SecretWaypoint::setMissing);
    }

    protected int getSecretCount() {
        return secretWaypoints.rowMap().size();
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
				case ROOM, PUZZLE, TRAP, MINIBOSS -> true;
                default -> false;
            };
        }
    }

    public enum Shape {
        ONE_BY_ONE("1x1"),
        ONE_BY_TWO("1x2"),
        ONE_BY_THREE("1x3"),
        ONE_BY_FOUR("1x4"),
        L_SHAPE("L-shape"),
        TWO_BY_TWO("2x2"),
        PUZZLE("puzzle"),
        TRAP("trap"),
		MINIBOSS("miniboss");
        final String shape;

        Shape(String shape) {
            this.shape = shape;
        }

        @Override
        public String toString() {
            return shape;
        }
    }

    public enum Direction implements StringIdentifiable {
        NW("northwest"), NE("northeast"), SW("southwest"), SE("southeast");
        private static final Codec<Direction> CODEC = StringIdentifiable.createCodec(Direction::values);
        private final String name;

        Direction(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }

        static class DirectionArgumentType extends EnumArgumentType<Direction> {
            DirectionArgumentType() {
                super(CODEC, Direction::values);
            }

            static DirectionArgumentType direction() {
                return new DirectionArgumentType();
            }

            static <S> Direction getDirection(CommandContext<S> context, String name) {
                return context.getArgument(name, Direction.class);
            }
        }
    }

    protected enum MatchState {
        MATCHING, DOUBLE_CHECKING, MATCHED, FAILED
    }
}
