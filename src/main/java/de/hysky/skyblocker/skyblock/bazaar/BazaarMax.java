package de.hysky.skyblocker.skyblock.bazaar;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.font.TextFieldHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
		boolean hasBuyOrderQuantity = false;
		boolean hasClickToSpecify = false;
		Matcher maxQuantityMatcher = null;
		ItemStack stack = focusedSlot.getItem();

		List<Component> lines = stack.getTooltipLines(
				Item.TooltipContext.of(client.level),
				client.player,
				TooltipFlag.NORMAL
		);

		for (Component line : lines) {
			String text = line.getString();
			if (BUY_ORDER_QUANTITY.equals(text)) {
				hasBuyOrderQuantity = true;
				continue;
			}
			if (CLICK_TO_SPECIFY.equals(text)) {
				hasClickToSpecify = true;
				continue;
			}

			Matcher matcher = MAX_QUANTITY_PATTERN.matcher(text);
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
		if (currentLine.endsWith("max ")) {
			signField.removeCharsFromCursor(-4);
		} else if (currentLine.endsWith("m ")) {
			signField.removeCharsFromCursor(-2);
		} else if (currentLine.endsWith("x ")) {
			signField.removeCharsFromCursor(-2);
		} else {
			return;
		}

		signField.insertText(Integer.toString(lastSeenMax));
	}

	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableBazaarMax;
	}
}
