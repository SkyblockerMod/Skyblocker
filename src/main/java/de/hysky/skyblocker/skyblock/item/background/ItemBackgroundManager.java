package de.hysky.skyblocker.skyblock.item.background;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

public class ItemBackgroundManager {
	// This method is populated at compile time in buildSrc with classes annotated with RegisterItemBackground
	private static native ColoredItemBackground<?>[] getBackgrounds();
	private static final ColoredItemBackground<?>[] BACKGROUNDS = getBackgrounds();

	@Init
	public static void init() {
		// Clear the cache of every background every 5 minutes
		for (ColoredItemBackground<?> background : BACKGROUNDS) {
			Scheduler.INSTANCE.scheduleCyclic(background::clearCache, 6000);
		}

		// Hook into screen changes for per-background logic
		ScreenEvents.BEFORE_INIT.register((client, screen, width, height) -> {
			String title = screen.getTitle().getString();

			for (ColoredItemBackground<?> background : BACKGROUNDS) {
				background.onScreenChange(title, screen);
			}
		});
	}

	/**
	 * Attempts to draw all enabled item backgrounds on a single {@link ItemStack}.
	 *
	 * @param stack   The {@link ItemStack} to check
	 * @param context The {@link DrawContext} to use for rendering
	 * @param x       X position of the item
	 * @param y       Y position of the item
	 */
	public static void drawBackgrounds(ItemStack stack, DrawContext context, int x, int y) {
		for (ColoredItemBackground<?> background : BACKGROUNDS) {
			if (background.isEnabled()) {
				background.tryDraw(stack, context, x, y);
			}
		}
	}
}
