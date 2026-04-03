package de.hysky.skyblocker.skyblock.tabhud.widget.element;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.ItemUtils;

import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

/**
 * Abstract base class for an element that may be added to a Widget.
 */
public abstract class Element {
	public final Supplier<Integer> ICO_DIM = () -> SkyblockerConfigManager.get().uiAndVisuals.tabHud.compactWidgets ? 12 : 16;
	public static final int PAD_S = 2;
	public static final int PAD_L = 2;

	static final Font txtRend = Minecraft.getInstance().font;

	// these should always be the content dimensions without any padding.
	int width, height;

	private LayoutElement parent = SpacerElement.width(0);

	public abstract void extractRenderState(GuiGraphicsExtractor graphics, int x, int y);

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

	public void extractIcon(GuiGraphicsExtractor graphics, FlexibleItemStack icon, int x, int y) {
		ItemStack stack = icon.getStack();

		graphics.pose().pushMatrix();
		graphics.pose().translate(x, y);

		if (stack != null) {
			graphics.pose().scale((float) ICO_DIM.get() / 16);
			graphics.item(stack, 0, 0);
		} else {
			graphics.pose().scale(2f);
			graphics.pose().scale((float) ICO_DIM.get() / 16);
			graphics.text(txtRend, ItemUtils.getIcon(icon), 0, 0, CommonColors.WHITE, false);
		}

		graphics.pose().popMatrix();
	}
}
