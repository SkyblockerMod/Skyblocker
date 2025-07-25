package de.hysky.skyblocker.skyblock.item.custom.screen;

import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPreset;
import de.hysky.skyblocker.skyblock.item.custom.preset.ArmorPresets;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.ArrayList;
import java.util.List;

public class ArmorPresetListWidget extends ElementListWidget<ArmorPresetListWidget.Row> {
    private static final int ITEM_HEIGHT = ArmorPresetCardWidget.HEIGHT + 4;
    private final Runnable closeAction;

    public ArmorPresetListWidget(int width, int height, int y, Runnable closeAction) {
        super(MinecraftClient.getInstance(), width, height, y, ITEM_HEIGHT);
        this.closeAction = closeAction;
        updateEntries();
    }

    @Override
    public int getRowWidth() {
        return width;
    }

    private void updateEntries() {
        clearEntries();
        var presets = ArmorPresets.getInstance().getPresets();
        int cardsPerRow = Math.max(1, this.width / (ArmorPresetCardWidget.WIDTH + 8));
        for (int i = 0; i < presets.size(); i += cardsPerRow) {
            List<ArmorPreset> row = presets.subList(i, Math.min(i + cardsPerRow, presets.size()));
            addEntry(new Row(row));
        }
    }

    public class Row extends Entry<Row> {
        private final List<ArmorPresetCardWidget> cards = new ArrayList<>();

        Row(List<ArmorPreset> presets) {
            for (ArmorPreset p : presets) {
                cards.add(new ArmorPresetCardWidget(p, () -> {
                    ArmorPresets.getInstance().apply(p);
                    closeAction.run();
                }));
            }
        }

        @Override
        public List<? extends Element> children() {
            return cards;
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return cards;
        }

        @Override
        public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            int totalWidth = cards.size() * (ArmorPresetCardWidget.WIDTH + 8) - 8;
            int startX = x + (entryWidth - totalWidth) / 2;
            for (int i = 0; i < cards.size(); i++) {
                ArmorPresetCardWidget card = cards.get(i);
                card.setPosition(startX + i * (ArmorPresetCardWidget.WIDTH + 8), y + 2);
                card.render(context, mouseX, mouseY, tickDelta);
            }
        }
    }
}
