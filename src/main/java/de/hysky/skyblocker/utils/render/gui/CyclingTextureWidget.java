package de.hysky.skyblocker.utils.render.gui;

import de.hysky.skyblocker.utils.Identifiable;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.screen.narration.NarrationPart;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A widget designed for cycling through a set of textures, represented by an enum.
 *
 * @param <T> The type of the enum entries, which must be an {@link Enum} and implement {@link Identifiable}.
 */
public class CyclingTextureWidget<T extends Enum<T> & Identifiable> extends ClickableWidget {

	private final T[] entries;
	private Function<T, Text> textSupplier = t -> Text.of(t.name());
	private Function<T, Tooltip> tooltipSupplier = t -> Tooltip.of(Text.of(textSupplier.apply(t)));
	private Consumer<T> onCycle = t -> {};
	private int currentIndex;

	private static final ButtonTextures BUTTON = new ButtonTextures(Identifier.ofVanilla("widget/button"),
			Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));

	public CyclingTextureWidget(int x, int y, int width, int height, Class<T> clazz) {
		super(x, y, width, height, Text.empty());
		this.entries = clazz.getEnumConstants();
		if (this.entries.length == 0) {
			throw new IllegalArgumentException("Cannot cycle through an empty array of textures.");
		}
		this.currentIndex = 0;
		this.setTooltip(tooltipSupplier.apply(this.entries[0]));
	}

	public void setCycleListener(Consumer<T> onCycle) {
		this.onCycle = onCycle;
	}

	public void setTextSupplier(Function<T, Text> textSupplier) {
		this.textSupplier = textSupplier;
		setTooltip(tooltipSupplier.apply(getCurrent()));
	}

	public void setTooltipSupplier(Function<T, Tooltip> tooltipSupplier) {
		this.tooltipSupplier = tooltipSupplier;
		setTooltip(tooltipSupplier.apply(getCurrent()));
	}

	@Override
	public void onClick(double mouseX, double mouseY) {
		currentIndex = (currentIndex + 1) % entries.length;
		this.setTooltip(tooltipSupplier.apply(getCurrent()));
		this.onCycle.accept(getCurrent());
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		var button = BUTTON.get(this.active, this.isFocused());
		context.drawGuiTexture(RenderLayer::getGuiTextured, button, this.getX(),
				this.getY(), width, height);
		context.drawTexture(RenderLayer::getGuiTextured, getCurrent().identify(),
				this.getX(), this.getY(), 0, 0, width, height, width, height);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		builder.put(NarrationPart.TITLE, this.textSupplier.apply(getCurrent()));
	}

	public T getCurrent() {
		return entries[currentIndex];
	}
}
