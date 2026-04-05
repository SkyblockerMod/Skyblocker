package de.hysky.skyblocker.skyblock.dungeon.preview;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2i;
import org.joml.Vector2ic;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class PreviewRoom extends Room {
	private static final Vector2ic ORIGIN = new Vector2i(0, 0);

	public PreviewRoom(String name) {
		super(Type.ROOM, ORIGIN);
		this.direction = Direction.NW;
		this.matchState = MatchState.MATCHED;
		this.name = name;
		this.physicalCornerPos = ORIGIN;
		loadSecrets();
	}

	@Override
	protected Direction[] getPossibleDirections(IntSortedSet segmentsX, IntSortedSet segmentsY) {
		return new Direction[]{Direction.NW};
	}

	@Override
	protected void onUseBlock(Level level, BlockPos pos) {
		// Fix Scheduler being called from the Server thread
		if (!level.isClientSide()) return;
		super.onUseBlock(level, pos);
	}

	protected void loadSecrets() {
		List<DungeonManager.RoomWaypoint> roomWaypoints = DungeonManager.getRoomWaypoints(RoomPreviewServer.selectedRoom);
		if (roomWaypoints != null) {
			roomWaypoints.forEach(waypoint -> {
				String secretName = waypoint.secretName();
				Matcher secretIndexMatcher = SECRET_INDEX.matcher(secretName);
				int secretIndex = secretIndexMatcher.find() ? Integer.parseInt(secretIndexMatcher.group(1)) : 0;
				//noinspection DataFlowIssue - won't be null, it is set in the constructor.
				BlockPos pos = DungeonMapUtils.relativeToActual(getDirection(), getPhysicalCornerPos(), waypoint);
				SecretWaypoint secretWaypoint = new SecretWaypoint(secretIndex, waypoint.category(), secretName, pos);
				secretWaypoints.put(secretIndex, pos, secretWaypoint);
				showSecretInLevel(secretWaypoint);
			});
		}

		Map<BlockPos, SecretWaypoint> customWaypoints = DungeonManager.getCustomWaypoints(RoomPreviewServer.selectedRoom);
		customWaypoints.forEach((pos, secretWaypoint) -> secretWaypoints.put(secretWaypoint.secretIndex, pos, secretWaypoint));
	}

	protected void showSecretInLevel(SecretWaypoint waypoint) {
		IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
		if (server == null) return;
		ServerLevel level = server.overworld();

		Block block = switch (waypoint.category) {
			case ENTRANCE, STONK, AOTV, PEARL, PRINCE, DEFAULT -> null;
			case BAT -> Blocks.GREEN_STAINED_GLASS;
			case CHEST -> Blocks.CHEST;
			case FAIRYSOUL -> Blocks.PINK_STAINED_GLASS;
			case ITEM -> Blocks.BLUE_STAINED_GLASS;
			case LEVER -> Blocks.LEVER;
			case SUPERBOOM -> Blocks.TNT;
			case WITHER, REDSTONE_KEY -> Blocks.PLAYER_HEAD;
		};

		if (block == null) return;
		BlockState state = block.defaultBlockState();
		server.execute(() -> {
			level.setBlockAndUpdate(waypoint.pos, state);
			level.updateNeighboursOnBlockSet(waypoint.pos, state);
		});
	}
}
