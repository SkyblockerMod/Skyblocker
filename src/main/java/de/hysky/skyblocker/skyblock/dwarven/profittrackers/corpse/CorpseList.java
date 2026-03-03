package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.render.HudHelper;
import org.apache.commons.text.WordUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class CorpseList extends ContainerObjectSelectionList<CorpseList.AbstractEntry> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CorpseList.class);
	private static final int BORDER_COLOR = 0xFF6C7086;
	private static final int INNER_MARGIN = 2;

	public CorpseList(Minecraft client, int width, int height, int y, int entryHeight, List<CorpseLoot> lootList) {
		super(client, width, height, y, entryHeight);
		if (lootList.isEmpty()) {
			addEmptyEntry();
			addEmptyEntry();
			addEmptyEntry();
			addEntry(new CorpseList.SingleEntry(Component.literal("Your corpse history list is empty :(").withStyle(ChatFormatting.RED), false));
			return;
		}

		for (int i = 0; i < lootList.size(); i++) {
			CorpseLoot loot = lootList.get(i);
			CorpseType type = loot.corpseType();
			addEntry(new CorpseList.SingleEntry(Component.literal(WordUtils.capitalizeFully(type.name()) + " Corpse").withStyle(type.color)));
			//TODO: Make this use the Formatters class instead when it's added
			addEntry(new CorpseList.SingleEntry(Component.literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(loot.timestamp(), ZoneId.systemDefault()))).withStyle(ChatFormatting.LIGHT_PURPLE)));

			List<Reward> entries = loot.rewards();
			for (Reward reward : entries) {
				Component itemName = getItemName(reward.itemId());

				// If the item is priceless, don't show the prices
				if (CorpseProfitTracker.PRICELESS_ITEMS.contains(reward.itemId())) addEntry(new CorpseList.MultiEntry(itemName, reward.amount()));
				else addEntry(new CorpseList.MultiEntry(itemName, reward.amount(), reward.pricePerUnit()));
			}

			if (type != CorpseType.LAPIS && type != CorpseType.UNKNOWN) {
				addEntry(new CorpseList.MultiEntry(type.getKeyPrice(), true));
			}

			if (loot.isPriceDataComplete()) addEntry(new CorpseList.MultiEntry(loot.profit()));
			else addEntry(new CorpseList.SingleEntry(Component.literal("Price data incomplete, can't calculate profit").withStyle(ChatFormatting.RED)));

			if (i < lootList.size() - 1) {
				addEmptyEntry();
				addEmptyEntry();
			}
		}
	}

	public static Component getItemName(String itemId) {
		return switch (itemId) {
			case CorpseProfitTracker.GLACITE_POWDER -> Component.literal("Glacite Powder").withStyle(ChatFormatting.AQUA);
			case CorpseProfitTracker.OPAL_CRYSTAL -> Component.literal("Opal Crystal").withStyle(ChatFormatting.WHITE);
			case CorpseProfitTracker.ONYX_CRYSTAL -> Component.literal("Onyx Crystal").withStyle(ChatFormatting.DARK_GRAY);
			case CorpseProfitTracker.AQUAMARINE_CRYSTAL -> Component.literal("Aquamarine Crystal").withStyle(ChatFormatting.BLUE);
			case CorpseProfitTracker.PERIDOT_CRYSTAL -> Component.literal("Peridot Crystal").withStyle(ChatFormatting.DARK_GREEN);
			case CorpseProfitTracker.CITRINE_CRYSTAL -> Component.literal("Citrine Crystal").withStyle(ChatFormatting.DARK_RED);
			case CorpseProfitTracker.RUBY_CRYSTAL -> Component.literal("Ruby Crystal").withStyle(ChatFormatting.RED);
			case CorpseProfitTracker.JASPER_CRYSTAL -> Component.literal("Jasper Crystal").withStyle(ChatFormatting.LIGHT_PURPLE);
			case CorpseProfitTracker.ENCHANTMENT_ICE_COLD_1 -> Component.literal("Enchanted Book (Ice Cold I)").withStyle(ChatFormatting.WHITE);
			default -> {
				ItemStack itemStack = ItemRepository.getItemStack(itemId);
				if (itemStack == null) {
					LOGGER.error("Item stack for item ID {} is null", itemId);
					yield Component.empty();
				}
				yield itemStack.getHoverName();
			}
		};
	}

	private void addEmptyEntry() {
		addEntry(new EmptyEntry());
	}

	@Override
	public int getRowWidth() {
		return 500;
	}

	public abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
		protected List<AbstractWidget> children;

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}
	}

	// As a separator between entries
	public static class EmptyEntry extends AbstractEntry {
		public EmptyEntry() {
			children = List.of();
		}
	}

	// For a single line of text, allows for a border to be drawn or not
	public static class SingleEntry extends AbstractEntry {
		private boolean drawBorder = true;

		public SingleEntry(Component text) {
			children = List.of(new StringWidget(text, Minecraft.getInstance().font));
		}

		public SingleEntry(Component text, boolean drawBorder) {
			this(text);
			this.drawBorder = drawBorder;
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			if (drawBorder) HudHelper.drawBorder(context, this.getX(), this.getY(), this.getWidth(), this.getHeight() + 1, BORDER_COLOR);
			for (var child : children) {
				child.setX(this.getX() + INNER_MARGIN);
				child.setY(this.getY() + INNER_MARGIN);
				child.setWidth(this.getWidth() - 2 * INNER_MARGIN);
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}
	}

	// The main grid structure
	public static class MultiEntry extends AbstractEntry {
		protected @Nullable StringWidget itemName;
		protected @Nullable StringWidget amount = null;
		protected @Nullable StringWidget totalPrice;
		protected @Nullable StringWidget pricePerUnit = null;

		// For the items
		public MultiEntry(Component itemName, int amount, double pricePerUnit) {
			this.itemName = new StringWidget(itemName, Minecraft.getInstance().font);
			this.amount = new StringWidget(Component.literal("x" + amount).withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font);
			this.totalPrice = new StringWidget(Component.literal(NumberFormat.getInstance().format(amount * pricePerUnit) + " Coins").withStyle(ChatFormatting.GOLD), Minecraft.getInstance().font);
			this.pricePerUnit = new StringWidget(Component.literal(NumberFormat.getInstance().format(pricePerUnit) + " each").withStyle(ChatFormatting.GRAY), Minecraft.getInstance().font);
			children = List.of(this.itemName, this.amount, this.totalPrice, this.pricePerUnit);
		}

		// For the items
		public MultiEntry(Component itemName, int amount) {
			this.itemName = new StringWidget(itemName, Minecraft.getInstance().font);
			this.amount = new StringWidget(Component.literal("x" + amount).withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font);
			children = List.of(this.itemName, this.amount);
		}

		// For the total profit line
		public MultiEntry(double profit) {
			this.itemName = new StringWidget(Component.literal("Total Profit").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), Minecraft.getInstance().font);
			this.totalPrice = new StringWidget(Component.literal(NumberFormat.getInstance().format(profit) + " Coins").withStyle(profit > 0 ? ChatFormatting.GREEN : ChatFormatting.RED), Minecraft.getInstance().font);
			children = List.of(this.itemName, this.totalPrice);
		}

		// For the keys
		public MultiEntry(double keyPrice, boolean isKey) { // The extra boolean is just to prevent constructor overloading conflicts
			if (!isKey) throw new IllegalArgumentException("This constructor is only for key entries");
			this.itemName = new StringWidget(Component.literal("Key Price").withStyle(ChatFormatting.RED, ChatFormatting.BOLD), Minecraft.getInstance().font);
			this.amount = new StringWidget(Component.literal("x1").withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font);
			this.totalPrice = new StringWidget(Component.literal("-" + NumberFormat.getInstance().format(keyPrice) + " Coins").withStyle(ChatFormatting.RED), Minecraft.getInstance().font);
			children = List.of(this.itemName, this.amount, this.totalPrice);
		}

		// Space distribution:
		// Name  | amount | total price | price per unit
		// 33.3% | 16.6%  | 25%         | 25%
		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			int x = this.getX();
			int y = this.getY();
			int entryWidth = this.getWidth();
			int entryHeight = this.getHeight();
			// The +1 is to make the borders stack on top of each other
			HudHelper.drawBorder(context, x, y, entryWidth, entryHeight + 1, BORDER_COLOR);
			HudHelper.drawBorder(context, x + entryWidth / 3, y, entryWidth / 6 + 2, entryHeight + 1, BORDER_COLOR);
			HudHelper.drawBorder(context, x + entryWidth / 2, y, entryWidth / 4, entryHeight + 1, BORDER_COLOR);

			int entryY = y + INNER_MARGIN;
			if (itemName != null) {
				itemName.setX(x + INNER_MARGIN);
				itemName.setY(entryY);
				itemName.setMaxWidth(entryWidth / 3 - 2 * INNER_MARGIN, StringWidget.TextOverflow.SCROLLING);
				itemName.render(context, mouseX, mouseY, deltaTicks);
			}

			if (amount != null) {
				position(amount, x + entryWidth / 3 + INNER_MARGIN, entryWidth / 6 - 2 * INNER_MARGIN, entryY);
				amount.render(context, mouseX, mouseY, deltaTicks);
			}

			if (totalPrice != null) {
				position(totalPrice, x + entryWidth / 2 + INNER_MARGIN, entryWidth / 4 - 2 * INNER_MARGIN, entryY);
				totalPrice.render(context, mouseX, mouseY, deltaTicks);
			}

			if (pricePerUnit != null) {
				position(pricePerUnit, x + 3 * entryWidth / 4 + INNER_MARGIN, entryWidth / 4 - 2 * INNER_MARGIN, entryY);
				pricePerUnit.render(context, mouseX, mouseY, deltaTicks);
			}
		}

		private static void position(StringWidget widget, int start, int width, int y) {
			widget.setMaxWidth(width, StringWidget.TextOverflow.SCROLLING);
			widget.setX(start + (width - widget.getWidth()) / 2);
			widget.setY(y);
		}
	}
}
