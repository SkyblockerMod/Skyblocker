package de.hysky.skyblocker;

import com.google.gson.*;
import de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard.Waterboard;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

public class VerifyJsonTest {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
	}

	@Test
	void verifyKuudraPearlWaypoints() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject waypoints = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/crimson/kuudra/pearl_waypoints.json")), JsonObject.class);

		verifyBlockPosObjectArray(waypoints.get("waypoints"));
	}

	@Test
	void verifyKuudraSafeSpotWaypoints() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject waypoints = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/crimson/kuudra/safe_spot_waypoints.json")), JsonObject.class);

		verifyBlockPosObjectArray(waypoints.get("waypoints"));
	}

	@Test
	void verifyDungeonRooms() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject rooms = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/dungeons/dungeonrooms.json")), JsonObject.class);

		Assertions.assertFalse(rooms.isEmpty());
		rooms.asMap().forEach((roomKey, roomElement) -> {
			if (roomKey.equals("copyright") || roomKey.equals("license")) return;
			Assertions.assertTrue(roomElement.isJsonObject());
			JsonObject room = roomElement.getAsJsonObject();
			Assertions.assertTrue(room.getAsJsonPrimitive("category").isString());
			Assertions.assertTrue(room.getAsJsonPrimitive("secrets").isNumber());
			Assertions.assertTrue(room.getAsJsonPrimitive("fairysoul").isBoolean());
		});
	}

	@Test
	void verifySecretLocations() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject rooms = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/dungeons/secretlocations.json")), JsonObject.class);

		Assertions.assertFalse(rooms.isEmpty());
		rooms.asMap().forEach((secretKey, roomElement) -> {
			if (secretKey.equals("copyright") || secretKey.equals("license")) return;
			Assertions.assertTrue(roomElement.isJsonArray());
			JsonArray room = roomElement.getAsJsonArray();
			Assertions.assertFalse(room.isEmpty());

			room.forEach(secretElement -> {
				verifyBlockPosObject(secretElement);

				JsonObject secret = secretElement.getAsJsonObject();
				Assertions.assertTrue(secret.getAsJsonPrimitive("secretName").isString());
				Assertions.assertTrue(secret.getAsJsonPrimitive("category").isString());
			});
		});
	}

	@Test
	void verifyGoldorWaypoints() {
		@SuppressWarnings("DataFlowIssue")
		JsonArray waypoints = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/dungeons/goldorwaypoints.json")), JsonArray.class);

		Assertions.assertFalse(waypoints.isEmpty());
		waypoints.forEach(goldorWaypoint -> {
			Assertions.assertTrue(goldorWaypoint.isJsonObject());
			JsonObject waypoint = goldorWaypoint.getAsJsonObject();
			Assertions.assertTrue(waypoint.getAsJsonPrimitive("kind").isString());
			Assertions.assertTrue(waypoint.getAsJsonPrimitive("phase").isNumber());
			Assertions.assertTrue(waypoint.getAsJsonPrimitive("name").isString());
			verifyBlockPos(waypoint.get("pos"));
		});
	}

	@Test
	void verifyEnigmaSoulWaypoints() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject waypoints = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/rift/enigma_soul_waypoints.json")), JsonObject.class);

		verifyBlockPosObjectArray(waypoints.get("waypoints"));
	}

	@Test
	void verifyMirrorverseWaypoints() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject waypoints = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/rift/mirrorverse_waypoints.json")), JsonObject.class);

		Assertions.assertTrue(waypoints.get("sections").isJsonArray());
		JsonArray sections = waypoints.get("sections").getAsJsonArray();
		Assertions.assertFalse(sections.isEmpty());

		sections.forEach(sectionElement -> {
			Assertions.assertTrue(sectionElement.isJsonObject());
			JsonObject section = sectionElement.getAsJsonObject();
			Assertions.assertTrue(section.getAsJsonPrimitive("name").isString());
			verifyBlockPosObjectArray(section.get("waypoints"));
		});
	}

	@Test
	void verifyRelics() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject relics = GSON.fromJson(new InputStreamReader(this.getClass().getResourceAsStream("/assets/skyblocker/spidersden/relics.json")), JsonObject.class);

		verifyBlockPosObjectArray(relics.get("locations"));
	}

	static void verifyBlockPosObjectArray(JsonElement posArrayElement) {
		Assertions.assertTrue(posArrayElement.isJsonArray());
		JsonArray posArray = posArrayElement.getAsJsonArray();
		Assertions.assertFalse(posArray.isEmpty());
		posArray.forEach(VerifyJsonTest::verifyBlockPosObject);
	}

	static void verifyBlockPosObject(JsonElement posElement) {
		Assertions.assertTrue(posElement.isJsonObject());
		JsonObject pos = posElement.getAsJsonObject();
		Assertions.assertTrue(pos.getAsJsonPrimitive("x").isNumber());
		Assertions.assertTrue(pos.getAsJsonPrimitive("y").isNumber());
		Assertions.assertTrue(pos.getAsJsonPrimitive("z").isNumber());
	}

	/**
	 * Verifies the compact json form of block pos created by {@link net.minecraft.util.math.BlockPos#CODEC}.
	 */
	static void verifyBlockPos(JsonElement posElement) {
		Assertions.assertTrue(posElement.isJsonArray());
		JsonArray pos = posElement.getAsJsonArray();
		Assertions.assertEquals(3, pos.size());
		Assertions.assertTrue(pos.get(0).getAsJsonPrimitive().isNumber());
		Assertions.assertTrue(pos.get(1).getAsJsonPrimitive().isNumber());
		Assertions.assertTrue(pos.get(2).getAsJsonPrimitive().isNumber());
	}

	@Test
	void verifyWaterTimes() {
		@SuppressWarnings("DataFlowIssue")
		JsonObject waterTimes = GSON.fromJson(new InputStreamReader(Waterboard.class.getResourceAsStream("/assets/skyblocker/dungeons/watertimes.json")), JsonObject.class);

		Assertions.assertFalse(waterTimes.isEmpty());
		waterTimes.asMap().forEach(this::verifyVariant);
	}

	private void verifyVariant(String variant, JsonElement waterTimesElement) {
		Assertions.assertTrue(Integer.parseInt(variant) > 0);

		Assertions.assertTrue(waterTimesElement.isJsonObject());
		JsonObject waterTimes = waterTimesElement.getAsJsonObject();
		Assertions.assertFalse(waterTimes.isEmpty());
		waterTimes.asMap().forEach(this::verifySolution);
	}

	private void verifySolution(String doorCombination, JsonElement solutionElement) {
		// Verify that door is a valid combination
		Assertions.assertEquals(3, doorCombination.length());
		Assertions.assertTrue(Integer.parseInt(doorCombination) > 0);
		// Verify that door contains increasing digits
		Assertions.assertTrue(doorCombination.charAt(0) < doorCombination.charAt(1) && doorCombination.charAt(1) < doorCombination.charAt(2));

		Assertions.assertTrue(solutionElement.isJsonObject());
		JsonObject solution = solutionElement.getAsJsonObject();
		Assertions.assertFalse(solution.isEmpty());
		solution.asMap().forEach(this::verifyDoor);
	}

	private void verifyDoor(String door, JsonElement timesElement) {
		Assertions.assertNotNull(Waterboard.LeverType.fromName(door));

		Assertions.assertTrue(timesElement.isJsonArray());
		JsonArray times = timesElement.getAsJsonArray();
		Assertions.assertFalse(times.isEmpty());

		for (JsonElement time : times) {
			Assertions.assertTrue(time.isJsonPrimitive());
			Assertions.assertTrue(time.getAsJsonPrimitive().isNumber());
			double timeValue = time.getAsDouble();
			Assertions.assertTrue(timeValue >= 0);
		}
	}
}
