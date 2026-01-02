package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import org.jetbrains.annotations.Nullable;

/**
 * Component that consists of a player's skin icon and their name
 */
public class PlayerComponent extends Component {

	private static final int SKIN_ICO_DIM = 8;
	private final net.minecraft.network.chat.Component name;
	private final Identifier tex;

	public PlayerComponent(PlayerInfo ple) {
		this(ple, null);
	}

	public PlayerComponent(PlayerInfo ple, net.minecraft.network.chat.@Nullable Component name) {
		this.name = name == null ? ple.getTabListDisplayName() : name;
		this.tex = ple.getSkin().body().texturePath();

		this.width = SKIN_ICO_DIM + PAD_S + txtRend.width(this.name);
		this.height = txtRend.lineHeight;
	}

	@Override
	public void render(GuiGraphics context, int x, int y) {
		PlayerFaceRenderer.draw(context, tex, x, y, SKIN_ICO_DIM, true, false, -1);
		context.drawString(txtRend, name, x + SKIN_ICO_DIM + PAD_S, y, CommonColors.WHITE, false);
	}
}
