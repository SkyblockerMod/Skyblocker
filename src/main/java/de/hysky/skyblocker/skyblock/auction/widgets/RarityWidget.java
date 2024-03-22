package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;

public class RarityWidget extends ClickableWidget {

    private static final Identifier HOVER_TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/rarity_widget/hover.png");
    private static final Identifier TEXTURE = new Identifier(SkyblockerMod.NAMESPACE, "textures/gui/auctions_gui/rarity_widget/background.png");
    private final SlotClickHandler onClick;
    private int slotId = -1;

    public RarityWidget(int x, int y, SlotClickHandler onClick) {
        super(x, y, 48, 11, Text.literal("rarity selector thing, hi mom"));
        this.onClick = onClick;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate(getX(), getY(), 0);
        //context.drawText(parent.getTextRender(), String.valueOf(slotId), 0, -9, Colors.RED, true);
        boolean onLeftArrow = isOnLeftArrow(mouseX);
        boolean onRightArrow = isOnRightArrow(mouseX);
        context.drawTexture(TEXTURE, 0, 0, 0, 0, 48, 11, 48, 11);
        if (onLeftArrow) context.drawTexture(HOVER_TEXTURE, 0, 0, 0, 0, 6, 11, 6, 11);
        if (onRightArrow) context.drawTexture(HOVER_TEXTURE, 42, 0, 0, 0, 6, 11, 6, 11);

        // Text
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(current);
        int color = 0xFFEAEAEA;
        if (textWidth > 34) {
            matrices.push();
            //matrices.translate(-7-getX(), -5.5f-getY(), 0);
            matrices.translate(7, 5.5f, 0);
            matrices.scale(34.f/textWidth, 34.f/textWidth, 1.f);
            context.drawText(textRenderer, current, 0, -textRenderer.fontHeight/2, color, false);
            matrices.pop();
        } else {
            context.drawText(textRenderer, current, 7, 2, color, false);
        }

        matrices.pop();
        if (!onLeftArrow && !onRightArrow && isHovered()) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);

    }

    private boolean isOnRightArrow(int mouseX) {
        return isHovered() && mouseX - getX() > 40;
    }

    private boolean isOnLeftArrow(int mouseX) {
        return isHovered() && mouseX - getX() < 7;
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {

    }

    public void setSlotId(int slotId) {
        this.slotId = slotId;
    }

    private List<Text> tooltip = List.of();
    private String current = "?";

    public void setText(List<Text> tooltip, String current) {
        this.tooltip = tooltip;
        this.current = current;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (slotId == -1) return;
        if (isOnLeftArrow((int) mouseX)) {
            onClick.click(slotId, 1);
        } else if (isOnRightArrow((int) mouseX)) {
            onClick.click(slotId, 0);
        }
    }
}
