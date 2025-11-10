package de.hysky.skyblocker;

import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.JsonOps;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.util.List;

import static de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager.DUNGEONS_PATH;

@SuppressWarnings("UnstableApiUsage")
public class RoomDataTest implements FabricClientGameTest {
	private static final Logger LOGGER = LogUtils.getLogger();

	@Override
	public void runTest(ClientGameTestContext clientGameTestContext) {
		clientGameTestContext.waitFor((client) -> DungeonManager.isRoomsLoaded());
		clientGameTestContext.waitTicks(100);
		clientGameTestContext.runOnClient(this::testMain);
	}

	public void testMain(MinecraftClient client) {
		List<String> skeletonFiles = getRoomFilesByType(client, ".skeleton");
		List<String> roomFiles = getRoomFilesByType(client, ".json");
		LOGGER.info("Found {} .skeleton files and {} .json files!", skeletonFiles.size(), roomFiles.size());

		if (!checkRooms(skeletonFiles, roomFiles))
			throw new AssertionError("There are missing .skeleton or .json files!");

		if (!checkRoomJson(client))
			throw new AssertionError("There are invalid room .json files!");

		int roomCount = skeletonFiles.size();
		checkIfLoadedCorrectly(roomCount);
	}

	/**
	 * Ensures that for each .skeleton file there is an associated .json file, and vice versa.
	 */
	public boolean checkRooms(List<String> skeletonFiles, List<String> roomFiles) {
		boolean isValid = true;
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
		return isValid;
	}

	/**
	 * Ensures every room .json is parsable
	 */
	public boolean checkRoomJson(MinecraftClient client) {
		boolean isValid = true;
		for (Identifier filePath : getRoomJson(client)) {
			try (BufferedReader reader = client.getResourceManager().openAsReader(filePath)) {
				DungeonManager.RoomData.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(reader)).getOrThrow();
			} catch (Exception ex) {
				String[] parts = filePath.getPath().split("/");
				String roomName = parts[2] + "-" + parts[3];
				LOGGER.error("Failed to load room: {}", roomName, ex);
				isValid = false;
			}
		}
		return isValid;
	}

	public void checkIfLoadedCorrectly(int expected) {
		int count = DungeonManager.getLoadedRoomCount();
		if (count != expected) {
			throw new AssertionError(String.format("Expected %s room(s) but only %s room(s) loaded", expected, count));
		}
	}

	List<String> getRoomFilesByType(MinecraftClient client, String fileType) {
		return client.getResourceManager().findResources(DUNGEONS_PATH, id -> id.getPath().endsWith(fileType))
				.keySet().stream().map(identifier -> identifier.getPath().split("/"))
				.filter(path -> path.length == 4).map(path -> path[3].replace(fileType, "")).toList();
	}

	List<Identifier> getRoomJson(MinecraftClient client) {
		return client.getResourceManager().findResources(DUNGEONS_PATH, id ->
				id.getPath().split("/").length == 4 && id.getPath().endsWith(".json")).keySet().stream().toList();
	}
}
