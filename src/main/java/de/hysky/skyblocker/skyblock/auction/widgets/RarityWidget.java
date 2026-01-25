package de.hysky.skyblocker.skyblock.auction.widgets;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.auction.SlotClickHandler;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import org.joml.Matrix3x2fStack;

public class RarityWidget extends AbstractWidget {

	private static final Identifier LEFT_TEXTURE = SkyblockerMod.id("auctions_gui/rarity_widget/left");
	private static final Identifier LEFT_HOVER_TEXTURE = SkyblockerMod.id("auctions_gui/rarity_widget/left_hover");
	private static final Identifier RIGHT_TEXTURE = SkyblockerMod.id("auctions_gui/rarity_widget/right");
	private static final Identifier RIGHT_HOVER_TEXTURE = SkyblockerMod.id("auctions_gui/rarity_widget/right_hover");
	private static final Identifier BACKGROUND = SkyblockerMod.id("auctions_gui/rarity_widget/background");
	private final SlotClickHandler onClick;
	private int slotId = -1;

	public RarityWidget(int x, int y, SlotClickHandler onClick) {
		super(x, y, 48, 11, Component.literal("rarity selector thing, hi mom"));
		this.onClick = onClick;
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		Matrix3x2fStack matrices = context.pose();
		matrices.pushMatrix();
		matrices.translate(getX(), getY());
		boolean onLeftArrow = isOnLeftArrow(mouseX);
		boolean onRightArrow = isOnRightArrow(mouseX);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, 6, 0, 36, 11);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, onLeftArrow ? LEFT_HOVER_TEXTURE : LEFT_TEXTURE, 0, 0, 6, 11);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, onRightArrow ? RIGHT_HOVER_TEXTURE : RIGHT_TEXTURE, 42, 0, 6, 11);

		// Text
		Font textRenderer = Minecraft.getInstance().font;
		int textWidth = textRenderer.width(current);
		if (textWidth > 34) {
			float scale = 34f / textWidth;
			matrices.pushMatrix();
			matrices.translate(0f, 5.5f);
			matrices.scale(scale, scale);
			context.drawCenteredString(textRenderer, current, (int) (24 / scale), -textRenderer.lineHeight / 2, color);
			matrices.popMatrix();
		} else {
			context.drawCenteredString(textRenderer, current, 24, 2, color);
		}

		matrices.popMatrix();
		if (!onLeftArrow && !onRightArrow && isHovered()) context.setComponentTooltipForNextFrame(textRenderer, tooltip, mouseX, mouseY);

	}

	private boolean isOnRightArrow(double mouseX) {
		return isHovered() && mouseX - getX() > 40;
	}

	private boolean isOnLeftArrow(double mouseX) {
		return isHovered() && mouseX - getX() < 7;
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {

	}

	public void setSlotId(int slotId) {
		this.slotId = slotId;
	}

	private List<Component> tooltip = List.of();
	private String current = "?";
	private int color = 0xFFEAEAEA;

	public void setText(List<Component> tooltip, String current) {
		this.tooltip = tooltip;
		this.current = current;
		this.color = ARGB.opaque(SkyblockItemRarity.containsName(current.toUpperCase(Locale.ENGLISH)).map(r -> r.color).orElse(CommonColors.GRAY));
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		if (slotId == -1) return;
		if (isOnLeftArrow(click.x())) {
			onClick.click(slotId, 1);
		} else if (isOnRightArrow(click.x())) {
			onClick.click(slotId, 0);
		}
	}
}
