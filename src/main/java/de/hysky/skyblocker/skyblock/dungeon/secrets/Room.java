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
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import it.unimi.dsi.fastutil.ints.IntRBTreeSet;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import it.unimi.dsi.fastutil.ints.IntSortedSets;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.StringRepresentableArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ambient.AmbientCreature;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Room implements Tickable, Renderable {
	public static final Pattern SECRET_INDEX = Pattern.compile("^(\\d+)");
	private static final Pattern SECRETS = Pattern.compile("§7(\\d{1,2})/(\\d{1,2}) Secrets");
	private static final String CHEST_ALREADY_OPENED = "This chest has already been searched!";
	protected static final float[] RED_COLOR_COMPONENTS = {1, 0, 0};
	protected static final float[] GREEN_COLOR_COMPONENTS = {0, 1, 0};
	private final Type type;
	final Set<Vector2ic> segments;
	/**
	 * The current state of the room as it appears on the map.
	 */
	public ClearState clearState = ClearState.UNCLEARED;

	private int maxSecrets = -1;
	protected int secretsFound = 0;

	public boolean secretCountOutdated = true;

	/**
	 * The shape of the room. See {@link #determineShape(IntSortedSet, IntSortedSet)}.
	 */
	private final Shape shape;
	/**
	 * The room data containing all rooms for a specific dungeon and {@link #shape}.
	 * This is null after the room is matched.
	 */
	protected @Nullable Map<String, int[]> roomsData;
	/**
	 * Contains all possible dungeon rooms for this room. The list is gradually shrunk by checking blocks until only one room is left.
	 * This is null after the room is matched.
	 */
	protected @Nullable List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms;

	/**
	 * Contains all blocks that have been checked to prevent checking the same block multiple times.
	 * This is null after the room is matched.
	 */
	private @Nullable Set<BlockPos> checkedBlocks = new HashSet<>();
	/**
	 * The task that is used to check blocks. This is used to ensure only one such task can run at a time.
	 */
	protected @Nullable CompletableFuture<Void> findRoom;
	private int doubleCheckBlocks;
	/**
	 * Represents the matching state of the room with the following possible values:
	 * <li>{@link MatchState#MATCHING} means that the room has not been checked, is being processed, or does not {@link Type#needsScanning() need to be processed}.</li>
	 * <li>{@link MatchState#DOUBLE_CHECKING} means that the room has a unique match and is being double-checked.</li>
	 * <li>{@link MatchState#MATCHED} means that the room has a unique match and has been double-checked.</li>
	 * <li>{@link MatchState#FAILED} means that the room has been checked and there is no match.</li>
	 */
	protected MatchState matchState = MatchState.MATCHING;
	protected Table<Integer, BlockPos, SecretWaypoint> secretWaypoints = HashBasedTable.create();
	protected @Nullable String name;
	protected @Nullable Direction direction;
	protected @Nullable Vector2ic physicalCornerPos;

	protected List<Tickable> tickables = new ArrayList<>();
	protected List<Renderable> renderables = new ArrayList<>();
	private @Nullable BlockPos lastChestSecret;
	private long lastChestSecretTime;
	boolean fromWebsocket = false;

	public Room(Type type, Vector2ic... physicalPositions) {
		this.type = type;
		segments = Set.of(physicalPositions);
		IntSortedSet segmentsX = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::x).toArray()));
		IntSortedSet segmentsY = IntSortedSets.unmodifiable(new IntRBTreeSet(segments.stream().mapToInt(Vector2ic::y).toArray()));
		shape = determineShape(segmentsX, segmentsY);
		roomsData = DungeonManager.ROOMS_DATA.getOrDefault("catacombs", Collections.emptyMap()).getOrDefault(shape.shape.toLowerCase(Locale.ENGLISH), Collections.emptyMap());
		possibleRooms = getPossibleRooms(segmentsX, segmentsY);
	}

	// Room from WS
	Room(Type type, Shape shape, Direction direction, String roomName, Set<Vector2ic> segments, IntSortedSet segmentsX, IntSortedSet segmentsY) {
		fromWebsocket = true;
		this.type = type;
		this.shape = shape;
		this.segments = segments;
		this.name = roomName;
		this.direction = direction;
		this.physicalCornerPos = DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY);

		roomsData = DungeonManager.ROOMS_DATA.getOrDefault("catacombs", Collections.emptyMap()).getOrDefault(shape.shape.toLowerCase(Locale.ENGLISH), Collections.emptyMap());
		roomMatched();
		matchState = MatchState.MATCHED;
		DungeonEvents.ROOM_MATCHED.invoker().onRoomMatched(this);
		discard();
	}

	public Type getType() {
		return type;
	}

	public Set<Vector2ic> getSegments() {
		return segments;
	}

	public Shape getShape() {
		return shape;
	}

	public @Nullable Vector2ic getPhysicalCornerPos() {
		return physicalCornerPos;
	}

	public boolean isMatched() {
		// This technically isn't very thread safe but should be fine
		return matchState == MatchState.DOUBLE_CHECKING || matchState == MatchState.MATCHED;
	}

	/**
	 * Not null if {@link #isMatched()}.
	 */
	public @Nullable String getName() {
		return name;
	}

	/**
	 * Not null if {@link #isMatched()}.
	 */
	public @Nullable Direction getDirection() {
		return direction;
	}

	@Override
	public String toString() {
		return "Room{type=%s, segments=%s, shape=%s, matchState=%s, name=%s, direction=%s, physicalCornerPos=%s}".formatted(type, Arrays.toString(segments.toArray()), shape, matchState, name, direction, physicalCornerPos);
	}

	private Shape determineShape(IntSortedSet segmentsX, IntSortedSet segmentsY) {
		return determineShape(type, segments, segmentsX, segmentsY);
	}

	protected static Shape determineShape(Type type, Set<Vector2ic> segments, IntSortedSet segmentsX, IntSortedSet segmentsY) {
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
		if (roomsData == null) return List.of();
		List<String> possibleDirectionRooms = new ArrayList<>(roomsData.keySet());
		List<MutableTriple<Direction, Vector2ic, List<String>>> possibleRooms = new ArrayList<>();
		for (Direction direction : getPossibleDirections(segmentsX, segmentsY)) {
			possibleRooms.add(MutableTriple.of(direction, DungeonMapUtils.getPhysicalCornerPos(direction, segmentsX, segmentsY), possibleDirectionRooms));
		}
		return possibleRooms;
	}

	protected Direction[] getPossibleDirections(IntSortedSet segmentsX, IntSortedSet segmentsY) {
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
	 * @see #addCustomWaypoint(int, SecretWaypoint.Category, Component, BlockPos)
	 */
	protected void addCustomWaypoint(CommandContext<FabricClientCommandSource> context, BlockPos pos) {
		int secretIndex = IntegerArgumentType.getInteger(context, "secretIndex");
		SecretWaypoint.Category category = SecretWaypoint.Category.CategoryArgumentType.getCategory(context, "category");
		Component waypointName = context.getArgument("name", Component.class);
		addCustomWaypoint(secretIndex, category, waypointName, pos);
		context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatableEscape("skyblocker.dungeons.secrets.customWaypointAdded", pos.getX(), pos.getY(), pos.getZ(), name, secretIndex, category, waypointName)));
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
	private void addCustomWaypoint(int secretIndex, SecretWaypoint.Category category, Component waypointName, BlockPos pos) {
		if (!isMatched()) return;
		SecretWaypoint waypoint = new SecretWaypoint(secretIndex, category, waypointName, pos);
		//noinspection DataFlowIssue - room is matched
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
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.customWaypointRemoved", pos.getX(), pos.getY(), pos.getZ(), name, waypoint.secretIndex, waypoint.category.getSerializedName(), waypoint.getName())));
		} else {
			context.getSource().sendFeedback(Constants.PREFIX.get().append(Component.translatable("skyblocker.dungeons.secrets.customWaypointNotFound", pos.getX(), pos.getY(), pos.getZ(), name)));
		}
	}

	/**
	 * Removes a custom waypoint relative to this room from {@link DungeonManager#customWaypoints} and all existing instances of this room.
	 *
	 * @param pos the position of the secret waypoint relative to this room
	 * @return the removed secret waypoint or {@code null} if there was no secret waypoint at the given position
	 */
	@SuppressWarnings("JavadocReference")
	private @Nullable SecretWaypoint removeCustomWaypoint(BlockPos pos) {
		if (name == null) return null;
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
	 *     <li> Calls {@link #checkBlock(ClientLevel, BlockPos)} </li>
	 * </ul>
	 */
	@SuppressWarnings("JavadocReference")
	@Override
	public void tick(Minecraft client) {
		if (client.level == null) {
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
		LocalPlayer player = client.player;
		if (player == null) {
			return;
		}
		findRoom = CompletableFuture.runAsync(() -> {
			for (BlockPos pos : BlockPos.betweenClosed(player.blockPosition().offset(-5, -5, -5), player.blockPosition().offset(5, 5, 5))) {
				assert checkedBlocks != null;
				if (segments.contains(DungeonMapUtils.getPhysicalRoomPos(pos)) && notInDoorway(pos) && checkedBlocks.add(pos) && checkBlock(client.level, pos)) {
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
	protected boolean checkBlock(ClientLevel world, BlockPos pos) {
		byte id = DungeonManager.NUMERIC_ID.getByte(BuiltInRegistries.BLOCK.getKey(world.getBlockState(pos).getBlock()).toString());
		if (id == 0) {
			return false;
		}
		assert possibleRooms != null;
		for (MutableTriple<Direction, Vector2ic, List<String>> directionRooms : possibleRooms) {
			int block = posIdToInt(DungeonMapUtils.actualToRelative(directionRooms.getLeft(), directionRooms.getMiddle(), pos), id);
			List<String> possibleDirectionRooms = new ArrayList<>();
			for (String room : directionRooms.getRight()) {
				assert roomsData != null;
				if (Arrays.binarySearch(roomsData.get(room), block) >= 0) {
					possibleDirectionRooms.add(room);
				}
			}
			directionRooms.setRight(possibleDirectionRooms);
		}

		int matchingRoomsSize = possibleRooms.stream().map(Triple::getRight).mapToInt(Collection::size).sum();
		if (matchingRoomsSize == 0) {
			// If no rooms match, reset the fields and scan again after 50 ticks.
			matchState = MatchState.FAILED;
			assert checkedBlocks != null;
			DungeonManager.LOGGER.warn("[Skyblocker Dungeon Secrets] No dungeon room matched after checking {} block(s) including double checking {} block(s)", checkedBlocks.size(), doubleCheckBlocks);
			RenderHelper.runOnRenderThread(() -> {
				Scheduler.INSTANCE.schedule(() -> matchState = MatchState.MATCHING, 50);
				reset();
			});
			return true;
		} else if (matchingRoomsSize == 1) {
			if (matchState == MatchState.MATCHING) {
				// If one room matches, load the secrets for that room and set state to double-checking.
				matchState = MatchState.DOUBLE_CHECKING;
				Triple<Direction, Vector2ic, List<String>> directionRoom = possibleRooms.stream().filter(directionRooms -> directionRooms.getRight().size() == 1).findAny().orElseThrow();
				name = directionRoom.getRight().getFirst();
				direction = directionRoom.getLeft();
				physicalCornerPos = directionRoom.getMiddle();
				assert checkedBlocks != null;
				DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} matched after checking {} block(s), starting double checking", name, checkedBlocks.size());
				RenderHelper.runOnRenderThread(this::roomMatched);
				return false;
			} else if (matchState == MatchState.DOUBLE_CHECKING && ++doubleCheckBlocks >= 10) {
				// If double-checked, set state to matched and discard the no longer needed fields.
				matchState = MatchState.MATCHED;
				assert checkedBlocks != null;
				DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Room {} confirmed after checking {} block(s) including double checking {} block(s)", name, checkedBlocks.size(), doubleCheckBlocks);
				RenderHelper.runOnRenderThread(() -> {
					DungeonEvents.ROOM_MATCHED.invoker().onRoomMatched(this);
					discard();
				});
				return true;
			}
			return false;
		} else {
			assert checkedBlocks != null;
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
		// Save fields and null check for thread safety
		String name = this.name;
		Direction direction = this.direction;
		Vector2ic physicalCornerPos = this.physicalCornerPos;
		if (name == null || direction == null || physicalCornerPos == null) {
			DungeonManager.LOGGER.warn("[Skyblocker Dungeon Secrets] Room matched called with invalid fields: matchState={}, name={}, direction={}, physicalCornerPos={}", matchState, name, direction, physicalCornerPos);
			return;
		}

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

		// Calculate max secrets based off room id, surely this isn't going to be wrong for some rooms
		this.maxSecrets = Utils.parseInt(name.replaceAll("\\D", "")).orElse(-1);
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
		secretWaypoints.clear();
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
		assert direction != null && physicalCornerPos != null;
		return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos);
	}

	/**
	 * Fails if !{@link #isMatched()}
	 */
	public Vec3 actualToRelative(Vec3 pos) {
		assert direction != null && physicalCornerPos != null;
		return DungeonMapUtils.actualToRelative(direction, physicalCornerPos, pos);
	}

	/**
	 * Fails if !{@link #isMatched()}
	 */
	public BlockPos relativeToActual(BlockPos pos) {
		assert direction != null && physicalCornerPos != null;
		return DungeonMapUtils.relativeToActual(direction, physicalCornerPos, pos);
	}

	/**
	 * Fails if !{@link #isMatched()}
	 */
	public Vec3 relativeToActual(Vec3 pos) {
		assert direction != null && physicalCornerPos != null;
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

		if (SkyblockerConfigManager.get().dungeons.secretWaypoints.enableSecretWaypoints && isMatched()) {
			for (SecretWaypoint secretWaypoint : secretWaypoints.values()) {
				if (secretWaypoint.shouldRender()) {
					secretWaypoint.extractRendering(collector);
				}
			}
		}
	}

	/**
	 * Marks {@link #lastChestSecret} as found if message equals {@link #CHEST_ALREADY_OPENED}.
	 */
	protected void onChatMessage(String message) {
		if (CHEST_ALREADY_OPENED.equals(message) && lastChestSecretTime + 1000 > System.currentTimeMillis() && lastChestSecret != null) {
			secretWaypoints.column(lastChestSecret).values().stream().filter(SecretWaypoint::needsInteraction).findAny()
					.ifPresent(secretWaypoint -> {
						markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected already searched chest interaction, setting secret #{} as found", secretWaypoint.secretIndex);
					});
		}
		if (secretCountOutdated) updateSecretCount(message);
	}

	protected void updateSecretCount(String message) {
		Matcher matcher = SECRETS.matcher(message);
		if (!matcher.find()) return;
		secretsFound = Integer.parseInt(matcher.group(1));
		secretCountOutdated = false;
		DungeonEvents.SECRET_COUNT_UPDATED.invoker().onSecretCountUpdate(this, false);
	}

	/**
	 * Marks the secret at the interaction position as found when the player interacts with a player head or lever.<br>
	 * Chest secrets are only marked as found here if the block disappears (Mimic). Otherwise, chests are handled in {@link #onChestOpened(BlockPos)} and {@link #onChatMessage(String)}.
	 *
	 * @param world the world to get the block from
	 * @param pos   the position of the block being interacted with
	 * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
	 */
	protected void onUseBlock(Level world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.is(Blocks.CHEST) || state.is(Blocks.TRAPPED_CHEST)) {
			lastChestSecret = pos;
			lastChestSecretTime = System.currentTimeMillis();
			Scheduler.INSTANCE.schedule(() -> {
				if (!world.getBlockState(pos).isAir()) return;
				secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::needsInteraction).filter(SecretWaypoint::isEnabled).findAny()
						.ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected chest block removed, setting secret #{} as found", secretWaypoint.secretIndex));
			}, 5);
		} else if (state.is(Blocks.PLAYER_HEAD) || state.is(Blocks.PLAYER_WALL_HEAD)) {
			secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::needsInteraction).filter(SecretWaypoint::isEnabled).findAny()
					.ifPresent(secretWaypoint -> {
						if (secretWaypoint.category == SecretWaypoint.Category.REDSTONE_KEY) {
							DungeonManager.LOGGER.info("[Skyblocker Dungeon Secrets] Detected {} interaction, hiding secret #{} waypoint {}", secretWaypoint.category, secretWaypoint.secretIndex, secretWaypoint.name);
							secretWaypoint.setFound();
							return;
						}
						markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} interaction, setting secret #{} as found", secretWaypoint.category, secretWaypoint.secretIndex);
					});
		} else if (state.is(Blocks.REDSTONE_BLOCK)) {
			secretWaypoints.column(pos.above()).values().stream().filter(SecretWaypoint::needsInteraction).filter(SecretWaypoint::isEnabled).findAny()
					.ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected {} interaction, setting secret #{} as found", secretWaypoint.category, secretWaypoint.secretIndex));
		} else if (state.is(Blocks.LEVER)) {
			secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::isLever).forEach(SecretWaypoint::setFound);
		}
	}

	/**
	 * Marks the chest at the position as found.
	 */
	protected void onChestOpened(BlockPos pos) {
		secretWaypoints.column(pos).values().stream().filter(SecretWaypoint::needsInteraction).filter(SecretWaypoint::isEnabled).findAny()
				.ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected chest opened, setting secret #{} as found", secretWaypoint.secretIndex));
	}

	/**
	 * Marks the closest secret that requires item pickup no greater than 6 blocks away as found when a secret item is removed from the world.
	 *
	 * @param itemEntity the item entity being picked up
	 * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
	 */
	protected void onItemPickup(ItemEntity itemEntity) {
		if (SecretWaypoint.SECRET_ITEMS.stream().noneMatch(itemEntity.getItem().getHoverName().getString()::contains)) return;
		secretWaypoints.values().stream().filter(SecretWaypoint::needsItemPickup).min(Comparator.comparingDouble(SecretWaypoint.getSquaredDistanceToFunction(itemEntity))).filter(SecretWaypoint.getRangePredicate(itemEntity))
				.ifPresent(secretWaypoint -> markSecretsFoundAndLogInfo(secretWaypoint, "[Skyblocker Dungeon Secrets] Detected item {} removed from a {} secret, setting secret #{} as found", itemEntity.getName().getString(), secretWaypoint.category, secretWaypoint.secretIndex));
	}

	/**
	 * Marks the closest bat secret as found when a bat is killed.
	 *
	 * @param bat the bat being killed
	 * @see #markSecretsFoundAndLogInfo(SecretWaypoint, String, Object...)
	 */
	protected void onBatRemoved(AmbientCreature bat) {
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
		if (found) {
			DungeonEvents.SECRET_FOUND.invoker().onSecretFound(this, secretWaypoint);
			secretCountOutdated = true;
		}
		markSecrets(secretWaypoint.secretIndex, found);
		DungeonManager.LOGGER.info(msg, args);
	}

	protected int getIndexByWaypointHash(int waypointHash) {
		for (int i = 0; i < getSecretCount(); i++) {
			if (!secretWaypoints.containsRow(i)) continue;
			for (SecretWaypoint waypoint : secretWaypoints.row(i).values()) {
				if (!waypoint.isEnabled()) continue;
				if (waypoint.hashCode() == waypointHash) return i;
			}
		}
		return -1;
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
		secretWaypoints.values().forEach(found ? SecretWaypoint::setFound : SecretWaypoint::setMissing);
	}

	public int getMaxSecretCount() {
		return maxSecrets;
	}

	protected int getSecretCount() {
		return secretWaypoints.rowMap().size();
	}

	public int getFoundSecretCount() {
		return secretsFound;
	}

	public enum Type implements StringRepresentable {
		ENTRANCE(MapColor.PLANT.getPackedId(MapColor.Brightness.HIGH), "Entrance"),
		ROOM(MapColor.COLOR_ORANGE.getPackedId(MapColor.Brightness.LOWEST), "Room"),
		PUZZLE(MapColor.COLOR_MAGENTA.getPackedId(MapColor.Brightness.HIGH), "Puzzle"),
		TRAP(MapColor.COLOR_ORANGE.getPackedId(MapColor.Brightness.HIGH), "Trap"),
		MINIBOSS(MapColor.COLOR_YELLOW.getPackedId(MapColor.Brightness.HIGH), "Miniboss"),
		FAIRY(MapColor.COLOR_PINK.getPackedId(MapColor.Brightness.HIGH), "Fairy"),
		BLOOD(MapColor.FIRE.getPackedId(MapColor.Brightness.HIGH), "Blood"),
		UNKNOWN(MapColor.COLOR_GRAY.getPackedId(MapColor.Brightness.NORMAL), "Unknown");

		public final byte color;
		final String name;

		public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

		Type(byte color, String name) {
			this.color = color;
			this.name = name;
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

		@Override
		public String getSerializedName() {
			return name;
		}
	}

	public enum Shape implements StringRepresentable {
		ONE_BY_ONE("1x1"),
		ONE_BY_TWO("1x2"),
		ONE_BY_THREE("1x3"),
		ONE_BY_FOUR("1x4"),
		L_SHAPE("L-shape"),
		TWO_BY_TWO("2x2"),
		PUZZLE("puzzle"),
		TRAP("trap"),
		MINIBOSS("miniboss");
		public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);
		final String shape;

		Shape(String shape) {
			this.shape = shape;
		}

		@Override
		public String toString() {
			return shape;
		}

		@Override
		public String getSerializedName() {
			return shape;
		}
	}

	public enum Direction implements StringRepresentable {
		NW("northwest"), NE("northeast"), SW("southwest"), SE("southeast");
		public static final Codec<Direction> CODEC = StringRepresentable.fromEnum(Direction::values);
		private final String name;

		Direction(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}

		static class DirectionArgumentType extends StringRepresentableArgument<Direction> {
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

	public enum ClearState {
		GREEN_CHECKED, WHITE_CHECKED, FAILED, UNCLEARED
	}

	protected enum MatchState {
		MATCHING, DOUBLE_CHECKING, MATCHED, FAILED
	}
}
