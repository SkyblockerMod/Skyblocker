package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

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
	public void start(ContainerScreen screen) {
		copied = false;
		previousItem = ItemStack.EMPTY;
	}

	@Override
	public void markDirty() {
		Minecraft client = Minecraft.getInstance();
		if (!(client.screen instanceof ContainerScreen screen)) return;

		ItemStack stack = screen.getMenu().getSlot(13).getItem();

		if (stack.isEmpty()) {
			copied = false;
			previousItem = ItemStack.EMPTY;
			return;
		}

		if (!ItemStack.matches(stack, previousItem)) {
			copied = false;
			previousItem = stack.copy();
		}

		if (copied) return;

		double price = ItemUtils.getItemPrice(stack).leftDouble();
		if (price <= 1) return;

		long underbid = (long) price - 1;
		client.keyboardHandler.setClipboard(String.valueOf(underbid));

		if (client.player != null) {
			Component priceText = Component.literal(Formatters.INTEGER_NUMBERS.format(underbid)).withStyle(ChatFormatting.GOLD);
			client.player.displayClientMessage(Constants.PREFIX.get()
					.append(Component.translatable("skyblocker.copyUnderbidPrice.copied", priceText).withStyle(ChatFormatting.GRAY)), false);
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
