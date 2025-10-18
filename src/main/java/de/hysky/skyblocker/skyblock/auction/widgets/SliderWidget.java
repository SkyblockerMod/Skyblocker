package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

// This is kinda excessive, but I thought it was a good idea
public class SliderWidget<E extends Enum<E> & SliderWidget.OptionInfo> extends ClickableWidget {
    private final SlotClickHandler clickSlot;
    private int button = 0;
    private int slotId = -1;

    protected E current;

    float posProgress;

    /**
     * @param x             x position
     * @param y             y position
     * @param width         width
     * @param height        height
     * @param message       probably useless, just put the widget name
     * @param clickSlot     the parent AuctionsBrowser
     * @param defaultOption the default option <strong>should be the one at ordinal 0</strong>
     */
    public SliderWidget(int x, int y, int width, int height, Text message, SlotClickHandler clickSlot, E defaultOption) {
        super(x, y, width, height, message);
        this.clickSlot = clickSlot;
        this.current = defaultOption;
        posProgress = current.getOffset();
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (posProgress < current.getOffset()) {
            posProgress += delta * 5;
            if (posProgress > current.getOffset()) posProgress = current.getOffset();
        } else if (posProgress > current.getOffset()) {
            posProgress -= delta * 5;
            if (posProgress < current.getOffset()) posProgress = current.getOffset();
        }


        context.getMatrices().pushMatrix();
        context.getMatrices().translate(getX(), getY());

        int x = current.isVertical() ? 0 : Math.round(posProgress);
        int y = current.isVertical() ? Math.round(posProgress) : 0;

        int optionWidth = current.getOptionSize()[0];
        int optionHeight = current.getOptionSize()[1];

        context.drawTexture(RenderPipelines.GUI_TEXTURED, current.getBackTexture(), 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        context.drawTexture(RenderPipelines.GUI_TEXTURED, current.getOptionTexture(), x, y, 0, 0, optionWidth, optionHeight, optionWidth, optionHeight);
        if (isHovered()) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, current.getHoverTexture(), x, y, 0, 0, optionWidth, optionHeight, optionWidth, optionHeight);

        }
        context.getMatrices().popMatrix();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (slotId == -1) return;
        clickSlot.click(slotId, button);
        super.onClick(mouseX, mouseY);
    }

    @Override
    protected boolean isValidClickButton(int button) {
        this.button = button;
        return super.isValidClickButton(button) || button == 1;
    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    public void setCurrent(E current) {
        this.current = current;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
    }

    public interface OptionInfo {
        boolean isVertical();

        /**
         * @return The current option's position offset from the first option's position
         */
        int getOffset();

        int[] getOptionSize();

        Identifier getOptionTexture();

        Identifier getBackTexture();

        Identifier getHoverTexture();

    }
}
