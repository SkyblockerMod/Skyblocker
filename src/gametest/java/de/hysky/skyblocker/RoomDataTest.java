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
	private static boolean isValid = true;

	@Override
	public void runTest(ClientGameTestContext clientGameTestContext) {
		clientGameTestContext.waitFor((client) -> DungeonManager.isRoomsLoaded());
		clientGameTestContext.runOnClient(client -> {
			int roomCount = checkRooms();
			if (!isValid) throw new AssertionError(".skeleton and .room files do not match!");
			checkIfLoadedCorrectly(roomCount);
		});
	}

	public int checkRooms() {
		MinecraftClient client = MinecraftClient.getInstance();
		List<String> skeletonFiles = getRoomFilesByType(client, ".skeleton");
		List<String> roomFiles = getRoomFilesByType(client, ".json");

		LOGGER.info("Found {} .skeleton files and {} .json files!", skeletonFiles.size(), roomFiles.size());

		for (String roomName : skeletonFiles) {
			if (!roomFiles.contains(roomName)) {
				isValid = false;
				LOGGER.error("{} is missing a .json file!", roomName);
			}
		}

		for (String roomName : roomFiles) {
			if (!skeletonFiles.contains(roomName)) {
				isValid = false;
				LOGGER.error("{} is mising a .skeleton file!", roomName);
			}
		}

		return roomFiles.size();
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
