package de.hysky.skyblocker.skyblock.dungeon.roomPreview;

import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonMapUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.skyblock.dungeon.secrets.SecretWaypoint;
import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector2i;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

public class PreviewRoom extends Room {
	private static final Vector2i ORIGIN = new Vector2i(0, 0);
	private static final Vector2i OFFSET = new Vector2i(-8, -8);

	public PreviewRoom(String name) {
		super(Type.ROOM, ORIGIN, OFFSET);
		this.matchState = MatchState.MATCHED;
		this.name = name;
		this.direction = Direction.NW;
		this.physicalCornerPos = ORIGIN;
		loadSecrets();
	}

	@Override
	protected Direction[] getPossibleDirections(IntSortedSet segmentsX, IntSortedSet segmentsY) {
		return new Direction[]{Direction.NW};
	}

	protected void loadSecrets() {
		List<DungeonManager.RoomWaypoint> roomWaypoints = DungeonManager.getRoomWaypoints(RoomPreviewServer.selectedRoom);
		if (roomWaypoints != null) {
			roomWaypoints.forEach(waypoint -> {
				String secretName = waypoint.secretName();
				Matcher secretIndexMatcher = SECRET_INDEX.matcher(secretName);
				int secretIndex = secretIndexMatcher.find() ? Integer.parseInt(secretIndexMatcher.group(1)) : 0;
				BlockPos pos = DungeonMapUtils.relativeToActual(getDirection(), getPhysicalCornerPos(), waypoint);
				SecretWaypoint secretWaypoint = new SecretWaypoint(secretIndex, waypoint.category(), secretName, pos);
				secretWaypoints.put(secretIndex, pos, secretWaypoint);
				showSecretInWorld(secretWaypoint);
			});
		}

		Map<BlockPos, SecretWaypoint> customWaypoints = DungeonManager.getCustomWaypoints(RoomPreviewServer.selectedRoom);
		customWaypoints.forEach((pos, secretWaypoint) -> {
			secretWaypoints.put(secretWaypoint.secretIndex, pos, secretWaypoint);
		});
	}

	protected void showSecretInWorld(SecretWaypoint waypoint) {
		IntegratedServer server = MinecraftClient.getInstance().getServer();
		if (server == null) return;
		ServerWorld world = server.getOverworld();

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
		server.execute(() -> world.setBlockState(waypoint.pos, block.getDefaultState()));
	}
}
