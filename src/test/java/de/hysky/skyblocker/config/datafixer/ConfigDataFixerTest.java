package de.hysky.skyblocker.config.datafixer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

public class ConfigDataFixerTest {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	@BeforeAll
	public static void setupEnvironment() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	void testDataFix(int previousVersion, int newVersion) {
		@SuppressWarnings("DataFlowIssue")
		JsonObject oldConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v" + previousVersion + ".json")), JsonObject.class);
		@SuppressWarnings("DataFlowIssue")
		JsonObject expectedNewConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker-v" + newVersion + ".json")), JsonObject.class);

		Assertions.assertEquals(expectedNewConfig, ConfigDataFixer.apply(ConfigDataFixer.CONFIG_TYPE, oldConfig, newVersion));
	}

	@Test
	void testDataFixer1() {
		//testDataFix(1, 2);
	}

	@Test
	void testDataFixer2QuickNav() {
		testDataFix(2, 3);
	}

	@Test
	void testDataFixer3() {
		testDataFix(3, 4);
	}

	@Test
	void testDataFixer6() {
		testDataFix(6, 7);
	}

	@Test
	void testDataFixer7() {
		testDataFix(7, 8);
	}
}
