package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Component that consists of a player's skin icon and their name
 */
public class PlayerComponent extends Component {

	private static final int SKIN_ICO_DIM = 8;

	private final Text name;
	private final Identifier tex;

	public PlayerComponent(PlayerListEntry ple) {

		name = ple.getDisplayName();
		tex = ple.getSkinTextures().texture();

		this.width = SKIN_ICO_DIM + PAD_S + txtRend.getWidth(name);
		this.height = txtRend.fontHeight;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		PlayerSkinDrawer.draw(context, tex, x, y, SKIN_ICO_DIM, true, false, -1);
		context.drawText(txtRend, name, x + SKIN_ICO_DIM + PAD_S, y, 0xffffffff, false);
	}

}
