package de.hysky.skyblocker.skyblock.dwarven.profittrackers.corpse;

import de.hysky.skyblocker.skyblock.dwarven.CorpseType;
import de.hysky.skyblocker.utils.render.HudHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2IntArrayMap;
import org.apache.commons.text.WordUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.Comparator;
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

// This class is a copy of CorpseList because it's a very similar widget, but the way entries are added is different to achieve a different layout.
// The main difference between this class and that class is the constructor and the constructors of MultiEntry.
// Sure, you could reuse some of the code if you really wanted to, but it's honestly not worth it for 2 classes.
public class RewardList extends ContainerObjectSelectionList<RewardList.AbstractEntry> {
	private static final Logger LOGGER = LoggerFactory.getLogger(RewardList.class);
	private static final int BORDER_COLOR = 0xFF6C7086;
	private static final int INNER_MARGIN = 2;

	public RewardList(Minecraft client, int width, int height, int y, int entryHeight, List<CorpseLoot> lootList) {
		super(client, width, height, y, entryHeight);
		if (lootList.isEmpty()) {
			addEmptyEntry();
			addEmptyEntry();
			addEmptyEntry();
			addEntry(new SingleEntry(Component.translatable("skyblocker.corpseTracker.emptyHistory").withStyle(ChatFormatting.RED), false));
			return;
		}

		List<Reward> rewards = lootList.stream()
				.flatMap(loot -> loot.rewards().stream())
				.collect(ObjectArrayList::new,
						(list, entry) -> {
							if (list.stream().anyMatch(reward -> reward.itemId().equals(entry.itemId()))) {
								list.stream().filter(reward -> reward.itemId().equals(entry.itemId())).findFirst().ifPresent(reward -> reward.amount(reward.amount() + entry.amount()));
							} else {
								list.add(new Reward(entry.amount(), entry.itemId(), entry.pricePerUnit())); // Add a clone of the entry so we don't modify the original
							}
						}, ObjectArrayList::addAll);
		// Sorts in-place
		rewards.sort(Comparator.comparingInt(RewardList::comparePriority).thenComparing(Reward::itemId));

		Reference2IntArrayMap<CorpseType> keyAmounts = lootList.stream()
				.collect(Reference2IntArrayMap::new,
						(map, loot) -> map.mergeInt(loot.corpseType(), 1, Integer::sum),
						Reference2IntArrayMap::putAll);

		double profit = lootList.stream().mapToDouble(CorpseLoot::profit).sum();

		for (Reward reward : rewards) {
			Component itemName = CorpseList.getItemName(reward.itemId());
			if (CorpseProfitTracker.PRICELESS_ITEMS.contains(reward.itemId())) {
				addEntry(new MultiEntry(itemName, reward.amount()));
			} else {
				addEntry(new MultiEntry(itemName, reward.amount(), reward.pricePerUnit()));
			}
		}
		addEntry(new SingleEntry(Component.empty())); // Just an empty line to separate the items from the keys
		for (var entry : keyAmounts.reference2IntEntrySet()) {
			addEntry(new MultiEntry(entry.getKey(), entry.getIntValue()));
		}
		addEntry(new SingleEntry(Component.empty())); // Just an empty line to separate the keys from the total profit
		addEntry(new MultiEntry(profit));
	}

	private static int comparePriority(Reward reward) {
		// Lists according to the order in which they appear in the NAME2ID map
		ObjectIterator<String> ids = CorpseProfitTracker.getName2IdMap().values().iterator();
		int i = 0;
		while (ids.hasNext()) {
			if (ids.next().equals(reward.itemId())) return i;
			i++;
		}
		LOGGER.warn("Item ID `{}` not found in NAME2ID map", reward.itemId());
		return Integer.MAX_VALUE;
	}

	private void addEmptyEntry() {
		addEntry(new EmptyEntry());
	}

	@Override
	public int getRowWidth() {
		return 500;
	}

	abstract static class AbstractEntry extends ContainerObjectSelectionList.Entry<AbstractEntry> {
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
	private static class EmptyEntry extends AbstractEntry {
		private EmptyEntry() {
			children = List.of();
		}
	}

