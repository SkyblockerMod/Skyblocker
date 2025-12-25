package de.hysky.skyblocker.utils.render.gui;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.utils.EnumUtils;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * A widget designed for cycling through a set of textures, represented by an enum.
 *
 * @param <T> The type of the enum entries, which must be an {@link Enum} and implement {@link Supplier}{@code <}{@link ResourceLocation}{@code >}.
 */
public class CyclingTextureWidget<T extends Enum<T> & Supplier<ResourceLocation>> extends AbstractWidget {

	private Function<T, Component> textSupplier = t -> Component.nullToEmpty(t.name());
	private Function<T, Tooltip> tooltipSupplier = t -> Tooltip.create(Component.translationArg(textSupplier.apply(t)));
	private Consumer<T> onCycle = t -> {};
	private T current;

	private static final WidgetSprites BUTTON = new WidgetSprites(ResourceLocation.withDefaultNamespace("widget/button"),
			ResourceLocation.withDefaultNamespace("widget/button_disabled"), ResourceLocation.withDefaultNamespace("widget/button_highlighted"));

	public CyclingTextureWidget(int x, int y, int width, int height, T initial) {
		super(x, y, width, height, Component.empty());
		this.current = initial;
		this.setTooltip(tooltipSupplier.apply(initial));
	}

	public void setCycleListener(Consumer<T> onCycle) {
		this.onCycle = onCycle;
	}

	public void setTextSupplier(Function<T, Component> textSupplier) {
		this.textSupplier = textSupplier;
		setTooltip(tooltipSupplier.apply(getCurrent()));
	}

	public void setTooltipSupplier(Function<T, Tooltip> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		setTooltip(tooltipSupplier.apply(getCurrent()));
	}

	@Override
	public void onClick(MouseButtonEvent click, boolean doubled) {
		this.current = EnumUtils.cycle(current);
		this.setTooltip(tooltipSupplier.apply(getCurrent()));
		this.onCycle.accept(getCurrent());
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		var button = BUTTON.get(this.active, this.isHoveredOrFocused());
		context.blitSprite(RenderPipelines.GUI_TEXTURED, button, this.getX(),
				this.getY(), width, height);
		context.blit(RenderPipelines.GUI_TEXTURED, getCurrent().get(),
				this.getX(), this.getY(), 0, 0, width, height, width, height);

		if (this.isHovered()) {
			context.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {
		builder.add(NarratedElementType.TITLE, this.textSupplier.apply(getCurrent()));
	}

	public T getCurrent() {
		return current;
	}
}
