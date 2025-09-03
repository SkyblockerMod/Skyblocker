package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

/**
 * Abstract base class for a component that may be added to a Widget.
 */
public abstract class Component {
	public final Supplier<Integer> ICO_DIM = () -> SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 12 : 16;
	public static final int PAD_S = 2;
	public static final int PAD_L = 2;

	static final TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

	// these should always be the content dimensions without any padding.
	int width, height;

	private Widget parent;

	public abstract void render(DrawContext context, int x, int y);

	public void setParent(Widget parent) {
		this.parent = parent;
	}

	public Widget getParent() {
		return this.parent;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void renderIcon(DrawContext context, ItemStack icon, int x, int y) {
		context.getMatrices().pushMatrix();
		context.getMatrices().translate(x, y);
		context.getMatrices().scale((float) ICO_DIM.get() / 16);
		context.drawItem(icon, 0, 0);
		context.getMatrices().popMatrix();
	}
}
