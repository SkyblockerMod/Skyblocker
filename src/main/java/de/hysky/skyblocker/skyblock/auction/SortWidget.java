package de.hysky.skyblocker.skyblock.auction;

import de.hysky.skyblocker.SkyblockerMod;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;

public class SortWidget extends ClickableWidget {
    private static final Identifier BACK_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE,"textures/gui/auctions_gui/sort_widget/back.png");

    private final AuctionsBrowserScreen parent;
    private int button = 0;
    private int slotId = -1;

    private Option current = Option.HIGH;

    public SortWidget(int x, int y, AuctionsBrowserScreen parent) {
        super(x, y, 36, 9, Text.literal("Sort"));
        this.parent = parent;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        context.getMatrices().push();
        context.getMatrices().translate(getX(), getY(), 0);
        context.drawText(parent.getTextRender(), String.valueOf(slotId), 0, -9, Colors.RED, true);
        context.drawTexture(BACK_TEXTURE, 0, 0, 0, 0, getWidth(), getHeight(), getWidth(), getHeight());
        context.drawTexture(current.getTexture(), current.ordinal() * Option.OFFSET, 0, 0, 0, 21, 9, 21, 9);
        if (isHovered()) {
            context.drawTexture(Option.HOVER_TEXTURE, current.ordinal() * Option.OFFSET, 0, 0, 0, 21, 9, 21, 9);

        }
        context.getMatrices().pop();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (parent.isWaitingForServer() || slotId == -1) return;
        parent.clickAndWaitForServer(slotId, button);
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

    public void setCurrent(Option current) {
        this.current = current;
    }

    public enum Option {
        HIGH("high.png"),
        LOW("low.png"),
        SOON("soon.png"),
        RAND("rand.png");

        public static final int OFFSET = 5;

        private final Identifier texture;
        public static final Identifier HOVER_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/sort_widget/hover.png");
        Option(String textureName) {
            texture = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/sort_widget/" + textureName);
        }
        public Identifier getTexture() {
            return texture;
        }

        private static final Option[] values = values();
        public static Option get(int ordinal) {return values[MathHelper.clamp(ordinal, 0, values.length-1)];}
    }
}
