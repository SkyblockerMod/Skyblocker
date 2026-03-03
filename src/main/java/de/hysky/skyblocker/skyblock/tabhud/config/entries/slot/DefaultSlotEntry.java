package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class DefaultSlotEntry extends WidgetsListSlotEntry {
	private final Button leftClick;
	private final Button rightClick;

	public DefaultSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		leftClick = Button.builder(Component.literal("LEFT"), button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(32, 12)
				.build();
		rightClick = Button.builder(Component.literal("RIGHT"), button -> this.parent.clickAndWaitForServer(this.slotId, 1))
				.size(32, 12)
				.build();
	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 80 && mouseY >= y && mouseY <= y + entryHeight) {
			@SuppressWarnings("deprecation")
			List<Component> lore = ItemUtils.getLore(icon);
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(leftClick, rightClick);
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		rightClick.setPosition(this.getX() + this.getWidth() - 40, this.getY() + (this.getHeight() - 12) / 2);
		rightClick.render(context, mouseX, mouseY, deltaTicks);
		leftClick.setPosition(this.getX() + this.getWidth() - 80, this.getY() + (this.getHeight() - 12) / 2);
		leftClick.render(context, mouseX, mouseY, deltaTicks);
	}
}
