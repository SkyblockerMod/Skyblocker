package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListScreen;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Locale;

public class BooleanSlotEntry extends WidgetsListSlotEntry {
	private final ButtonWidget enableButton;

	public BooleanSlotEntry(WidgetsListScreen parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		boolean enabled = !ItemUtils.getLore(icon).getLast().getString().toLowerCase(Locale.ENGLISH).contains("enable");
		enableButton = ButtonWidget.builder(enabled ? ENABLED_TEXT : DISABLED_TEXT, button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(64, 12)
				.build();

	}

	@Override
	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 70 && mouseY >= y && mouseY <= y + entryHeight) {
			List<Text> lore = ItemUtils.getLore(icon);
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends Element> children() {
		return List.of(enableButton);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		enableButton.setPosition(this.getX() + this.getWidth() - 70, this.getY() + (this.getHeight() - 12) / 2);
		enableButton.render(context, mouseX, mouseY, deltaTicks);
	}
}
