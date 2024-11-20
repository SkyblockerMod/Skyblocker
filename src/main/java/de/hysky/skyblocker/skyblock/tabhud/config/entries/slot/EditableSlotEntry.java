package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;

public class EditableSlotEntry extends WidgetsListSlotEntry {
	private final ButtonWidget editButton;
	private final boolean locked;

	public EditableSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		editButton = ButtonWidget.builder(Text.literal("EDIT"), button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(32, 12)
				.build();
		this.locked = ItemUtils.getLoreLineIf(icon, s -> s.startsWith("Click to edit")) == null || icon.isOf(Items.RED_STAINED_GLASS_PANE);
	}

	@Override
	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 50 && mouseY >= y && mouseY <= y + entryHeight) {
			List<Text> lore = ItemUtils.getLore(icon);
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, locked ? lore : lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends Element> children() {
		return List.of(editButton);
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		int textY = y + (entryHeight - 9) / 2;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		renderIconAndText(context, y, x, entryHeight);
		if (locked) {
			context.drawText(textRenderer, "LOCKED", x + entryWidth - 50, textY, Colors.RED, true);
		} else {
			editButton.setPosition(x + entryWidth - 40, y + (entryHeight - 12) / 2);
			editButton.render(context, mouseX, mouseY, tickDelta);
		}
	}
}
