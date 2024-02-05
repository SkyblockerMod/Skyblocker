package de.hysky.skyblocker.skyblock.garden;

import java.util.HashMap;
import java.util.Map;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.NEURepoManager;
import io.github.moulberry.repo.data.NEUItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text.Serialization;
import net.minecraft.util.Formatting;

//TODO: check inventory items, sum all repeated items into one
public class VisitorHelper {
    private static final Map<String, Map<String, Integer>> itemMap = new HashMap<>();
    private static final Map<String, NEUItem> itemCache = new HashMap<>();
    private static final int TEXT_START_X = 3;
    private static final int TEXT_START_Y = 13;
    private static final int LINE_SPACING = 3;

    public static void renderScreen(String title, DrawContext context, TextRenderer textRenderer,
            ScreenHandler handler, int mouseX, int mouseY) {
        if (handler.getCursorStack() == ItemStack.EMPTY)
            processVisitorItem(title, handler);
        drawScreenItems(context, textRenderer, mouseX, mouseY);
    }

    public static void onMouseClicked(double mouseX, double mouseY, int mouseButton, TextRenderer textRenderer) {
        int yPosition = TEXT_START_Y;

        for (var entry : itemMap.entrySet()) {
            String text = entry.getKey();
            int textWidth = textRenderer.getWidth(text);
            int textHeight = textRenderer.fontHeight;

            yPosition += LINE_SPACING + textHeight;

            for (var nestedEntry : entry.getValue().entrySet()) {
                String nestedText = nestedEntry.getKey();
                textWidth = textRenderer.getWidth(nestedText);

                if (isMouseOverText((int) mouseX, (int) mouseY, TEXT_START_X, yPosition, textWidth, textHeight)) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.player.networkHandler.sendChatMessage("/bz " + nestedText);
                    return;
                }
                yPosition += LINE_SPACING + textHeight;
            }
        }
    }

    public static void onSlotClick(Slot slot, int slotId, String title) {
        if (slotId == 29 || slotId == 13) {
            itemMap.remove(title);
        }
    }

    private static void processVisitorItem(String title, ScreenHandler handler) {
        var visitorItem = handler.getSlot(13).getStack();
        if (visitorItem != null && visitorItem.hasNbt() && visitorItem.getNbt().toString().contains("Times Visited")) {
            var acceptButton = handler.getSlot(29).getStack();
            if (acceptButton != null && acceptButton.hasNbt()) {
                var acceptButtonNbt = acceptButton.getNbt().getCompound("display");
                if (acceptButtonNbt.getType("Lore") == 9) {
                    processLore(title, acceptButtonNbt.getList("Lore", 8));
                }
            }
        }
    }

    private static void processLore(String title, NbtList nbtList) {
        boolean saveRequiredItems = false;
        for (var j = 0; j < nbtList.size(); j++) {
            var string = nbtList.getString(j);
            if (string.contains("Items Required"))
                saveRequiredItems = true;
            else if (string.contains("Rewards"))
                break;
            else if (saveRequiredItems)
                updateItemMap(title, string);
        }
    }

    private static void updateItemMap(String title, String lore) {
        var mutableText = Serialization.fromJson(lore);
        var split = mutableText.getString().split(" x");
        var itemName = split[0].trim();
        if (!itemName.isEmpty()) {
            var amount = split.length == 2 ? Integer.parseInt(split[1].trim()) : 1;
            Map<String, Integer> nestedMap = itemMap.getOrDefault(title, new HashMap<>());
            if (!nestedMap.containsKey(itemName)) {
                nestedMap.put(itemName, amount);
            }
            if (!itemMap.containsKey(title)) {
                itemMap.put(title, nestedMap);
            }
        }
    }

    private static void drawScreenItems(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        int index = 0;
        for (var entry : itemMap.entrySet()) {
            var primaryKey = entry.getKey();
            drawTextWithOptionalUnderline(context, textRenderer, primaryKey, TEXT_START_X,
                    TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)), -1, mouseX, mouseY);
            index++;

            for (var nestedEntry : entry.getValue().entrySet()) {
                index = drawItemEntryWithHover(context, textRenderer, nestedEntry, index, mouseX, mouseY);
            }
        }
    }

    private static int drawItemEntryWithHover(DrawContext context, TextRenderer textRenderer,
            Map.Entry<String, Integer> nestedEntry, int index, int mouseX, int mouseY) {
        var subItem = nestedEntry.getKey();
        var amount = nestedEntry.getValue();
        var item = getCachedItem(subItem);
        if (item != null) {
            var text = "  " + item.getDisplayName() + " x" + amount;
            drawTextWithOptionalUnderline(context, textRenderer, text, TEXT_START_X,
                    TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)), -1, mouseX, mouseY);
            drawItemStack(context, textRenderer, item, amount, index);
        }
        return index + 1;
    }

    private static NEUItem getCachedItem(String displayName) {
        var strippedName = Formatting.strip(displayName);
        var cachedItem = itemCache.get(strippedName);
        if (cachedItem == null) {
            var item = NEURepoManager.NEU_REPO.getItems().getItems().values().stream()
                    .filter(i -> Formatting.strip(i.getDisplayName()).equals(strippedName))
                    .findFirst()
                    .orElse(null);
            if (item != null) {
                itemCache.put(strippedName, item);
            }
            return item;
        }
        return cachedItem;
    }

    private static void drawTextWithOptionalUnderline(DrawContext context, TextRenderer textRenderer, String text,
            int x, int y, int color, int mouseX, int mouseY) {
        context.drawText(textRenderer, text, x, y, color, true);
        if (isMouseOverText(mouseX, mouseY, x, y, textRenderer.getWidth(text), textRenderer.fontHeight)) {
            context.drawHorizontalLine(x, x + textRenderer.getWidth(text), y + textRenderer.fontHeight, color);
        }
    }

    private static boolean isMouseOverText(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private static void drawItemStack(DrawContext context, TextRenderer textRenderer, NEUItem item, int amount,
            int index) {
        var stack = ItemRepository.getItemStack(item.getSkyblockItemId());
        var text = "  " + item.getDisplayName() + " x" + amount;
        context.drawItem(stack, TEXT_START_X + 2 + textRenderer.getWidth(text),
                TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)) - textRenderer.fontHeight + 5);
        context.drawText(textRenderer, text, TEXT_START_X,
                TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)), -1, true);
    }
}
