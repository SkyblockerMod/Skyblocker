package de.hysky.skyblocker;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;

import java.util.List;

import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager.DUNGEONS_PATH;

@SuppressWarnings("UnstableApiUsage")
public class RoomDataTest implements FabricClientGameTest {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void runTest(ClientGameTestContext clientGameTestContext) {
		clientGameTestContext.waitFor((client) -> DungeonManager.isRoomsLoaded());
		clientGameTestContext.runOnClient(this::testMain);
	}

	public void testMain(MinecraftClient client) {
		List<String> skeletonFiles = getRoomFilesByType(client, ".skeleton");
		List<String> roomFiles = getRoomFilesByType(client, ".json");
		LOGGER.info("Found {} .skeleton files and {} .json files!", skeletonFiles.size(), roomFiles.size());

		boolean isValid = checkRooms(skeletonFiles, roomFiles);
		if (!isValid) throw new AssertionError("One or more rooms have an issue!");

		int roomCount = skeletonFiles.size();
		checkIfLoadedCorrectly(roomCount);
	}

	public boolean checkRooms(List<String> skeletonFiles, List<String> roomFiles) {
		boolean isValid = true;
		for (String roomName : skeletonFiles) {
			if (!roomFiles.contains(roomName)) {
				isValid = false;
				LOGGER.error("{} is missing a .json file!", roomName);
			}

			if (DungeonManager.getRoomMetadata(roomName) == null) {
				isValid = false;
				LOGGER.error("{} is missing room metadata!", roomName);
			}

			if (DungeonManager.getRoomWaypoints(roomName) == null) {
				isValid = false;
				LOGGER.error("{} is missing waypoints!", roomName);
			}
		}

		for (String roomName : roomFiles) {
			if (!skeletonFiles.contains(roomName)) {
				isValid = false;
				LOGGER.error("{} is mising a .skeleton file!", roomName);
			}
		}
		return isValid;
	}

	public void checkIfLoadedCorrectly(int expected) {
		int count = DungeonManager.getLoadedRoomCount();
		if (count != expected) {
			throw new AssertionError(String.format("Expected %s rooms but only %s was loaded", expected, count));
		}
	}

	List<String> getRoomFilesByType(MinecraftClient client, String fileType) {
		return client.getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(fileType))
				.keySet().stream().map(identifier -> identifier.getPath().split("/"))
				.filter(path -> path.length == 4).map(path -> path[3].replace(fileType, "")).toList();
	}
}
