package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jspecify.annotations.Nullable;

/**
 * Element that consists of a player's skin icon and their name
 */
public class PlayerElement extends Element {

	private static final int SKIN_ICO_DIM = 8;
	private final Component name;
	private final Identifier tex;

	public PlayerElement(PlayerInfo ple) {
		this(ple, null);
	}

	public PlayerElement(PlayerInfo ple, @Nullable Component name) {
		this.name = name == null ? ple.getTabListDisplayName() : name;
		this.tex = ple.getSkin().body().texturePath();

		this.width = SKIN_ICO_DIM + PAD_S + txtRend.width(this.name);
		this.height = txtRend.lineHeight;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int x, int y) {
		PlayerFaceExtractor.extractRenderState(graphics, tex, x, y, SKIN_ICO_DIM, true, false, -1);
		graphics.text(txtRend, name, x + SKIN_ICO_DIM + PAD_S, y, CommonColors.WHITE, false);
	}
}
