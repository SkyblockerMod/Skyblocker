package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import de.hysky.skyblocker.skyblock.item.ItemRarityBackgrounds;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

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
        boolean onLeftArrow = isOnLeftArrow(mouseX);
        boolean onRightArrow = isOnRightArrow(mouseX);
        context.drawTexture(TEXTURE, 0, 0, 0, 0, 48, 11, 48, 11);
        if (onLeftArrow) context.drawTexture(HOVER_TEXTURE, 0, 0, 0, 0, 6, 11, 6, 11);
        if (onRightArrow) context.drawTexture(HOVER_TEXTURE, 42, 0, 0, 0, 6, 11, 6, 11);

        // Text
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(current);
        if (textWidth > 34) {
            float scale = 34f / textWidth;
            matrices.push();
            matrices.translate(0, 5.5, 0);
            matrices.scale(scale, scale, 1);
            context.drawCenteredTextWithShadow(textRenderer, current, (int) (24 / scale), -textRenderer.fontHeight / 2, color);
            matrices.pop();
        } else {
            context.drawCenteredTextWithShadow(textRenderer, current, 24, 2, color);
        }

        matrices.pop();
        if (!onLeftArrow && !onRightArrow && isHovered()) context.drawTooltip(textRenderer, tooltip, mouseX, mouseY);

    }

    private boolean isOnRightArrow(double mouseX) {
        return isHovered() && mouseX - getX() > 40;
    }

    private boolean isOnLeftArrow(double mouseX) {
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
    private int color = 0xFFEAEAEA;

    public void setText(List<Text> tooltip, String current) {
        this.tooltip = tooltip;
        this.current = current;
        for (Map.Entry<String, SkyblockItemRarity> rarity : ItemRarityBackgrounds.LORE_RARITIES.entrySet()) {
            if (current.toUpperCase().contains(rarity.getKey())) {
                this.color = rarity.getValue().color | 0xFF000000;
                return;
            }
        }
        //noinspection DataFlowIssue
        this.color = Formatting.GRAY.getColorValue() | 0xFF000000;
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (slotId == -1) return;
        if (isOnLeftArrow(mouseX)) {
            onClick.click(slotId, 1);
        } else if (isOnRightArrow(mouseX)) {
            onClick.click(slotId, 0);
        }
    }
}
