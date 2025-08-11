package de.hysky.skyblocker.skyblock.item.background.adders;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.background.ColoredItemBackground;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ReferenceOpenHashMap;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;

import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JacobMedalBackground extends ColoredItemBackground<Integer> {

	private static final Pattern PATTERN = Pattern.compile("You placed in the (?<bracket>DIAMOND|PLATINUM|GOLD|SILVER|BRONZE) bracket!");
	private static final Map<String, Integer> BRACKET_COLORS = Map.of(
			"DIAMOND", Objects.requireNonNull(Formatting.AQUA.getColorValue()),
			"PLATINUM", Objects.requireNonNull(Formatting.DARK_AQUA.getColorValue()),
			"GOLD", Objects.requireNonNull(Formatting.GOLD.getColorValue()),
			"SILVER", Objects.requireNonNull(Formatting.WHITE.getColorValue()),
			"BRONZE", Objects.requireNonNull(Formatting.RED.getColorValue())
	);

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().general.itemInfoDisplay.jacobMedalBackgrounds;
	}

	@Override
	protected Integer getColorKey(ItemStack stack, Int2ReferenceOpenHashMap<Integer> cache) {
		if (stack == null || stack.isEmpty()) {
			return null;
		}

		int hashCode = stack.getUuid().isEmpty() ? System.identityHashCode(stack) : stack.getUuid().hashCode();
		if (cache.containsKey(hashCode)) {
			return cache.get(hashCode);
		}

		Matcher matcher = ItemUtils.getLoreLineIfMatch(stack, PATTERN);
		if (matcher != null) {
			Integer color = BRACKET_COLORS.get(matcher.group("bracket"));
			if (color != null) {
				cache.put(hashCode, color);
				return color;
			}
		}

		cache.put(hashCode, null);
		return null;
	}

	@Override
	protected void draw(DrawContext context, int x, int y, Integer color) {
		float r = ((color >> 16) & 0xFF) / 255F;
		float g = ((color >> 8) & 0xFF) / 255F;
		float b = (color & 0xFF) / 255F;

		context.drawSpriteStretched(RenderPipelines.GUI_TEXTURED, getSprite(), x, y, 16, 16,
				ColorHelper.fromFloats(
						SkyblockerConfigManager.get().general.itemInfoDisplay.itemBackgroundOpacity,
						r, g, b
				)
		);
	}
}
