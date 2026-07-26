package de.hysky.skyblocker.config.datafixer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.mojang.datafixers.DSL;
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

	void testDataFix(int previousVersion, int newVersion, DSL.TypeReference type, String file) {
		@SuppressWarnings("DataFlowIssue")
		JsonObject oldConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream(file + previousVersion + ".json")), JsonObject.class);
		@SuppressWarnings("DataFlowIssue")
		JsonObject expectedNewConfig = GSON.fromJson(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream(file + newVersion + ".json")), JsonObject.class);

		Assertions.assertEquals(expectedNewConfig, ConfigDataFixer.apply(type, oldConfig, newVersion));
	}

	void testDataFix(int previousVersion, int newVersion) {
		testDataFix(previousVersion, newVersion, ConfigDataFixer.CONFIG_TYPE, "/assets/skyblocker/config/skyblocker-v");
	}

	@Test
	void testDataFixer1() {
		testDataFix(1, 2);
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

	@Test
	void testDataFixer8() {
		testDataFix(8, 9);
	}

	@Test
	void testDataFixerHudWidgets() {
		testDataFix(1, 11, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_v");
	}
}
