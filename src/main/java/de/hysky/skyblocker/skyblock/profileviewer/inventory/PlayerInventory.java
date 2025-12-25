package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import com.google.gson.JsonObject;

import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.background.ItemBackgroundManager;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.InventoryItemLoader;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import java.awt.Color;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class PlayerInventory implements ProfileViewerPage {
	private static final ResourceLocation TEXTURE = ResourceLocation.parse("textures/gui/container/generic_54.png");
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Font textRenderer = CLIENT.font;
	private final List<ItemStack> containerList;
	private List<Component> tooltip = Collections.emptyList();

	public PlayerInventory(JsonObject inventory) {
		this.containerList = new InventoryItemLoader().loadItems(inventory);
	}

	// Z-STACKING forces this nonsense of separating the Background texture and Item Drawing :(
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
		drawContainerTextures(context, "armor", rootX, rootY + 108, IntIntPair.of(1, 4));
		drawContainerTextures(context, "inventory", rootX, rootY + 2, IntIntPair.of(4, 9));
		drawContainerTextures(context, "equipment", rootX + 90, rootY + 108, IntIntPair.of(1, 4));

		tooltip.clear();
		drawContainerItems(context, rootX, rootY + 108, IntIntPair.of(1, 4), 36, 40, mouseX, mouseY);
		drawContainerItems(context, rootX, rootY + 2, IntIntPair.of(4, 9), 0, 36, mouseX, mouseY);
		drawContainerItems(context, rootX + 90, rootY + 108, IntIntPair.of(1, 4), 40, containerList.size(), mouseX, mouseY);
		if (!tooltip.isEmpty()) context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);
	}

	private void drawContainerTextures(GuiGraphics context, String containerName, int rootX, int rootY, IntIntPair dimensions) {
		if (containerName.equals("inventory")) {
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY + dimensions.leftInt() + 10, 0, 136, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 21, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, 14, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 21, 169, 215, 7, 7, 256, 256);
		} else {
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 17, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX, rootY + dimensions.leftInt() * 18 + 17, 0, 215, dimensions.rightInt() * 18 + 7, 7, 256, 256);
			context.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7, 256, 256);
		}
		context.drawString(textRenderer,  I18n.get("skyblocker.profileviewer.inventory." + containerName), rootX + 7, rootY + 7, Color.DARK_GRAY.getRGB(), false);
	}

	private void drawContainerItems(GuiGraphics context, int rootX, int rootY, IntIntPair dimensions, int startIndex, int endIndex, int mouseX, int mouseY) {
		for (int i = 0; i < endIndex - startIndex; i++) {
			ItemStack stack = containerList.get(startIndex + i);
			if (stack.isEmpty()) continue;

			int column = i % dimensions.rightInt();
			int row = i / dimensions.rightInt();

			int x = rootX + 8 + column * 18;
			int y = (rootY + 18 + row * 18) + (dimensions.leftInt()  > 1 && row + 1 == dimensions.leftInt() ? 4 : 0);

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
	}
}
