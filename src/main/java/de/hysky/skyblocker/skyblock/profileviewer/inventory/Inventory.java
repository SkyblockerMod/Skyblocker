package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import com.google.gson.JsonObject;

import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.ItemLoader;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class Inventory implements ProfileViewerPage {
	private static final ResourceLocation TEXTURE = ResourceLocation.parse("textures/gui/container/generic_54.png");
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Font textRenderer = CLIENT.font;
	private final IntIntPair dimensions;
	private final int itemsPerPage;
	private final List<ItemStack> containerList;
	private final String containerName;
	private int activePage = 0;
	private int totalPages = 1;
	private final PaginationButton previousPage = new PaginationButton(this, -1000, 0, false);
	private final PaginationButton nextPage = new PaginationButton(this, -1000, 0, true);

	public Inventory(String name, IntIntPair dimensions, JsonObject inventory) {
		this(name, dimensions, inventory, new ItemLoader());
	}

	public Inventory(String name, IntIntPair dimensions, JsonObject inventory, ItemLoader itemLoader) {
		containerName = name;
		this.dimensions = dimensions;
		itemsPerPage = dimensions.rightInt() * dimensions.leftInt();
		this.containerList = itemLoader.loadItems(inventory);
		this.totalPages = (int) Math.ceil((double) containerList.size() / itemsPerPage);
	}

	public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
		int rootYAdjusted = rootY + (26 - dimensions.leftInt() * 3);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootYAdjusted, 0, 0, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootYAdjusted, 169, 0, 7, dimensions.leftInt() * 18 + 17, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootYAdjusted + dimensions.leftInt() * 18 + 17, 0, 215, dimensions.rightInt() * 18 + 7, 7, 256, 256);
		context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootYAdjusted + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7, 256, 256);

		context.drawString(textRenderer,  I18n.get("skyblocker.profileviewer.inventory." + containerName), rootX + 7, rootYAdjusted + 7, Color.DARK_GRAY.getRGB(), false);

		if (containerList.size() > itemsPerPage) {
			previousPage.setX(rootX + 44);
			previousPage.setY(rootY + 136);
			previousPage.render(context, mouseX, mouseY, delta);

			context.drawCenteredString(textRenderer, "Page: " + (activePage + 1) + "/" + totalPages, rootX + 88, rootY + 140, Color.WHITE.getRGB());

			nextPage.setX(rootX + 121);
			nextPage.setY(rootY + 136);
			nextPage.render(context, mouseX, mouseY, delta);
		}

		int startIndex = activePage * itemsPerPage;
		int endIndex = Math.min(startIndex + itemsPerPage, containerList.size());
		List<Component> tooltip = Collections.emptyList();
		for (int i = 0; i < endIndex - startIndex; i++) {
			ItemStack stack = containerList.get(startIndex + i);
			if (stack.isEmpty()) continue;

			int column = i % dimensions.rightInt();
			int row = i / dimensions.rightInt();

			int x = rootX + 8 + column * 18;
			int y = rootYAdjusted + 18 + row * 18;

			ItemBackgroundManager.drawBackgrounds(stack, context, x, y);

			if (ItemProtection.isItemProtected(stack)) {
				context.blit(RenderPipelines.GUI_TEXTURED, ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
			}

			context.renderItem(stack, x, y);
			context.renderItemDecorations(textRenderer, stack, x, y);
			SlotTextManager.renderSlotText(context, textRenderer, null, stack, i, x, y);

			if (mouseX > x - 2 && mouseX < x + 16 + 1 && mouseY > y - 2 && mouseY < y + 16 + 1) {
				tooltip = stack.getTooltipLines(Item.TooltipContext.EMPTY, CLIENT.player, CLIENT.options.advancedItemTooltips ? TooltipFlag.ADVANCED : TooltipFlag.NORMAL);
			}
		}

		if (!tooltip.isEmpty()) context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
	}

	public void nextPage() {
		if (activePage < totalPages - 1) {
			activePage++;
		}
	}

	public void previousPage() {
		if (activePage > 0) {
			activePage--;
		}
	}

	@Override
	public void markWidgetsAsVisible() {
		nextPage.visible = true;
		previousPage.visible = true;
		nextPage.active = true;
		previousPage.active = true;
	}

	@Override
	public void markWidgetsAsInvisible() {
		nextPage.visible = false;
		previousPage.visible = false;
		nextPage.active = false;
		previousPage.active = false;
	}

	@Override
	public List<AbstractWidget> getButtons() {
		List<AbstractWidget> buttons = new ArrayList<>();
		buttons.add(nextPage);
		buttons.add(previousPage);
		return buttons;
	}
}
