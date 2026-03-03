package de.hysky.skyblocker.skyblock.item.background;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.world.item.ItemStack;

/**
 * Base class for rendering colored backgrounds behind Minecraft items.
 * Subclasses should implement {@link #getColorKey}, {@link #draw}, and optionally
 * {@link #onScreenChange} and {@link #isEnabled} for per-background logic.
 *
 * @param <T> The key type used to determine what color to render, e.g. an enum or Integer RGB.
 */
public abstract class ColoredItemBackground<T> {

	private final Int2ReferenceOpenHashMap<T> cache = new Int2ReferenceOpenHashMap<>();
	private final Supplier<TextureAtlasSprite> sprite;

	protected ColoredItemBackground() {
		this.sprite = () -> Minecraft.getInstance()
				.getAtlasManager()
				.getAtlasOrThrow(AtlasIds.GUI)
				.getSprite(SkyblockerConfigManager.get().general.itemInfoDisplay.itemBackgroundStyle.tex);
	}

	/**
	 * Called when a new screen is initialized. Subclasses can use this to register
	 * screen-specific behavior when the screen is opened.
	 *
	 * @param title  The screen's title text
	 * @param screen The {@link Screen screen} instance itself
	 */
	protected void onScreenChange(String title, Screen screen) {
		// Default implementation does nothing
	}

	/**
	 * Determines the color key (e.g. {@link de.hysky.skyblocker.skyblock.item.SkyblockItemRarity} enum or RGB int)
	 * to use for the given item.
	 *
	 * @param stack The item to inspect
	 * @param cache The internal cache used to store/reuse results
	 * @return A non-null color key if a background should be drawn, or null to skip rendering
	 */
	protected abstract T getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<T> cache);

	/**
	 * Performs the actual background rendering using the resolved color key.
	 *
	 * @param context  The rendering context
	 * @param x        Slot x position
	 * @param y        Slot y position
	 * @param colorKey The color key, e.g. an enum or RGB integer
	 */
	protected abstract void draw(GuiGraphics context, int x, int y, T colorKey);

	/**
	 * Whether this background renderer is enabled.
	 *
	 * @return True if the background should be rendered, false otherwise.
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * Attempts to draw a background for the given {@link ItemStack} if a valid color
	 * key is found.
	 *
	 * @param stack   The {@link ItemStack} to check
	 * @param context The rendering context
	 * @param x       The slot's x position
	 * @param y       The slot's y position
	 */
	public final void tryDraw(ItemStack stack, GuiGraphics context, int x, int y) {
		T value = getColorKey(stack, cache);
		if (value != null) {
			draw(context, x, y, value);
		}
	}

	/**
	 * Clears the internal cache manually.
	 */
	protected final void clearCache() {
		cache.clear();
	}

	/**
	 * Returns the current background sprite for this renderer.
	 *
	 * @return The sprite to render with
	 */
	protected final TextureAtlasSprite getSprite() {
		return sprite.get();
	}
}
