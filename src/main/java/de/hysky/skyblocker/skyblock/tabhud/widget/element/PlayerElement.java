package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.config.SkyblockerConfigManager;

/**
 * Element that consists of a player's skin icon and their name
 */
public class PlayerElement extends Element {

	private static final int SKIN_ICO_DIM = 8;
	private final Component name;
	private final Identifier tex;
	private final int iconDim;
	private final int textYOffset;

	public PlayerElement(PlayerInfo ple) {
		this(ple, null, false);
	}

	public PlayerElement(PlayerInfo ple, @Nullable Component name, boolean large) {
		this.name = name != null ? name : ple.getTabListDisplayName() != null ? ple.getTabListDisplayName() : Component.literal("No data").withStyle(ChatFormatting.GRAY);
		this.tex = ple.getSkin().body().texturePath();

		this.iconDim = large ? ICO_DIM.get() : SKIN_ICO_DIM;
		this.textYOffset = large ? (SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 2 : 4) : 0;

		this.width = this.iconDim + PAD_S + txtRend.width(this.name);
		this.height = Math.max(this.iconDim, txtRend.lineHeight);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		PlayerFaceExtractor.extractRenderState(graphics, tex, x, y, iconDim, true, false, -1);
		graphics.text(txtRend, name, x + iconDim + PAD_S, y + textYOffset, CommonColors.WHITE, false);
	}
}
