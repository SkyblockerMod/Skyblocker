package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.Element;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.ElementCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.element.PlainTextElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import de.hysky.skyblocker.utils.SkyBlockColors;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Abstract base class for a element based Widget.
 * Widgets are containers for elements with a border and a title.
 * Their size is dependent on the elements inside,
 * the position may be changed after construction.
 */
public abstract class ElementBasedWidget extends HudWidget implements ElementCollector {
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final Font txtRend = Minecraft.getInstance().font;

	private @Nullable String lastError = null;
	private static final List<Element> ERROR_ELEMENTS = List.of(new PlainTextElement(Component.literal("An error occurred! Please check logs.").withColor(0xFFFF0000)));

	private final ArrayList<Element> elements = new ArrayList<>();
	private List<Element> configElements;

	public static final int BORDER_SZE_N = txtRend.lineHeight + 2;
	public static final int BORDER_SZE_S = 4;
	public static final int BORDER_SZE_W = 4;
	public static final int BORDER_SZE_E = 4;
	public static final int DEFAULT_COL_BG_BOX = 0xC00C0C0C;
	// More transparent background for minimal style
	public static final int MINIMAL_COL_BG_BOX = 0x64000000;

	private final int color;
	private final Component title;

	private boolean lastRenderedConfig = false;

	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param title      title
	 * @param colorValue the colour
	 */
	public ElementBasedWidget(Component title, @Nullable Integer colorValue, Information information) {
		super(information);
		this.title = title;
		this.color = 0xFF000000 | (colorValue == null ? 0 : SkyBlockColors.fromVanilla(colorValue));
		configElements = List.of(new PlainTextElement(title.plainCopy()));
		pack(elements); // initial pack to limit weird rendering artifacts
	}

	public <T extends Element> T addElement(T c) {
		c.setParent(this);
		this.elements.add(c);
		return c;
	}

	public final boolean isEmpty() {
		return this.elements.isEmpty();
	}

	public final void update() {
		this.elements.clear();
		try {
			this.updateContent();
		} catch (Exception e) {
			if (e.getMessage() == null || !e.getMessage().equals(lastError)) {
				lastError = e.getMessage();
				LOGGER.error("Failed to update contents of {}", this, e);
			}
			this.elements.clear();
			this.elements.addAll(ERROR_ELEMENTS);
		}
		if (!lastRenderedConfig) this.pack(elements);
	}

	protected final void updateConfig() {
		ElementCollection collector = new ElementCollection();
		updateConfigContent(collector);
		if (!collector.getElements().isEmpty()) {
			configElements = collector.getElements();
		}
		this.pack(configElements);
	}

	@Override
	public void updateConfigPreview() {
		super.updateConfigPreview();
		updateConfig();
	}

	public abstract void updateContent();

	protected void updateConfigContent(ElementCollector collector) {
		// very basic default impl
		update();
		elements.forEach(collector::addElement);
	}

	public boolean shouldUpdateBeforeRendering() {
		return false;
	}

	@Override
	protected final void extractWidgetRenderState(GuiGraphicsExtractor context, float delta) {
		if (shouldUpdateBeforeRendering()) update();
		if (lastRenderedConfig) {
			lastRenderedConfig = false;
			pack(elements);
		}
		extractInternal(context, elements, false);
	}

	@Override
	protected void extractWidgetRenderStateForConfig(GuiGraphicsExtractor graphics, float delta) {
		if (!lastRenderedConfig) {
			lastRenderedConfig = true;
			pack(configElements);
		}
		extractInternal(graphics, configElements, true);
	}

	private void extractInternal(GuiGraphicsExtractor context, Collection<Element> elements, boolean config) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground) {
			Options options = Minecraft.getInstance().options;
			int textBackgroundColor = options.getBackgroundColor(SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal() ? MINIMAL_COL_BG_BOX : DEFAULT_COL_BG_BOX);
			context.fill(1, 0, w - 1, h, textBackgroundColor);
			context.fill(0, 1, 1, h - 1, textBackgroundColor);
			context.fill(w - 1, 1, w, h - 1, textBackgroundColor);
		}

		// Display Hypixel or Skyblocker widget in config mode.
		/*
		Component title = this.title;
		boolean isHypixelWidget = this instanceof TabHudWidget;
		if (config) {
			title = this.title.copy().append(" (").append(isHypixelWidget ? "Hypixel" : "Skyblocker").append(" Widget)");
			if (txtRend.width(title) + 8 >= w) {
				title = this.title.copy().append(" (").append(isHypixelWidget ? "Hypixel" : "Skyblocker").append(")");
			}
			if (txtRend.width(title) + 8 >= w) {
				title = this.title.copy().append(" (").append(isHypixelWidget ? "Hy" : "Skb").append(")");
			}
			if (txtRend.width(title) + 8 >= w) {
				title = this.title.copy().append(" (").append(isHypixelWidget ? "H" : "S").append(")");
			}
			if (txtRend.width(title) + 8 >= w) {
				title = this.title.copy().append(" ").append(isHypixelWidget ? "H" : "S");
			}
		}
		 */

		int strHeightHalf = txtRend.lineHeight / 2;
		int strAreaWidth = txtRend.width(title) + 4;

		context.text(txtRend, title, 8, 2, this.color, false);

		// Only draw borders if not in minimal mode
		if (!SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal()) {
			this.extractHorizontalLine(context, 2, 1 + strHeightHalf, 4);
			this.extractHorizontalLine(context, 2 + strAreaWidth + 4, 1 + strHeightHalf, w - 4 - 4 - strAreaWidth);
			this.extractHorizontalLine(context, 2, h - 2, w - 4);

			this.extractVerticalLine(context, 1, 2 + strHeightHalf, h - 4 - strHeightHalf);
			this.extractVerticalLine(context, w - 2, 2 + strHeightHalf, h - 4 - strHeightHalf);
		}

		int yOffs = BORDER_SZE_N;

		for (Element c : elements) {
			c.extractRenderState(context, BORDER_SZE_W, yOffs);
			yOffs += c.getHeight() + Element.PAD_L;
		}
	}

	/**
	 * Calculate the size of this widget.
	 * <b>Must be called before returning from the widget constructor and after all
	 * elements are added!</b>
	 */
	private void pack(Collection<Element> elements) {
		h = 0;
		w = 0;
		for (Element c : elements) {
			h += c.getHeight() + Element.PAD_L;
			w = Math.max(w, c.getWidth() + Element.PAD_S);
		}

		h -= Element.PAD_L / 2; // less padding after lowest/last element
		h += BORDER_SZE_N + BORDER_SZE_S - 2;
		w += BORDER_SZE_E + BORDER_SZE_W;

		// min width is dependent on title
		w = Math.max(w, BORDER_SZE_W + BORDER_SZE_E + txtRend.width(title) + 4 + 4 + 1);
	}

	private void extractHorizontalLine(GuiGraphicsExtractor graphics, int xpos, int ypos, int width) {
		graphics.fill(xpos, ypos, xpos + width, ypos + 1, this.color);
	}

	private void extractVerticalLine(GuiGraphicsExtractor graphics, int xpos, int ypos, int height) {
		graphics.fill(xpos, ypos, xpos + 1, ypos + height, this.color);
	}

}
