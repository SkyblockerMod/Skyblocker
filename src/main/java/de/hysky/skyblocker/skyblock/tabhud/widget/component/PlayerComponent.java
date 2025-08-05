package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.PlayerSkinDrawer;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

/**
 * Component that consists of a player's skin icon and their name
 */
public class PlayerComponent extends Component {

	private static final int SKIN_ICO_DIM = 8;

	private final Text name;
	private final Identifier tex;

	public PlayerComponent(PlayerListEntry ple) {
		this(ple, null);
	}

	public PlayerComponent(PlayerListEntry ple, @Nullable Text name) {
		this.name = name == null ? ple.getDisplayName() : name;
		this.tex = ple.getSkinTextures().texture();

		this.width = SKIN_ICO_DIM + PAD_S + txtRend.getWidth(this.name);
		this.height = txtRend.fontHeight;
	}

	@Override
	public void render(DrawContext context, int x, int y) {
		PlayerSkinDrawer.draw(context, tex, x, y, SKIN_ICO_DIM, true, false, -1);
		context.drawText(txtRend, name, x + SKIN_ICO_DIM + PAD_S, y, Colors.WHITE, false);
	}

}
