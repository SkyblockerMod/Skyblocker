package de.hysky.skyblocker.skyblock.quicknav;

import com.mojang.blaze3d.systems.RenderSystem;

import de.hysky.skyblocker.mixin.accessor.HandledScreenAccessor;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
@Environment(value=EnvType.CLIENT)
public class QuickNavButton extends ClickableWidget {
    private final int index;
    private boolean toggled;
    private final String command;
    private final ItemStack icon;

    /**
     * Checks if the current tab is a top tab based on its index.
     * @return true if the index is less than 6, false otherwise.
     */
    private boolean isTopTab() {
        return index < 7;
    }

    /**
     * Constructs a new QuickNavButton with the given parameters.
     * @param index the index of the button.
     * @param toggled the toggled state of the button.
     * @param command the command to execute when the button is clicked.
     * @param icon the icon to display on the button.
     */
    public QuickNavButton(int index, boolean toggled, String command, ItemStack icon) {
        super(0, 0, 26, 32, Text.empty());
        this.index = index;
        this.toggled = toggled;
        this.command = command;
        this.icon = icon;
    }
    private void updateCoordinates() {
        Screen screen = MinecraftClient.getInstance().currentScreen;
        if (screen instanceof HandledScreen<?> handledScreen) {
            int x = ((HandledScreenAccessor)handledScreen).getX();
            int y = ((HandledScreenAccessor)handledScreen).getY();
            int h = ((HandledScreenAccessor)handledScreen).getBackgroundHeight();
            if (h > 166) --h; // why is this even a thing
            this.setX(x + this.index % 7 * 25);
            this.setY(this.index < 7 ? y - 25 : y + h - 4);
        }
    }

    /**
     * Handles click events. If the button is not currently toggled,
     * it sets the toggled state to true and sends a message with the command after cooldown.
     * @param mouseX the x-coordinate of the mouse click
     * @param mouseY the y-coordinate of the mouse click
     */
    @Override
    public void onClick(double mouseX, double mouseY) {
        if (!this.toggled) {
            this.toggled = true;
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(command);
            // TODO : add null check with log error
        }
    }

    /**
     * Renders the button on screen. This includes both its texture and its icon.
     * The method first updates the coordinates of the button,
     * then calculates appropriate values for rendering based on its current state,
     * and finally draws both the background and icon of the button on screen.
     * @param context the context in which to render the button
     * @param mouseX the x-coordinate of the mouse cursor
     * @param mouseY the y-coordinate of the mouse cursor
     */
    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.updateCoordinates();
        RenderSystem.disableDepthTest();

        // Construct the texture identifier based on the index and toggled state
        String tabType = isTopTab() ? "top" : "bottom";
        Identifier BUTTON_TEXTURES = new Identifier("container/creative_inventory/tab_" + tabType +
                (toggled ? "_selected_" : "_unselected_") + 2);

        // Render the button texture
        int y = this.getY();
        if (this.toggled) {
            if (this.index < 7) y -= 2;
        } else {
            y += (this.index >= 7) ? 4 : -2;
        }
        int height = this.height - ((this.toggled ) ? 0 : 4);

        context.drawGuiTexture(BUTTON_TEXTURES, this.getX(), y, this.width - 1, height);

        // Render the button icon
        int yOffset = !this.toggled && this.index < 7 ? 1 : 0;
        context.drawItem(this.icon, this.getX() + 4, this.getY() + 6 + yOffset);

        RenderSystem.enableDepthTest();
    }

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		// TODO Auto-generated method stub

	}
}
