package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class SkyblockRecipeResultButton extends AbstractWidget {
	//Corresponds to AnimatedResultButton#field_32415
	private static final int SIZE = 25;
	private static final int ITEM_OFFSET = 4;

	private ItemStack itemStack = ItemStack.EMPTY;

	protected SkyblockRecipeResultButton() {
		super(0, 0, SIZE, SIZE, CommonComponents.EMPTY);
	}

	protected SkyblockRecipeResultButton(int x, int y) {
		this();
		setPosition(x, y);
	}

	protected @Nullable ItemStack getDisplayStack() {
		return this.itemStack;
	}

	protected SkyblockRecipeResultButton setDisplayStack(ItemStack stack) {
		this.active = !stack.isEmpty();
		this.visible = true;
		this.itemStack = stack;
		return this;
	}

	protected void clearDisplayStack() {
		this.visible = false;
		this.itemStack = ItemStack.EMPTY;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		Minecraft client = Minecraft.getInstance();

		context.blitSprite(RenderPipelines.GUI_TEXTURED, RecipeButton.SLOT_CRAFTABLE_SPRITE, this.getX(), this.getY(), this.width, this.height);
		context.renderFakeItem(itemStack, this.getX() + ITEM_OFFSET, this.getY() + ITEM_OFFSET);
		context.renderItemDecorations(client.font, itemStack, this.getX() + ITEM_OFFSET, this.getY() + ITEM_OFFSET);
	}

	protected static List<Component> getTooltip(ItemStack stack) {
		return new ArrayList<>(Screen.getTooltipFromItem(Minecraft.getInstance(), stack));
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}
}
