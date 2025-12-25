package de.hysky.skyblocker.skyblock.bazaar;

import com.mojang.blaze3d.platform.InputConstants;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.container.SimpleContainerSolver;
import de.hysky.skyblocker.utils.container.TooltipAdder;
import de.hysky.skyblocker.utils.render.gui.ColorHighlight;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ReorderHelper extends SimpleContainerSolver implements TooltipAdder {
	private static final Pattern BUY_PATTERN = Pattern.compile("([\\d,]+)x missing items\\.");
	private static final Pattern SELL_PATTERN = Pattern.compile("([\\d,]+)x items\\.");

	public ReorderHelper() {
		super("^Order options");
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableReorderHelper;
	}

	@Override
	public boolean onClickSlot(int slot, ItemStack stack, int screenId, int button) {
		//   V This part is so that it short-circuits if not necessary
		if ((slot == 11 || slot == 13) && stack.is(Items.GREEN_TERRACOTTA) && InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL)) {
			Matcher matcher;
			// The terracotta is at slot 13 on sell orders and at slot 11 on buy orders
			if (slot == 13) matcher = ItemUtils.getLoreLineIfContainsMatch(stack, SELL_PATTERN);
			else matcher = ItemUtils.getLoreLineIfContainsMatch(stack, BUY_PATTERN);
			if (matcher != null) {
				Minecraft.getInstance().keyboardHandler.setClipboard(matcher.group(1).replace(",", ""));
				return false;
			}
		}
		return false;
	}

	@Override
	public List<ColorHighlight> getColors(Int2ObjectMap<ItemStack> slots) {
		return List.of();
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (focusedSlot == null || !stack.is(Items.GREEN_TERRACOTTA)) return;
		switch (focusedSlot.index) {
			case 11, 13 -> {
				lines.add(Component.empty());
				lines.add(Constants.PREFIX.get());
				lines.add(Component.translatable("skyblocker.reorderHelper.tooltip").withStyle(ChatFormatting.DARK_GRAY));
			}
		}
	}

	@Override
	public int getPriority() {
		return Integer.MIN_VALUE;
	}
}
