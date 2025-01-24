package de.hysky.skyblocker.skyblock.itemlist.recipebook;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.recipebook.AnimatedResultButton;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class SkyblockRecipeResultButton extends ClickableWidget {
	//Corresponds to AnimatedResultButton#field_32415
	private static final int SIZE = 25;
	private static final int ITEM_OFFSET = 4;

	private ItemStack itemStack = ItemStack.EMPTY;

	protected SkyblockRecipeResultButton() {
		super(0, 0, SIZE, SIZE, ScreenTexts.EMPTY);
	}

	protected ItemStack getDisplayStack() {
		return this.itemStack;
	}

	protected void setDisplayStack(ItemStack stack) {
		this.active = !stack.isEmpty();
		this.visible = true;
		this.itemStack = stack;
	}

	protected void clearDisplayStack() {
		this.visible = false;
		this.itemStack = ItemStack.EMPTY;
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		MinecraftClient client = MinecraftClient.getInstance();

		context.drawGuiTexture(RenderLayer::getGuiTextured, AnimatedResultButton.SLOT_CRAFTABLE_TEXTURE, this.getX(), this.getY(), this.width, this.height);
		context.drawItemWithoutEntity(itemStack, this.getX() + ITEM_OFFSET, this.getY() + ITEM_OFFSET);
		context.drawStackOverlay(client.textRenderer, itemStack, this.getX() + ITEM_OFFSET, this.getY() + ITEM_OFFSET);
	}

	protected static List<Text> getTooltip(ItemStack stack) {
		return new ArrayList<>(Screen.getTooltipFromItem(MinecraftClient.getInstance(), stack));
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
