package de.hysky.skyblocker.utils;

import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenRectangle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScreenUtilsTest {
	private final ScreenRectangle widget = new ScreenRectangle(100, 100, 50, 20);
	@Test
	void testSnapBox() {
		assertSnapBox(ScreenUtils.getSnapBox(widget, ScreenDirection.LEFT), 95, 100, 30, 20);
		assertSnapBox(ScreenUtils.getSnapBox(widget, ScreenDirection.RIGHT), 125, 100, 30, 20);
		assertSnapBox(ScreenUtils.getSnapBox(widget, ScreenDirection.UP), 100, 95, 50, 15);
		assertSnapBox(ScreenUtils.getSnapBox(widget, ScreenDirection.DOWN), 100, 110, 50, 15);
	}

	private void assertSnapBox(ScreenRectangle snapBox, int expectedLeft, int expectedTop, int expectedWidth, int expectedHeight) {
		assertEquals(expectedLeft, snapBox.left());
		assertEquals(expectedTop, snapBox.top());
		assertEquals(expectedWidth, snapBox.width());
		assertEquals(expectedHeight, snapBox.height());
	}
}
