package de.hysky.skyblocker.skyblock.item.custom.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ArmorPresetsScreen extends Screen {
    private final Screen parent;
    private ArmorPresetListWidget list;

    public ArmorPresetsScreen(Screen parent) {
        super(Text.translatable("skyblocker.armorPresets.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (list == null) {
            list = new ArmorPresetListWidget(width, height - 28, 20, this::returnToParent);
        } else {
            list.setDimensions(width, height - 28);
        }
        addDrawableChild(list);
        ButtonWidget done = ButtonWidget.builder(ScreenTexts.DONE, b -> returnToParent())
                .width(100).position(width / 2 - 50, height - 20).build();
        addDrawableChild(done);
    }

    private void returnToParent() {
        client.setScreen(parent);
        if (parent instanceof CustomizeArmorScreen cas) {
            cas.updateWidgets();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, 8, 0xFFFFFF);
    }

    @Override
    public void close() {
        returnToParent();
    }
}
