package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.skyblock.item.tooltip.TooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import de.hysky.skyblocker.utils.render.gui.ContainerSolver;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReorderHelper extends ContainerSolver {
	private static final Pattern BUY_PATTERN = Pattern.compile("([\\d,]+)x missing items\\.");
	private static final Pattern SELL_PATTERN = Pattern.compile("([\\d,]+)x items\\.");

	public ReorderHelper() {
		super("^Order options");
	}

	@Override
	protected boolean isEnabled() {
		return true;
	}

	@Override
	protected boolean onClickSlot(int slot, ItemStack stack, int screenId, String[] groups) {
		//   V This part is so that it short-circuits if not necessary
		if ((slot == 11 || slot == 13) && stack.isOf(Items.GREEN_TERRACOTTA) && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
			Matcher matcher;
			// The terracotta is at slot 13 on sell orders and at slot 11 on buy orders
			if (slot == 13) matcher = ItemUtils.getLoreLineIfContainsMatch(stack, SELL_PATTERN);
			else matcher = ItemUtils.getLoreLineIfContainsMatch(stack, BUY_PATTERN);
			if (matcher != null) {
				MinecraftClient.getInstance().keyboard.setClipboard(matcher.group(1).replace(",", ""));
				return false;
			}
		}
		return false;
	}

	@Override
	protected List<ColorHighlight> getColors(String[] groups, Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}

	public static class Tooltip extends TooltipAdder {
		public Tooltip() {
			super("^Order options", Integer.MIN_VALUE);
		}

		@Override
		public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
			if (focusedSlot == null || !stack.isOf(Items.GREEN_TERRACOTTA)) return;
			switch (focusedSlot.id) {
				case 11, 13 -> {
					lines.add(Text.empty());
					lines.add(Text.empty().append(Text.literal("[Skyblocker] You can copy the amount of items").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)));
					lines.add(Text.empty().append(Text.literal("by holding CTRL while clicking on the item!").formatted(Formatting.DARK_GRAY, Formatting.ITALIC)));
				}
			}
		}
	}
}
