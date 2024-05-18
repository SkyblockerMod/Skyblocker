package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class PreviewTab implements Tab {

    private final MinecraftClient client;

    public PreviewTab(MinecraftClient client) {
        this.client = client;
    }

    @Override
    public Text getTitle() {
        return Text.literal("Preview");
    }

    @Override
    public void forEachChild(Consumer<ClickableWidget> consumer) {

    }

    @Override
    public void refreshGrid(ScreenRect tabArea) {

    }

    public static class PreviewWidget extends ClickableWidget {

        public PreviewWidget(int x, int y, int width, int height, Text message) {
            super(x, y, width, height, message);
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            ScreenMaster.getScreenBuilder(Utils.getLocation());
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }
    }
}
