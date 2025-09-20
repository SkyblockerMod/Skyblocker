package de.hysky.skyblocker.skyblock.bazaar;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.SimpleTooltipAdder;
import de.hysky.skyblocker.utils.ItemUtils;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BazaarOrderTracker extends SimpleTooltipAdder {
	private static final String BAZAAR_HEAD_TEXTURE = "ewogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJpZCIgOiAiYmE0ODUzODFjNzI5NDhiY2E0NzY1NjJjNzRlZmE0NTkiLAogICAgICAidHlwZSIgOiAiU0tJTiIsCiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzIzMmUzODIwODk3NDI5MTU3NjE5YjBlZTA5OWZlYzA2MjhmNjAyZmZmMTJiNjk1ZGU1NGFlZjExZDkyM2FkNyIsCiAgICAgICJwcm9maWxlSWQiIDogIjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwKICAgICAgInRleHR1cmVJZCIgOiAiYzIzMmUzODIwODk3NDI5MTU3NjE5YjBlZTA5OWZlYzA2MjhmNjAyZmZmMTJiNjk1ZGU1NGFlZjExZDkyM2FkNyIKICAgIH0KICB9LAogICJza2luIiA6IHsKICAgICJpZCIgOiAiYmE0ODUzODFjNzI5NDhiY2E0NzY1NjJjNzRlZmE0NTkiLAogICAgInR5cGUiIDogIlNLSU4iLAogICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jMjMyZTM4MjA4OTc0MjkxNTc2MTliMGVlMDk5ZmVjMDYyOGY2MDJmZmYxMmI2OTVkZTU0YWVmMTFkOTIzYWQ3IiwKICAgICJwcm9maWxlSWQiIDogIjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwKICAgICJ0ZXh0dXJlSWQiIDogImMyMzJlMzgyMDg5NzQyOTE1NzYxOWIwZWUwOTlmZWMwNjI4ZjYwMmZmZjEyYjY5NWRlNTRhZWYxMWQ5MjNhZDciCiAgfSwKICAiY2FwZSIgOiBudWxsCn0=";
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
	public void addToTooltip(@Nullable Slot focusedSlot, ItemStack stack, List<Text> lines) {
		if (stack.isOf(Items.FILLED_MAP) && CREATE_BUY_ORDER_TEXT.equals(stack.getName().getString())) {
			addOrderMarker(lines, false);
		} else if (stack.isOf(Items.MAP) && CREATE_SELL_OFFER_TEXT.equals(stack.getName().getString())) {
			addOrderMarker(lines, true);
		}
	}

	private void addOrderMarker(List<Text> lines, boolean sell) {
		if (!(MinecraftClient.getInstance().currentScreen instanceof GenericContainerScreen screen)) return;
		String skyblockId = screen.getScreenHandler().slots.get(13).getStack().getSkyblockId();
		List<Order> yourOrders = orders.values().stream()
				.filter(o -> o.sell() == sell)
				.filter(o -> o.skyblockId().equals(skyblockId))
				.sorted(Comparator.comparingDouble(Order::unitPrice))
				.toList();
		if (yourOrders.isEmpty()) return;
		if (!sell) yourOrders = yourOrders.reversed();

		for (int i = 0, yourOrdersIndex = 0; i < lines.size() && yourOrdersIndex < yourOrders.size(); i++) {
			Text line = lines.get(i);
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
			lines.add(++i, Text.literal("  - ").formatted(Formatting.DARK_GRAY).append(Text.translatable("skyblocker.config.helpers.bazaar.orderTrackerTooltip", Text.literal(String.valueOf(yourOrdersAmount)).formatted(Formatting.GREEN), Text.literal(String.valueOf(yourOrdersCount)).formatted(Formatting.WHITE)).formatted(Formatting.GRAY)));
		}
	}

	@Override
	public boolean isEnabled() {
		return SkyblockerConfigManager.get().helpers.bazaar.enableOrderTracker;
	}

	private record Order(String skyblockId, double unitPrice, int amount, boolean sell) {}
}
