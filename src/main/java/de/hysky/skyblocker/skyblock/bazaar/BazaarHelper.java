package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarHelper extends SimpleSlotTextAdder {
	private static final Pattern FILLED_PATTERN = Pattern.compile("Filled: \\S+ \\(?([\\d.]+)%\\)?!?");
	private static final int RED = 0xe60b1e;
	private static final int YELLOW = 0xe6ba0b;
	private static final int GREEN = 0x1ee60b;

	public BazaarHelper() {
		super("(?:Co-op|Your) Bazaar Orders");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableBazaarHelper;
	}

	@Override
	public @NotNull List<SlotText> getText(@Nullable Slot slot, @NotNull ItemStack stack, int slotId) {
		if (slot == null) return List.of();
		// Skip the first row as it's always glass panes.
		if (slotId < 10) return List.of();
		// Skip the last 10 items. 11 is subtracted because size is 1-based so the last slot is size - 1.
		if (slotId > slot.inventory.size() - 11) return List.of(); //Note that this also skips the slots in player's inventory (anything above 36/45/54 depending on the order count)

		int column = slotId % 9;
		if (column == 0 || column == 8) return List.of(); // Skip the first and last column as those are always glass panes as well.

		ItemStack item = slot.getStack();
		if (item.isEmpty()) return List.of(); //We've skipped all invalid slots, so we can just check if it's not air here.

		Matcher matcher = ItemUtils.getLoreLineIfMatch(item, FILLED_PATTERN);
		if (matcher != null) {
			List<Text> lore = ItemUtils.getLore(item);
			if (!lore.isEmpty() && lore.getLast().getString().equals("Click to claim!")) { //Only show the filled icon when there are items to claim
				int filled = NumberUtils.toInt(matcher.group(1));
				return SlotText.topLeftList(getFilledIcon(filled));
			}
		}

		if (ItemUtils.getLoreLineIf(item, str -> str.equals("Expired!")) != null) {
			return SlotText.topLeftList(getExpiredIcon());
		} else if (ItemUtils.getLoreLineIf(item, str -> str.startsWith("Expires in")) != null) {
			return SlotText.topLeftList(getExpiringIcon());
		}

		return List.of();
	}

	public static @NotNull MutableText getExpiredIcon() {
		return Text.literal("⏰").withColor(RED).formatted(Formatting.BOLD);
	}

	public static @NotNull MutableText getExpiringIcon() {
		return Text.literal("⏰").withColor(YELLOW).formatted(Formatting.BOLD);
	}

	public static @NotNull MutableText getFilledIcon(int filled) {
		if (filled < 100) return Text.literal("%").withColor(YELLOW).formatted(Formatting.BOLD);
		return Text.literal("✅").withColor(GREEN).formatted(Formatting.BOLD);
	}
}
