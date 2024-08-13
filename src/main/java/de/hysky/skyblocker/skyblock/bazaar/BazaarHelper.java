package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.slottext.SimpleSlotTextAdder;
import de.hysky.skyblocker.skyblock.item.slottext.SlotText;
import de.hysky.skyblocker.skyblock.item.tooltip.ItemTooltip;
import de.hysky.skyblocker.skyblock.item.tooltip.info.DataTooltipInfoType;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.math.NumberUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class BazaarHelper extends SimpleSlotTextAdder {
	private static final Pattern FILLED_PATTERN = Pattern.compile("Filled: \\S+ \\(?([\\d.]+)%\\)?!?");
	private static final int RED = 0xe60b1e;
	private static final int YELLOW = 0xe6ba0b;
	private static final int GREEN = 0x1ee60b;

	public static final KeyBinding BAZAAR_LOOKUP = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bazaarLookup",
            GLFW.GLFW_KEY_F6,
            "key.categories.skyblocker"
    ));
	public static final KeyBinding BAZAAR_REFRESH = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.bazaarRefresh",
            GLFW.GLFW_KEY_Z,
            "key.categories.skyblocker"
    ));

    public BazaarHelper() {
		super("(?:Co-op|Your) Bazaar Orders");
	}

	@Init
	public static void init() {}

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

	// ======== Other Bazaar Features ========

	public static void bazaarLookup(ClientPlayerEntity player, @NotNull Slot slot) {
        ItemStack stack = ItemRepository.getItemStack(slot.getStack().getNeuName());
		if (stack != null && !stack.isEmpty()) {
			MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + Formatting.strip(stack.getName().getString()));
		} else {
			player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.bazaarLookupFailed")));
		}
	}

	public static void refreshItemPrices(ClientPlayerEntity player) {
		player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.refreshingItemPrices")));
		CompletableFuture.allOf(Stream.of(TooltipInfoType.NPC, TooltipInfoType.BAZAAR, TooltipInfoType.LOWEST_BINS, TooltipInfoType.ONE_DAY_AVERAGE, TooltipInfoType.THREE_DAY_AVERAGE)
						.map(DataTooltipInfoType::downloadIfEnabled)
                        .toArray(CompletableFuture[]::new)
				).thenRun(() -> player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.refreshedItemPrices"))))
				.exceptionally(e -> {
					ItemTooltip.LOGGER.error("[Skyblocker] Failed to refresh item prices", e);
					player.sendMessage(Constants.PREFIX.get().append(Text.translatable("skyblocker.config.helpers.bazaar.refreshItemPricesFailed")));
					return null;
				});
	}
}
