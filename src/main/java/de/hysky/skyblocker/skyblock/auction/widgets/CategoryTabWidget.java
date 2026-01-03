package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import de.hysky.skyblocker.utils.render.gui.SideTabButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class CategoryTabWidget extends SideTabButtonWidget {
	private final SlotClickHandler slotClick;
	private int slotId = -1;

	public CategoryTabWidget(ItemStack icon, SlotClickHandler slotClick) {
		super(0, 0, false, icon);
		this.slotClick = slotClick;
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);

		if (isMouseOver(mouseX, mouseY)) {
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, icon.getTooltipLines(TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL), mouseX, mouseY);
		}
	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (isStateTriggered() || slotId == -1) return;
		super.onClick(click, doubled);
		slotClick.click(slotId);
	}
}
