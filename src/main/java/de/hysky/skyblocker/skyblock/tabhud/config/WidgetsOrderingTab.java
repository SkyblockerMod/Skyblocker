package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.config.entries.BooleanEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.DefaultEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.EditableEntry;
import de.hysky.skyblocker.skyblock.tabhud.config.entries.WidgetEntry;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class WidgetsOrderingTab implements Tab {

    private final WidgetsElementList widgetsElementList;
    private final ButtonWidget back;
    private final ButtonWidget previousPage;
    private final ButtonWidget nextPage;
    private GenericContainerScreenHandler handler;
    private final MinecraftClient client;
    private boolean waitingForServer = false;

    public WidgetsOrderingTab(MinecraftClient client, GenericContainerScreenHandler handler) {
        widgetsElementList = new WidgetsElementList(this, client, 0, 0, 0);
        this.client = client;
        this.handler = handler;
        back = ButtonWidget.builder(Text.literal("Back"), button -> clickAndWaitForServer(48, 0))
                .size(64, 12)
                .build();
        previousPage = ButtonWidget.builder(Text.literal("Previous Page"), button -> clickAndWaitForServer(45, 0))
                .size(100, 12)
                .build();
        nextPage = ButtonWidget.builder(Text.literal("Next Page"), button -> clickAndWaitForServer(53, 0))
                .size(100, 12)
                .build();
    }

    @Override
    public Text getTitle() {
        return Text.literal("Widgets");
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {
        consumer.accept(back);
        consumer.accept(previousPage);
        consumer.accept(nextPage);
        consumer.accept(widgetsElementList);
    }

    public void clickAndWaitForServer(int slot, int button) {
        if (waitingForServer) return;
        if (client.interactionManager == null || this.client.player == null) return;
        client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.PICKUP, this.client.player);
        waitingForServer = true;
    }

    public void shiftClickAndWaitForServer(int slot, int button) {
        if (waitingForServer) return;
        if (client.interactionManager == null || this.client.player == null) return;
        client.interactionManager.clickSlot(handler.syncId, slot, button, SlotActionType.QUICK_MOVE, this.client.player);
        waitingForServer = true;
    }

    public void updateHandler(GenericContainerScreenHandler newHandler) {
        this.handler = newHandler;
    }

    public void hopper(@Nullable List<Text> hopperTooltip) {
        if (hopperTooltip == null) {
            widgetsElementList.setEditingPosition(-1);
            return;
        }
        int start = -1;
        int editing = 1;
        for (int i = 0; i < hopperTooltip.size(); i++) {
            Text text = hopperTooltip.get(i);
            String string = text.getString();
            System.out.println(string);
            if (start == -1 && string.contains("▶")) {
                start = i;
            }
            if (string.contains("(EDITING)")) {
                editing = i;
                break;
            }
        }
        widgetsElementList.setEditingPosition(editing - start);
    }

    public void updateEntries(String titleLowercase) {
        waitingForServer = false;
        widgetsElementList.clearEntries();
        for (int i = titleLowercase.equals("tablist widgets") ? 9: 18; i < handler.getRows() * 9 - 9; i++) {
            Slot slot = handler.getSlot(i);
            ItemStack stack = slot.getStack();
            if (stack.isEmpty() || stack.isOf(Items.BLACK_STAINED_GLASS_PANE)) continue;
            String lowerCase = stack.getName().getString().trim().toLowerCase();
            List<Text> lore = ItemUtils.getLore(stack);
            String lastLowerCase = lore.getLast().getString().toLowerCase();
            if (lowerCase.startsWith("widgets on") || lowerCase.startsWith("widgets in") || lastLowerCase.contains("click to edit")) {
                widgetsElementList.addEntry(new EditableEntry(this, i, stack));
            } else if (lowerCase.endsWith("widget")) {
                widgetsElementList.addEntry(new WidgetEntry(this, i, stack));
            } else if (lastLowerCase.contains("enable") || lastLowerCase.contains("disable")) {
                widgetsElementList.addEntry(new BooleanEntry(this, i, stack));
            } else {
                widgetsElementList.addEntry(new DefaultEntry(this, i, stack));
            }
        }
        // Force it to update the scrollbar (it is stupid)
        widgetsElementList.setScrollAmount(widgetsElementList.getScrollAmount());
        previousPage.visible = handler.getRows() == 6 && handler.getSlot(45).getStack().isOf(Items.ARROW);
        nextPage.visible = handler.getRows() == 6 && handler.getSlot(53).getStack().isOf(Items.ARROW);
    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {
        back.setPosition(16, tabArea.getTop() + 4);
        widgetsElementList.setY(tabArea.getTop());
        widgetsElementList.setDimensions(tabArea.width(), tabArea.height() - 20);
        previousPage.setPosition(widgetsElementList.getRowLeft(), widgetsElementList.getBottom() + 4);
        nextPage.setPosition(widgetsElementList.getScrollbarX() - 100, widgetsElementList.getBottom() + 4);
    }
}
