package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public class BazaarOrderTracker extends SimpleTooltipAdder {
	private static final Pattern ORDER_AMOUNT_PATTERN = Pattern.compile("(?:Order|Offer) amount: ([0-9,]+)x");
	private static final Pattern UNIT_PRICE_PATTERN = Pattern.compile("Price per unit: ([0-9,.]+) coins");
	private static final String CREATE_BUY_ORDER_TEXT = "Create Buy Order";
	private static final String CREATE_SELL_OFFER_TEXT = "Create Sell Offer";
	private static final Pattern ORDER_PATTERN = Pattern.compile("- ([0-9,.]+) coins? each \\| ([0-9,]+)x (in|from) ([0-9,]+) (order|offer)s?");
	public static final BazaarOrderTracker INSTANCE = new BazaarOrderTracker();
	private final Int2ObjectMap<Order> orders = new Int2ObjectOpenHashMap<>();

	private BazaarOrderTracker() {
		super(".* ➜ .*", 0);
	}

	void clearOrders() {
		orders.clear();
	}

	void processOrder(ItemStack stack, int slotId) {
		List<Matcher> matchers = ItemUtils.getLoreLineIfMatch(stack, UNIT_PRICE_PATTERN, ORDER_AMOUNT_PATTERN);
		if (matchers.size() < 2) return;
		String skyblockId = stack.getSkyblockId();
		double unitPrice = Double.parseDouble(matchers.get(0).group(1).replace(",", ""));
		int amount = Integer.parseInt(matchers.get(1).group(1).replace(",", ""));
		orders.put(slotId, new Order(skyblockId, unitPrice, amount, slotId < 18));
	}

	@Override
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Component> lines) {
		if (stack.is(Items.FILLED_MAP) && CREATE_BUY_ORDER_TEXT.equals(stack.getHoverName().getString())) {
			addOrderMarker(lines, false);
		} else if (stack.is(Items.MAP) && CREATE_SELL_OFFER_TEXT.equals(stack.getHoverName().getString())) {
			addOrderMarker(lines, true);
		}
	}

	private void addOrderMarker(List<Component> lines, boolean sell) {
		if (!(Minecraft.getInstance().screen instanceof ContainerScreen screen)) return;
		String skyblockId = screen.getMenu().slots.get(13).getItem().getSkyblockId();
		List<Order> yourOrders = orders.values().stream()
				.filter(o -> o.sell() == sell)
				.filter(o -> o.skyblockId().equals(skyblockId))
				.sorted(Comparator.comparingDouble(Order::unitPrice))
				.toList();
		if (yourOrders.isEmpty()) return;
		if (!sell) yourOrders = yourOrders.reversed();

		for (int i = 0, yourOrdersIndex = 0; i < lines.size() && yourOrdersIndex < yourOrders.size(); i++) {
			Component line = lines.get(i);
			Matcher matcher = ORDER_PATTERN.matcher(line.getString());
			if (!matcher.matches()) continue;
			double unitPrice = Double.parseDouble(matcher.group(1).replace(",", ""));

			while (yourOrdersIndex < yourOrders.size() && (sell ? yourOrders.get(yourOrdersIndex).unitPrice() < unitPrice : yourOrders.get(yourOrdersIndex).unitPrice() > unitPrice)) {
				yourOrdersIndex++;
			}

			int yourOrdersCount = 0;
			int yourOrdersAmount = 0;
			while (yourOrdersIndex < yourOrders.size() && yourOrders.get(yourOrdersIndex).unitPrice() == unitPrice) {
				yourOrdersCount++;
				yourOrdersAmount += yourOrders.get(yourOrdersIndex).amount();
				yourOrdersIndex++;
			}
			if (yourOrdersCount == 0) continue;
			lines.add(++i, Component.literal("  - ").withStyle(ChatFormatting.DARK_GRAY).append(Component.translatable("skyblocker.config.helpers.bazaar.orderTrackerTooltip", Component.literal(String.valueOf(yourOrdersAmount)).withStyle(ChatFormatting.GREEN), Component.literal(String.valueOf(yourOrdersCount)).withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.GRAY)));
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableOrderTracker;
	}

	private record Order(String skyblockId, double unitPrice, int amount, boolean sell) {}
}
