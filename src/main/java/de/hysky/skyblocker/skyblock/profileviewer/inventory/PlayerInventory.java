package de.hysky.skyblocker.skyblock.profileviewer.inventory;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.item.slottext.SlotTextManager;
import de.hysky.skyblocker.skyblock.profileviewer.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.inventory.itemLoaders.InventoryItemLoader;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.Collections;
import java.util.List;

public class PlayerInventory implements ProfileViewerPage {
    private static final Identifier TEXTURE = Identifier.of("textures/gui/container/generic_54.png");
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final TextRenderer textRenderer = CLIENT.textRenderer;
    private final List<ItemStack> containerList;
    private List<Text> tooltip = Collections.emptyList();

    public PlayerInventory(JsonObject inventory) {
        this.containerList = new InventoryItemLoader().loadItems(inventory);
    }

    // Z-STACKING forces this nonsense of separating the Background texture and Item Drawing :(
    public void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY) {
        drawContainerTextures(context, "armor", rootX, rootY + 108, IntIntPair.of(1, 4));
        drawContainerTextures(context, "inventory", rootX, rootY + 2, IntIntPair.of(4, 9));
        drawContainerTextures(context, "equipment", rootX + 90, rootY + 108, IntIntPair.of(1, 4));

        tooltip.clear();
        drawContainerItems(context, rootX, rootY + 108, IntIntPair.of(1, 4), 36, 40, mouseX, mouseY);
        drawContainerItems(context, rootX, rootY + 2, IntIntPair.of(4, 9), 0, 36, mouseX, mouseY);
        drawContainerItems(context, rootX + 90, rootY + 108, IntIntPair.of(1, 4), 40, containerList.size(), mouseX, mouseY);
        if (!tooltip.isEmpty()) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);
    }

    private void drawContainerTextures(DrawContext context, String containerName, int rootX, int rootY, IntIntPair dimensions) {
        if (containerName.equals("inventory")) {
            context.drawTexture(TEXTURE, rootX, rootY + dimensions.leftInt() + 10, 0, 136, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 21);
            context.drawTexture(TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, 14);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 21, 169, 215, 7, 7);
        } else {
            context.drawTexture(TEXTURE, rootX, rootY, 0, 0, dimensions.rightInt() * 18 + 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY, 169, 0, 7, dimensions.leftInt() * 18 + 17);
            context.drawTexture(TEXTURE, rootX, rootY + dimensions.leftInt() * 18 + 17, 0, 215, dimensions.rightInt() * 18 + 7, 7);
            context.drawTexture(TEXTURE, rootX + dimensions.rightInt() * 18 + 7, rootY + dimensions.leftInt() * 18 + 17, 169, 215, 7, 7);
        }
        context.drawText(textRenderer,  I18n.translate("skyblocker.profileviewer.inventory." + containerName), rootX + 7, rootY + 7, Color.DARK_GRAY.getRGB(), false);
    }

    private void drawContainerItems(DrawContext context, int rootX, int rootY, IntIntPair dimensions, int startIndex, int endIndex, int mouseX, int mouseY) {
        for (int i = 0; i < endIndex - startIndex; i++) {
            ItemStack stack = containerList.get(startIndex + i);
            if (stack.isEmpty()) continue;

            int column = i % dimensions.rightInt();
            int row = i / dimensions.rightInt();

            int x = rootX + 8 + column * 18;
            int y = (rootY + 18 + row * 18) + (dimensions.leftInt()  > 1 && row + 1 == dimensions.leftInt() ? 4 : 0);

            if (SkyblockerConfigManager.get().general.itemInfoDisplay.itemRarityBackgrounds) {
                ItemRarityBackgrounds.tryDraw(stack, context, x, y);
            }

            if (ItemProtection.isItemProtected(stack)) {
                RenderSystem.enableBlend();
                context.drawTexture(ItemProtection.ITEM_PROTECTION_TEX, x, y, 0, 0, 16, 16, 16, 16);
                RenderSystem.disableBlend();
            }

            context.drawItem(stack, x, y);
            context.drawItemInSlot(textRenderer, stack, x, y);
            SlotTextManager.renderSlotText(context, textRenderer, null, stack, i, x, y);

            if (mouseX > x - 2 && mouseX < x + 16 + 1 && mouseY > y - 2 && mouseY < y + 16 + 1) {
                tooltip = stack.getTooltip(Item.TooltipContext.DEFAULT, CLIENT.player, CLIENT.options.advancedItemTooltips ? TooltipType.ADVANCED : TooltipType.BASIC);
            }
        }
    }
}
