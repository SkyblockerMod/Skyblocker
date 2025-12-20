package de.hysky.skyblocker.skyblock.item.tooltip;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CompactorDeletorPreview {
	/**
	 * The width and height in slots of the compactor/deletor
	 */
	private static final Map<String, IntIntPair> DIMENSIONS = Map.of(
			"4000", IntIntPair.of(1, 1),
			"5000", IntIntPair.of(1, 3),
			"6000", IntIntPair.of(1, 7),
			"7000", IntIntPair.of(2, 6)
	);
	private static final IntIntPair DEFAULT_DIMENSION = IntIntPair.of(1, 6);
	public static final Pattern NAME = Pattern.compile("PERSONAL_(?<type>COMPACTOR|DELETOR)_(?<size>\\d+)");
	private static final Minecraft client = Minecraft.getInstance();

	public static boolean drawPreview(GuiGraphics context, ItemStack stack, List<Component> tooltips, String type, String size, int x, int y) {
		int targetIndex = getTargetIndex(tooltips);
		if (targetIndex == -1) return false;

		// Get items in compactor or deletor
		CompoundTag customData = ItemUtils.getCustomData(stack);
		// Get the slots and their items from the nbt, which is in the format personal_compact_<slot_number> or personal_deletor_<slot_number>
		List<IntObjectPair<ItemStack>> slots = customData.keySet()
														.stream()
														.filter(slot -> slot.contains(type.toLowerCase(Locale.ENGLISH).substring(0, 7)))
														.map(slot -> IntObjectPair.of(Integer.parseInt(StringUtils.substringAfterLast(slot, "_")), ItemRepository.getItemStack(customData.getStringOr(slot, "")))).toList();

		List<ClientTooltipComponent> components = tooltips.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList());
		IntIntPair dimensions = DIMENSIONS.getOrDefault(size, DEFAULT_DIMENSION);

		// If there are no items in compactor or deletor
		if (slots.isEmpty()) {
			int slotsCount = dimensions.leftInt() * dimensions.rightInt();
			components.add(targetIndex, ClientTooltipComponent.create(Component.literal(slotsCount + (slotsCount == 1 ? " slot" : " slots")).withStyle(ChatFormatting.GRAY).getVisualOrderText()));

			context.renderTooltip(client.font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
			return true;
		}

		// Add the preview tooltip component
		components.add(targetIndex, new CompactorPreviewTooltipComponent(slots, dimensions));

		if (customData.contains("PERSONAL_DELETOR_ACTIVE")) {
			components.add(targetIndex, ClientTooltipComponent.create(Component.literal("Active: ")
					.append(customData.getBooleanOr("PERSONAL_DELETOR_ACTIVE", false) ? Component.literal("YES").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN) : Component.literal("NO").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED)).getVisualOrderText()));
		}
		context.renderTooltip(client.font, components, x, y, DefaultTooltipPositioner.INSTANCE, null);
		return true;
	}

	/**
	 * Finds the target index to insert the preview component, which is the second empty line
	 */
	private static int getTargetIndex(List<Component> tooltips) {
		int targetIndex = -1;
		int lineCount = 0;
		for (int i = 0; i < tooltips.size(); i++) {
			if (tooltips.get(i).getString().isEmpty()) {
				lineCount += 1;
			}
			if (lineCount == 2) {
				targetIndex = i;
				break;
			}
		}
		return targetIndex;
	}
}
