package de.hysky.skyblocker.skyblock.bazaar;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.config.SkyblockerConfigManager;

public final class BazaarMax {
	private static final String BUY_ORDER_QUANTITY = "Buy Order Quantity";
	private static final String CLICK_TO_SPECIFY = "Click to specify!";
	private static final Pattern MAX_QUANTITY_PATTERN = Pattern.compile("Buy up to ([0-9,]+)x\\.");
	private static final String PAGE_TITLE = "How many do you want\\?";
	public static final BazaarMax INSTANCE = new BazaarMax();
	private static final Minecraft client = Minecraft.getInstance();
	private int lastSeenMax = -1;

	private BazaarMax() {}

	public void checkMaxValue(@Nullable Slot focusedSlot) {
		if (focusedSlot == null) return;
		boolean hasBuyOrderQuantity = false;
		boolean hasClickToSpecify = false;
		Matcher maxQuantityMatcher = null;
		ItemStack stack = focusedSlot.getItem();

		List<String> lines = stack.skyblocker$getLoreStrings();

		for (String line : lines) {
			if (BUY_ORDER_QUANTITY.equals(line)) {
				hasBuyOrderQuantity = true;
				continue;
			}
			if (CLICK_TO_SPECIFY.equals(line)) {
				hasClickToSpecify = true;
				continue;
			}

			Matcher matcher = MAX_QUANTITY_PATTERN.matcher(line);
			if (matcher.matches()) maxQuantityMatcher = matcher;
		}

		if (hasBuyOrderQuantity && hasClickToSpecify && maxQuantityMatcher != null) {
			try {
				lastSeenMax = Integer.parseInt(maxQuantityMatcher.group(1).replace(",", ""));
			} catch (NumberFormatException _) {
				// do nothing
			}
		}
	}

	public void expandMax(TextFieldHelper signField, String currentLine) {
		if (lastSeenMax < 0) return;
		switch (currentLine) {
			case "max " -> signField.removeCharsFromCursor(-4);
			case "m ", "x " -> signField.removeCharsFromCursor(-2);
			default -> {
				return;
			}
		}

		signField.insertText(Integer.toString(lastSeenMax));
	}

	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableBazaarMax;
	}
}
