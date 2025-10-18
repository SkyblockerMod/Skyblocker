package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse.CorpseProfitTracker.*;

public class CorpseList extends ElementListWidget<CorpseList.AbstractEntry> {
	private static final Logger LOGGER = LoggerFactory.getLogger(CorpseList.class);
	private static final int BORDER_COLOR = 0xFF6C7086;
	private static final int INNER_MARGIN = 2;

	public CorpseList(MinecraftClient client, int width, int height, int y, int entryHeight, List<CorpseLoot> lootList) {
		super(client, width, height, y, entryHeight);
		if (lootList.isEmpty()) {
			addEmptyEntry();
			addEmptyEntry();
			addEmptyEntry();
			addEntry(new CorpseList.SingleEntry(Text.literal("Your corpse history list is empty :(").formatted(Formatting.RED), false));
			return;
		}

		for (int i = 0; i < lootList.size(); i++) {
			CorpseLoot loot = lootList.get(i);
			CorpseType type = loot.corpseType();
			addEntry(new CorpseList.SingleEntry(Text.literal(WordUtils.capitalizeFully(type.name()) + " Corpse").formatted(type.color)));
			//TODO: Make this use the Formatters class instead when it's added
			addEntry(new CorpseList.SingleEntry(Text.literal(DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.ofInstant(loot.timestamp(), ZoneId.systemDefault()))).formatted(Formatting.LIGHT_PURPLE)));

			List<Reward> entries = loot.rewards();
			for (Reward reward : entries) {
				Text itemName = getItemName(reward.itemId());

				// If the item is priceless, don't show the prices
				if (PRICELESS_ITEMS.contains(reward.itemId())) addEntry(new CorpseList.MultiEntry(itemName, reward.amount()));
				else addEntry(new CorpseList.MultiEntry(itemName, reward.amount(), reward.pricePerUnit()));
			}

			if (type != CorpseType.LAPIS && type != CorpseType.UNKNOWN) {
				addEntry(new CorpseList.MultiEntry(type.getKeyPrice(), true));
			}

			if (loot.isPriceDataComplete()) addEntry(new CorpseList.MultiEntry(loot.profit()));
			else addEntry(new CorpseList.SingleEntry(Text.literal("Price data incomplete, can't calculate profit").formatted(Formatting.RED)));

			if (i < lootList.size() - 1) {
				addEmptyEntry();
				addEmptyEntry();
			}
		}
	}

	public static Text getItemName(String itemId) {
		return switch (itemId) {
			case GLACITE_POWDER -> Text.literal("Glacite Powder").formatted(Formatting.AQUA);
			case OPAL_CRYSTAL -> Text.literal("Opal Crystal").formatted(Formatting.WHITE);
			case ONYX_CRYSTAL -> Text.literal("Onyx Crystal").formatted(Formatting.DARK_GRAY);
			case AQUAMARINE_CRYSTAL -> Text.literal("Aquamarine Crystal").formatted(Formatting.BLUE);
			case PERIDOT_CRYSTAL -> Text.literal("Peridot Crystal").formatted(Formatting.DARK_GREEN);
			case CITRINE_CRYSTAL -> Text.literal("Citrine Crystal").formatted(Formatting.DARK_RED);
			case RUBY_CRYSTAL -> Text.literal("Ruby Crystal").formatted(Formatting.RED);
			case JASPER_CRYSTAL -> Text.literal("Jasper Crystal").formatted(Formatting.LIGHT_PURPLE);
			default -> {
				ItemStack itemStack = ItemRepository.getItemStack(itemId);
				if (itemStack == null) {
					LOGGER.error("Item stack for item ID {} is null", itemId);
					yield Text.empty();
				}
				yield itemStack.getName();
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

	@Override
	public int getRowTop(int index) {
		return this.getY() - (int) this.getScrollY() + index * this.itemHeight + this.headerHeight;
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float delta) {
		int i = this.getRowLeft();
		int j = this.getRowWidth();
		int l = this.getEntryCount();

		for (int m = 0; m < l; m++) {
			int n = this.getRowTop(m);
			int o = this.getRowBottom(m);
			if (o >= this.getY() && n <= this.getBottom()) {
				this.renderEntry(context, mouseX, mouseY, delta, m, i, n, j, this.itemHeight);
			}
		}
	}

	public abstract static class AbstractEntry extends ElementListWidget.Entry<AbstractEntry> {
		protected List<ClickableWidget> children;

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
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

		public SingleEntry(Text text) {
			children = List.of(new TextWidget(text, MinecraftClient.getInstance().textRenderer).alignCenter());
		}

		public SingleEntry(Text text, boolean drawBorder) {
			this(text);
			this.drawBorder = drawBorder;
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (drawBorder) context.drawBorder(x, y, entryWidth, entryHeight + 1, BORDER_COLOR);
			for (var child : children) {
				child.setX(x + INNER_MARGIN);
				child.setY(y + INNER_MARGIN);
				child.setWidth(entryWidth - 2 * INNER_MARGIN);
				child.render(context, mouseX, mouseY, tickDelta);
			}
		}
	}

	// The main grid structure
	public static class MultiEntry extends AbstractEntry {
		protected @Nullable TextWidget itemName;
		protected @Nullable TextWidget amount = null;
		protected @Nullable TextWidget totalPrice;
		protected @Nullable TextWidget pricePerUnit = null;

		// For the items
		public MultiEntry(Text itemName, int amount, double pricePerUnit) {
			this.itemName = new TextWidget(itemName, MinecraftClient.getInstance().textRenderer).alignLeft();
			this.amount = new TextWidget(Text.literal("x" + amount).formatted(Formatting.AQUA), MinecraftClient.getInstance().textRenderer).alignCenter();
			this.totalPrice = new TextWidget(Text.literal(NumberFormat.getInstance().format(amount * pricePerUnit) + " Coins").formatted(Formatting.GOLD), MinecraftClient.getInstance().textRenderer);
			this.pricePerUnit = new TextWidget(Text.literal(NumberFormat.getInstance().format(pricePerUnit) + " each").formatted(Formatting.GRAY), MinecraftClient.getInstance().textRenderer);
			children = List.of(this.itemName, this.amount, this.totalPrice, this.pricePerUnit);
		}

		// For the items
		public MultiEntry(Text itemName, int amount) {
			this.itemName = new TextWidget(itemName, MinecraftClient.getInstance().textRenderer).alignLeft();
			this.amount = new TextWidget(Text.literal("x" + amount).formatted(Formatting.AQUA), MinecraftClient.getInstance().textRenderer).alignCenter();
			children = List.of(this.itemName, this.amount);
		}

		// For the total profit line
		public MultiEntry(double profit) {
			this.itemName = new TextWidget(Text.literal("Total Profit").formatted(Formatting.BOLD, Formatting.GOLD), MinecraftClient.getInstance().textRenderer).alignLeft();
			this.totalPrice = new TextWidget(Text.literal(NumberFormat.getInstance().format(profit) + " Coins").formatted(profit > 0 ? Formatting.GREEN : Formatting.RED), MinecraftClient.getInstance().textRenderer);
			children = List.of(this.itemName, this.totalPrice);
		}

		// For the keys
		public MultiEntry(double keyPrice, boolean isKey) { // The extra boolean is just to prevent constructor overloading conflicts
			if (!isKey) throw new IllegalArgumentException("This constructor is only for key entries");
			this.itemName = new TextWidget(Text.literal("Key Price").formatted(Formatting.RED, Formatting.BOLD), MinecraftClient.getInstance().textRenderer).alignLeft();
			this.amount = new TextWidget(Text.literal("x1").formatted(Formatting.AQUA), MinecraftClient.getInstance().textRenderer).alignCenter();
			this.totalPrice = new TextWidget(Text.literal("-" + NumberFormat.getInstance().format(keyPrice) + " Coins").formatted(Formatting.RED), MinecraftClient.getInstance().textRenderer);
			children = List.of(this.itemName, this.amount, this.totalPrice);
		}

		// Space distribution:
		// Name  | amount | total price | price per unit
		// 33.3% | 16.6%  | 25%         | 25%
		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			// The +1 is to make the borders stack on top of each other
			context.drawBorder(x, y, entryWidth, entryHeight + 1, BORDER_COLOR);
			context.drawBorder(x + entryWidth / 3, y, entryWidth / 6 + 2, entryHeight + 1, BORDER_COLOR);
			context.drawBorder(x + entryWidth / 2, y, entryWidth / 4, entryHeight + 1, BORDER_COLOR);

			int entryY = y + INNER_MARGIN;
			if (itemName != null) {
				itemName.setX(x + INNER_MARGIN);
				itemName.setY(entryY);
				itemName.setWidth(entryWidth / 3 - 2 * INNER_MARGIN);
				itemName.render(context, mouseX, mouseY, tickDelta);
			}

			if (amount != null) {
				amount.setX(x + entryWidth / 3 + INNER_MARGIN);
				amount.setY(entryY);
				amount.setWidth(entryWidth / 6 - 2 * INNER_MARGIN);
				amount.render(context, mouseX, mouseY, tickDelta);
			}

			if (totalPrice != null) {
				totalPrice.setX(x + entryWidth / 2 + INNER_MARGIN);
				totalPrice.setY(entryY);
				totalPrice.setWidth(entryWidth / 4 - 2 * INNER_MARGIN);
				totalPrice.render(context, mouseX, mouseY, tickDelta);
			}

			if (pricePerUnit != null) {
				pricePerUnit.setX(x + 3 * entryWidth / 4 + INNER_MARGIN);
				pricePerUnit.setY(entryY);
				pricePerUnit.setWidth(entryWidth / 4 - 2 * INNER_MARGIN);
				pricePerUnit.render(context, mouseX, mouseY, tickDelta);
			}
		}
	}
}
