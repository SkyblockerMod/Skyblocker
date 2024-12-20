package de.hysky.skyblocker.skyblock.tabhud.config.entries;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.List;

public abstract class WidgetsListEntry extends ElementListWidget.Entry<WidgetsListEntry> {
	public static final Text ENABLED_TEXT = Text.literal("ENABLED").formatted(Formatting.GREEN);
	public static final Text DISABLED_TEXT = Text.literal("DISABLED").formatted(Formatting.RED);

	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {}

	@Override
	public void drawBorder(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		if (hovered) context.drawBorder(x, y, entryWidth, entryHeight, -1);
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return List.of();
	}

	protected void renderIconAndText(DrawContext context, ItemStack icon, int y, int x, int entryHeight) {
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		context.drawItem(icon, x + 2, y + (entryHeight - 16) / 2);
		context.drawText(textRenderer, icon.getName(), x + 20, y + (entryHeight - 9) / 2, Colors.WHITE, true);
	}

}
