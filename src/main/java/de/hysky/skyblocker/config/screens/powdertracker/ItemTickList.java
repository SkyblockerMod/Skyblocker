package de.hysky.skyblocker.config.screens.powdertracker;

import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;

/**
 * A checkbox list for filter configuring purposes.
 */
public class ItemTickList<T> extends ElementListWidget<ItemTickList.ItemTickEntry> {
	private final Collection<T> filters;
	private final Collection<T> allItems;
	private final boolean whitelist;

	/**
	 *
	 * @param minecraftClient Minecraft client.
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param y The y value at which the list should render.
	 * @param entryHeight Height of a single item
	 * @param filters The items that will be marked. This should be a subset of allItems.
	 * @param allItems All possible values
	 */
	public ItemTickList(MinecraftClient minecraftClient, int width, int height, int y, int entryHeight, Collection<T> filters, Collection<T> allItems) {
		super(minecraftClient, width, height, y, entryHeight);
		this.filters = filters;
		this.allItems = allItems;
		this.whitelist = false;
	}

	/**
	 * @param minecraftClient Minecraft client.
	 * @param width The width of the list.
	 * @param height The height of the list.
	 * @param y The y value at which the list should render.
	 * @param entryHeight Height of a single item
	 * @param filters The items that will be marked. This should be a subset of allItems.
	 * @param allItems All possible values
	 * @param whitelist Whether the filter logic works as a whitelist or blacklist, to change whether the boxes for items in the filters collection should be checked. As an example: PowderFilter keeps which items to remove inside the filter (blacklist), while ChatRuleLocation keeps which locations the feature should work in (whitelist).
	 */
	public ItemTickList(MinecraftClient minecraftClient, int width, int height, int y, int entryHeight, Collection<T> filters, Collection<T> allItems, boolean whitelist) {
		super(minecraftClient, width, height, y, entryHeight);
		this.filters = filters;
		this.allItems = allItems;
		this.whitelist = whitelist;
	}

	public void clearAndInit() {
		clearEntries();
		init();
	}

	public ItemTickList<T> init() {
		for (T item : allItems) {
			ItemTickEntry entry = new ItemTickEntry(
					CheckboxWidget.builder(Text.of(item.toString()), client.textRenderer)
					              .checked(whitelist == filters.contains(item))
					              .callback((checkbox1, checked) -> {
						              if (checked) filters.remove(item);
						              else filters.add(item);
					              })
					              .build()
			);
			addEntry(entry);
		}
		return this;
	}

	public static class ItemTickEntry extends ElementListWidget.Entry<ItemTickEntry> {
		private final List<CheckboxWidget> children;

		ItemTickEntry(CheckboxWidget checkboxWidget) {
			children = List.of(checkboxWidget);
		}

		public void setChecked(boolean checked) {
			for (CheckboxWidget child : children) {
				((CheckboxWidgetAccessor) child).setChecked(checked);
			}
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			for (CheckboxWidget child : children) {
				child.setX(x);
				child.setY(y);
				child.setWidth(entryWidth);
				child.setHeight(entryHeight);
				child.render(context, mouseX, mouseY, tickDelta);
			}
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
			return children;
		}
	}
}
