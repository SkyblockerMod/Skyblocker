package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;

/**
 * Container solver that copies the lowest BIN price minus one coin
 * to the clipboard once the item is placed in the Create BIN Auction screen.
 */
public class CopyUnderbidPrice extends SimpleContainerSolver {
	private boolean copied;
	private ItemStack previousItem = ItemStack.EMPTY;

	public CopyUnderbidPrice() {
		super("^Create BIN Auction$");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.enableCopyUnderbidPrice;
	}


	@Override
	public void start(GenericContainerScreen screen) {
		copied = false;
		previousItem = ItemStack.EMPTY;
	}

	@Override
	public void markDirty() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!(client.currentScreen instanceof GenericContainerScreen screen)) return;

		ItemStack stack = screen.getScreenHandler().getSlot(13).getStack();

		if (stack.isEmpty()) {
			copied = false;
			previousItem = ItemStack.EMPTY;
			return;
		}

		if (!ItemStack.areEqual(stack, previousItem)) {
			copied = false;
			previousItem = stack.copy();
		}

		if (copied) return;

		double price = ItemUtils.getItemPrice(stack).leftDouble();
		if (price <= 1) return;

		long underbid = (long) price - 1;
		client.keyboard.setClipboard(String.valueOf(underbid));

		if (client.player != null) {
			Text priceText = Text.literal(Formatters.INTEGER_NUMBERS.format(underbid)).formatted(Formatting.GOLD);
			client.player.sendMessage(Constants.PREFIX.get()
					.append(Text.translatable("skyblocker.copyUnderbidPrice.copied", priceText).formatted(Formatting.GRAY)), false);
		}

		copied = true;
	}

	@Override
	public void reset() {
		copied = false;
		previousItem = ItemStack.EMPTY;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}
}
