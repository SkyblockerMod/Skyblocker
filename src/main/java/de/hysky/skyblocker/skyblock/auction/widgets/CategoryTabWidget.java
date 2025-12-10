package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import de.hysky.skyblocker.utils.render.gui.SideTabButtonWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;

import org.jetbrains.annotations.NotNull;

public class CategoryTabWidget extends SideTabButtonWidget {
	private final SlotClickHandler slotClick;
	private int slotId = -1;

	public CategoryTabWidget(@NotNull ItemStack icon, SlotClickHandler slotClick) {
		super(0, 0, false, icon);
		this.slotClick = slotClick;
	}

	@Override
	public void drawIcon(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderWidget(context, mouseX, mouseY, delta);

		if (isMouseOver(mouseX, mouseY)) {
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, icon.getTooltip(TooltipContext.DEFAULT, MinecraftClient.getInstance().player, TooltipType.BASIC), mouseX, mouseY);
		}

	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	@Override
	public void onClick(Click click, boolean doubled) {
		if (this.selected || slotId == -1) return;
		super.onClick(click, doubled);
		slotClick.click(slotId);
	}
}
