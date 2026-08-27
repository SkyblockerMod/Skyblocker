package de.hysky.skyblocker.config.datafixer;

import org.junit.jupiter.api.Test;

public class HudDataFixerTest {
	@Test
	void testDataFixerHudWidgets() {
		ConfigDataFixerTest.testDataFix(1, 12, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_v");
	}

	@Test
	void testDataFixerWidgetOptions() {
		ConfigDataFixerTest.testDataFix(9, 13);
		ConfigDataFixerTest.testDataFix(12, 13, ConfigDataFixer.HUD_WIDGETS_TYPE, "/assets/skyblocker/config/skyblocker/hud_widgets_v");
	}
}
