package de.hysky.skyblocker.config.datafixer;

import java.io.InputStreamReader;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;

public class HudDataFixerTest {
	@Test
	void testDataFixerHudWidgets() {
		ConfigDataFixerTest.testDataFix(9, 13);
		ConfigDataFixerTest.testDataFix(1, 12, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_v");
	}

	@Test
	void testDataFixerWidgetOptions() {
		ConfigDataFixerTest.testDataFix(9, 13);
		ConfigDataFixerTest.testDataFix(12, 13, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_v");
	}

	@Test
	void testDataFixerTabWidgets() {
		ConfigDataFixerTest.testDataFix(9, 13);
		ConfigDataFixerTest.testDataFix(1, 12, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_tab_v");
	}

	@Test
	void testDataFixerHudWidgetCopies() {
		Assertions.assertDoesNotThrow(() -> WidgetManager.Config.DATA_FIXING_CODEC.decode(JsonOps.INSTANCE, JsonParser.parseReader(new InputStreamReader(ConfigDataFixerTest.class.getResourceAsStream("/assets/skyblocker/config/skyblocker/hud_widgets_copies_v13.json")))).getOrThrow().getFirst());
	}
}
