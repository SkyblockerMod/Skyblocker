package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class BooleanSlotEntry extends WidgetsListSlotEntry {
	private final Button enableButton;

	public BooleanSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		boolean enabled = !icon.skyblocker$getLoreStrings().getLast().toLowerCase(Locale.ENGLISH).contains("enable");
		enableButton = Button.builder(enabled ? ENABLED_TEXT : DISABLED_TEXT, button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(64, 12)
				.build();

	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 70 && mouseY >= y && mouseY <= y + entryHeight) {
			@SuppressWarnings("deprecation")
			List<Component> lore = ItemUtils.getLore(icon);
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(enableButton);
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		enableButton.setPosition(this.getX() + this.getWidth() - 70, this.getY() + (this.getHeight() - 12) / 2);
		enableButton.render(context, mouseX, mouseY, deltaTicks);
	}
}
