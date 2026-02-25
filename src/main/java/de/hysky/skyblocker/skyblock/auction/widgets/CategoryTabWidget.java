package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import de.hysky.skyblocker.utils.render.gui.SideTabButtonWidget;
import de.hysky.skyblocker.utils.render.texture.FallbackedTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jspecify.annotations.Nullable;

public class CategoryTabWidget extends SideTabButtonWidget {
	@SuppressWarnings("unchecked")
	private static final @Nullable FallbackedTexture<WidgetSprites>[] SPRITES = new FallbackedTexture[6];
	private final SlotClickHandler slotClick;
	private int slotId = -1;

	private static WidgetSprites getSprites(int index) {
		FallbackedTexture<WidgetSprites> sprite = SPRITES[index];
		if (sprite == null) return (SPRITES[index] = FallbackedTexture.ofWidgetSprites(
				new WidgetSprites(
						SkyblockerMod.id("auctions_gui/category_tab_" + (index + 1)),
						SkyblockerMod.id("auctions_gui/category_tab_selected" + (index + 1))
				),
				RecipeBookTabButton.SPRITES)).get();
		return sprite.get();
	}

	public CategoryTabWidget(ItemStack icon, SlotClickHandler slotClick, int id) {
		super(0, 0, false, getSprites(id), icon);
		this.slotClick = slotClick;
	}

	@Override
	public void renderContents(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.renderContents(context, mouseX, mouseY, delta);

		if (isMouseOver(mouseX, mouseY)) {
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, icon.getTooltipLines(TooltipContext.EMPTY, Minecraft.getInstance().player, TooltipFlag.NORMAL), mouseX, mouseY);
		}
	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (this.selected || slotId == -1) return;
		super.onClick(click, doubled);
		slotClick.click(slotId);
	}
}
