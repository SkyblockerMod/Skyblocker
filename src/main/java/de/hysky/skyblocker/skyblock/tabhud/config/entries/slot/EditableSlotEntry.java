package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class EditableSlotEntry extends WidgetsListSlotEntry {
	private final Button editButton;
	private final boolean locked;

	public EditableSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		editButton = Button.builder(Component.literal("EDIT"), button -> {
			this.parent.clickAndWaitForServer(this.slotId, 0);
			this.parent.resetScrollOnLoad();
		}).size(32, 12).build();
		this.locked = ItemUtils.getLoreLineIf(icon, s -> s.startsWith("Click to edit")) == null || icon.is(Items.RED_STAINED_GLASS_PANE);
	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 50 && mouseY >= y && mouseY <= y + entryHeight) {
			@SuppressWarnings("deprecation")
			List<Component> lore = ItemUtils.getLore(icon);
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, locked ? lore : lore.subList(0, Math.max(lore.size() - 2, 0)), mouseX, mouseY);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(editButton);
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int textY = this.getY() + (this.getHeight() - 9) / 2;
		Font textRenderer = Minecraft.getInstance().font;
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		if (locked) {
			context.drawString(textRenderer, "LOCKED", this.getX() + this.getWidth() - 50, textY, CommonColors.RED, true);
		} else {
			editButton.setPosition(this.getX() + this.getWidth() - 40, this.getY() + (this.getHeight() - 12) / 2);
			editButton.render(context, mouseX, mouseY, deltaTicks);
		}
	}
}
