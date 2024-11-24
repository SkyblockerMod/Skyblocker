package de.hysky.skyblocker.config.screens.powdertracker;

import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.text.Text;

import java.util.List;

public class ItemTickList extends ElementListWidget<ItemTickList.ItemTickEntry> {
	private final List<String> filters;
	private final List<String> allItems;

	public ItemTickList(MinecraftClient minecraftClient, int width, int height, int y, int entryHeight, List<String> filters, List<String> allItems) {
		super(minecraftClient, width, height, y, entryHeight);
		this.filters = filters;
		this.allItems = allItems;
	}

	public void clearAndInit() {
		clearEntries();
		init();
	}

	public ItemTickList init() {
		for (String item : allItems) {
			ItemTickEntry entry = new ItemTickEntry(
					CheckboxWidget.builder(Text.of(item), client.textRenderer)
					              .checked(!filters.contains(item))
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
