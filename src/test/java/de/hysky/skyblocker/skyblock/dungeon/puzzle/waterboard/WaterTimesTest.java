package de.hysky.skyblocker.skyblock.dungeon.puzzle.waterboard;

import com.google.gson.*;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

public class WaterTimesTest {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@BeforeAll
	public static void setup() {
		SharedConstants.createGameVersion();
		Bootstrap.initialize();
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