	// For a single line of text, allows for a border to be drawn or not
	private static class SingleEntry extends AbstractEntry {
		private boolean drawBorder = true;

		private SingleEntry(Component text) {
			children = List.of(new StringWidget(text, Minecraft.getInstance().font)/*.alignCenter()*/);
		}

		private SingleEntry(Component text, boolean drawBorder) {
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

	// Represents a multi-column line of entry, with fixed width columns
	private static class MultiEntry extends AbstractEntry {
		protected @Nullable StringWidget itemName;
		protected @Nullable StringWidget amount;
		protected @Nullable StringWidget totalPrice;
		protected @Nullable StringWidget pricePerUnit = null;

		// For the items
		private MultiEntry(Component itemName, int amount, double pricePerUnit) {
			this.itemName = new StringWidget(itemName, Minecraft.getInstance().font)/*.alignLeft()*/;
			this.amount = new StringWidget(Component.literal("x" + amount).withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font)/*.alignCenter()*/;
			this.totalPrice = new StringWidget(Component.literal(NumberFormat.getInstance().format(amount * pricePerUnit) + " Coins").withStyle(ChatFormatting.GOLD), Minecraft.getInstance().font);
			if (amount > 1) { // Only show the price per unit if there's more than 1 item, otherwise it's equal to the total price anyway and is redundant.
				this.pricePerUnit = new StringWidget(Component.literal(NumberFormat.getInstance().format(pricePerUnit) + " each").withStyle(ChatFormatting.GRAY), Minecraft.getInstance().font);
				children = List.of(this.itemName, this.amount, this.totalPrice, this.pricePerUnit);
			} else children = List.of(this.itemName, this.amount, this.totalPrice);
		}

		// For the items
		private MultiEntry(Component itemName, int amount) {
			this.itemName = new StringWidget(itemName, Minecraft.getInstance().font)/*.alignLeft()*/;
			this.amount = new StringWidget(Component.literal("x" + amount).withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font)/*.alignCenter()*/;
			children = List.of(this.itemName, this.amount);
		}

		// For the total profit line
		private MultiEntry(double profit) {
			this.itemName = new StringWidget(Component.literal("Total Profit").withStyle(ChatFormatting.BOLD, ChatFormatting.GOLD), Minecraft.getInstance().font)/*.alignLeft()*/;
			this.totalPrice = new StringWidget(Component.literal(NumberFormat.getInstance().format(profit) + " Coins").withStyle(profit > 0 ? ChatFormatting.GREEN : ChatFormatting.RED), Minecraft.getInstance().font);
			children = List.of(this.itemName, this.totalPrice);
		}

		// For the keys
		private MultiEntry(CorpseType corpseType, int amount) {
			this.itemName = new StringWidget(Component.literal(WordUtils.capitalizeFully(corpseType.name()) + " Corpse Key Cost").withStyle(corpseType.color), Minecraft.getInstance().font)/*.alignLeft()*/;
			this.amount = new StringWidget(Component.literal("x" + amount).withStyle(ChatFormatting.AQUA), Minecraft.getInstance().font)/*.alignCenter()*/;
			double pricePerKey = corpseType.getKeyPrice();
			// Gotta make do with weird formatting until we have actual formatters
			String priceString = (pricePerKey > 0 ? "-" + NumberFormat.getInstance().format(pricePerKey * amount) : 0) + " Coins";
			Component priceText = Component.literal(priceString).withStyle(pricePerKey > 0 ? ChatFormatting.RED : ChatFormatting.GOLD); // We're inverting the price here so positive price is red
			this.totalPrice = new StringWidget(priceText, Minecraft.getInstance().font);
			if (amount > 1) {
				this.pricePerUnit = new StringWidget(Component.literal(NumberFormat.getInstance().format(pricePerKey) + " each").withStyle(ChatFormatting.GRAY), Minecraft.getInstance().font);
				children = List.of(this.itemName, this.amount, this.totalPrice, this.pricePerUnit);
			} else children = List.of(this.itemName, this.amount, this.totalPrice);
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
