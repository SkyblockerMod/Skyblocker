package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import io.github.moulberry.repo.data.NEUItem;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.Text.Serialization;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

//TODO: check inventory items, sum all repeated items into one
public class VisitorHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger("Skyblocker Visitor Helper");

    private static final Map<String, Object2IntMap<String>> itemMap = new HashMap<>();
    private static final Map<String, ItemStack> itemCache = new HashMap<>();
    private static final int TEXT_START_X = 4;
    private static final int TEXT_START_Y = 4;
    private static final int LINE_SPACING = 3;

    public static void renderScreen(String title, DrawContext context, TextRenderer textRenderer, ScreenHandler handler, int mouseX, int mouseY) {
        if (handler.getCursorStack() == ItemStack.EMPTY)
            processVisitorItem(title, handler);
        drawScreenItems(context, textRenderer, mouseX, mouseY);
    }

    public static void onMouseClicked(double mouseX, double mouseY, int mouseButton, TextRenderer textRenderer) {
        int yPosition = TEXT_START_Y;

        for (Map.Entry<String, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
            int textWidth;
            int textHeight = textRenderer.fontHeight;

            yPosition += LINE_SPACING + textHeight;

            for (Object2IntMap.Entry<String> itemEntry : visitorEntry.getValue().object2IntEntrySet()) {
                String itemText = itemEntry.getKey();
                textWidth = textRenderer.getWidth(itemText);

                if (isMouseOverText(mouseX, mouseY, TEXT_START_X, yPosition, textWidth, textHeight)) {
                    MessageScheduler.INSTANCE.sendMessageAfterCooldown("/bz " + itemText);
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

    private static void processVisitorItem(String visitorName, ScreenHandler handler) {
        ItemStack visitorItem = handler.getSlot(13).getStack();
        if (visitorItem == null || !visitorItem.hasNbt() || !visitorItem.getNbt().asString().contains("Times Visited")) return;
        ItemStack acceptButton = handler.getSlot(29).getStack();
        if (acceptButton == null) return;
        NbtCompound acceptButtonNbt = acceptButton.getSubNbt("display");
        if (acceptButtonNbt == null || !acceptButtonNbt.contains("Lore", NbtElement.LIST_TYPE)) return;
        processLore(visitorName, acceptButtonNbt.getList("Lore", NbtElement.STRING_TYPE));
    }

    private static void processLore(String visitorName, NbtList loreList) {
        boolean saveRequiredItems = false;
        for (int i = 0; i < loreList.size(); i++) {
            String lore = loreList.getString(i);
            if (lore.contains("Items Required"))
                saveRequiredItems = true;
            else if (lore.contains("Rewards"))
                break;
            else if (saveRequiredItems)
                updateItemMap(visitorName, lore);
        }
    }

    private static void updateItemMap(String visitorName, String lore) {
        Text itemText = Serialization.fromJson(lore);
        String[] splitItemText = itemText.getString().split(" x");
        String itemName = splitItemText[0].trim();
        if (itemName.isEmpty()) return;
        try {
            int amount = splitItemText.length == 2 ? NumberFormat.getInstance().parse(splitItemText[1].trim()).intValue() : 1;
            Object2IntMap<String> visitorMap = itemMap.getOrDefault(visitorName, new Object2IntOpenHashMap<>());
            visitorMap.putIfAbsent(itemName, amount);
            itemMap.putIfAbsent(visitorName, visitorMap);
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Visitor Helper] Failed to parse item: " + itemText.getString(), e);
        }
    }

    private static void drawScreenItems(DrawContext context, TextRenderer textRenderer, int mouseX, int mouseY) {
        int index = 0;
        for (Map.Entry<String, Object2IntMap<String>> visitorEntry : itemMap.entrySet()) {
            String visitorName = visitorEntry.getKey();
            drawTextWithOptionalUnderline(context, textRenderer, Text.literal(visitorName), TEXT_START_X, TEXT_START_Y + index * (LINE_SPACING + textRenderer.fontHeight), mouseX, mouseY);
            index++;

            for (Object2IntMap.Entry<String> itemEntry : visitorEntry.getValue().object2IntEntrySet()) {
                index = drawItemEntryWithHover(context, textRenderer, itemEntry, index, mouseX, mouseY);
            }
        }
    }

    private static int drawItemEntryWithHover(DrawContext context, TextRenderer textRenderer, Map.Entry<String, Integer> itemEntry, int index, int mouseX, int mouseY) {
        String itemName = itemEntry.getKey();
        int amount = itemEntry.getValue();
        ItemStack stack = getCachedItem(itemName);
        if (stack != null) {
            drawItemEntryWithHover(context, textRenderer, stack, amount, index, mouseX, mouseY);
        }
        return index + 1;
    }

    private static ItemStack getCachedItem(String displayName) {
        String strippedName = Formatting.strip(displayName);
        ItemStack cachedStack = itemCache.get(strippedName);
        if (cachedStack != null) return cachedStack;
        NEUItem neuItem = NEURepoManager.NEU_REPO.getItems().getItems().values().stream()
                .filter(i -> Formatting.strip(i.getDisplayName()).equals(strippedName))
                .findFirst()
                .orElse(null);
        if (neuItem == null) return null;
        ItemStack stack = ItemRepository.getItemStack(neuItem.getSkyblockItemId());
        itemCache.put(strippedName, stack);
        return stack;
    }

    private static void drawItemEntryWithHover(DrawContext context, TextRenderer textRenderer, ItemStack stack, int amount, int index, int mouseX, int mousseY) {
        Text text = Serialization.fromJson(stack.getSubNbt("display").getString("Name")).append(" x" + amount);
        drawTextWithOptionalUnderline(context, textRenderer, text, TEXT_START_X + 8, TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)), mouseX, mousseY);
        context.drawItem(stack, TEXT_START_X + 10 + textRenderer.getWidth(text), TEXT_START_Y + (index * (LINE_SPACING + textRenderer.fontHeight)) - textRenderer.fontHeight + 5);
    }

    private static void drawTextWithOptionalUnderline(DrawContext context, TextRenderer textRenderer, Text text, int x, int y, int mouseX, int mouseY) {
        context.drawText(textRenderer, text, x, y, -1, true);
        if (isMouseOverText(mouseX, mouseY, x, y, textRenderer.getWidth(text), textRenderer.fontHeight)) {
            context.drawHorizontalLine(x, x + textRenderer.getWidth(text), y + textRenderer.fontHeight, -1);
        }
    }

    private static boolean isMouseOverText(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
