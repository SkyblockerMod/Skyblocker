package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class DefaultSlotEntry extends WidgetsListSlotEntry {
	private final ButtonWidget leftClick;
	private final ButtonWidget rightClick;

	public DefaultSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		leftClick = ButtonWidget.builder(Text.literal("LEFT"), button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(32, 12)
				.build();
		rightClick = ButtonWidget.builder(Text.literal("RIGHT"), button -> this.parent.clickAndWaitForServer(this.slotId, 1))
				.size(32, 12)
				.build();
	}

	@Override
	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 80 && mouseY >= y && mouseY <= y + entryHeight) {
			List<Text> lore = ItemUtils.getLore(icon);
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends Element> children() {
		return List.of(leftClick, rightClick);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		rightClick.setPosition(this.getX() + this.getWidth() - 40, this.getY() + (this.getHeight() - 12) / 2);
		rightClick.render(context, mouseX, mouseY, deltaTicks);
		leftClick.setPosition(this.getX() + this.getWidth() - 80, this.getY() + (this.getHeight() - 12) / 2);
		leftClick.render(context, mouseX, mouseY, deltaTicks);
	}
}
