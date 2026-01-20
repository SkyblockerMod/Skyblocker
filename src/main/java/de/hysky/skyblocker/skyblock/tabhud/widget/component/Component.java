package de.hysky.skyblocker.skyblock.tabhud.widget.component;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.world.item.ItemStack;

/**
 * Abstract base class for a component that may be added to a Widget.
 */
public abstract class Component {
	public final Supplier<Integer> ICO_DIM = () -> SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 12 : 16;
	public static final int PAD_S = 2;
	public static final int PAD_L = 2;

	static final Font txtRend = Minecraft.getInstance().font;

	// these should always be the content dimensions without any padding.
	int width, height;

	private LayoutElement parent;

	public abstract void render(GuiGraphics context, int x, int y);

	public void setParent(LayoutElement parent) {
		this.parent = parent;
	}

	public LayoutElement getParent() {
		return this.parent;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public void renderIcon(GuiGraphics context, ItemStack icon, int x, int y) {
		context.pose().pushMatrix();
		context.pose().translate(x, y);
		context.pose().scale((float) ICO_DIM.get() / 16);
		context.renderItem(icon, 0, 0);
		context.pose().popMatrix();
	}
}
