package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.option.BooleanOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.FloatOption;
import de.hysky.skyblocker.skyblock.tabhud.config.option.WidgetOption;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.ColorHelper;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Abstract base class for a component based Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
public abstract class ComponentBasedWidget extends HudWidget {
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

	private static final List<Component> ERROR_COMPONENTS = List.of(new PlainTextComponent(Text.literal("An error occurred! Please check logs.").withColor(0xFFFF0000)));

	static final int BORDER_SZE_N = txtRend.fontHeight + 4;
	static final int BORDER_SZE_S = 4;
	static final int BORDER_SZE_W = 4;
	static final int BORDER_SZE_E = 4;
	static final int DEFAULT_COL_BG_BOX = 0xc00c0c0c;
	// More transparent background for minimal style
	static final int MINIMAL_COL_BG_BOX = 0x64000000;

	private final int color;
	private final Text title;
	private final ArrayList<Component> components = new ArrayList<>();
	private String lastError = null;
	private final @NotNull Information information;

	protected boolean drawBorder = true;
	protected float backgroundOpacity = 0.75f;

	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param title      title
	 * @param color the color for the border
	 */
	public ComponentBasedWidget(Text title, int color, @NotNull Information information) {
		this.title = title;
		this.color = 0xff000000 | color;
		this.information = information;
	}

	public ComponentBasedWidget(Text title, int color, String id) {
		this(title, color, new Information(id, title.copyContentOnly(), l -> true));
	}

	public ComponentBasedWidget(Text title, int color, String id, Predicate<Location> availableIn) {
		this(title, color, new Information(id, title.copyContentOnly(), availableIn));
	}

	/**
	 * @param availableLocations {@link java.util.EnumSet} IS VERY ENCOURAGED.
	 */
	public ComponentBasedWidget(Text title, int color, String id, Set<Location> availableLocations) {
		this(title, color, id, availableLocations::contains);
	}

	public ComponentBasedWidget(Text title, int color, String id, Location availableLocation) {
		this(title, color, id, EnumSet.of(availableLocation));
	}

	public ComponentBasedWidget(Text title, int color, String id, Location availableLocation, Location... otherAvailableLocations) {
		this(title, color, id, EnumSet.of(availableLocation, otherAvailableLocations));
	}

	public void addComponent(Component c) {
		this.components.add(c);
	}

	public final void update() {
		this.components.clear();
		try {
			this.updateContent();
		} catch (Exception e) {
			if (e.getMessage() == null || !e.getMessage().equals(lastError)) {
				lastError = e.getMessage();
				LOGGER.error("Failed to update contents of {}", this, e);
			}
			this.components.clear();
			this.components.addAll(ERROR_COMPONENTS);
		}
		this.pack(this.components);
	}

	/**
	 * @see ComponentBasedWidget#shouldUpdateBeforeRendering()
	 */
	public abstract void updateContent();

	protected abstract List<Component> getConfigComponents();

	/**
	 * @return true if this should be updated before rendering.
	 * @implNote Will not,update if {@link HudWidget#shouldRender()} is false.
	 */
	public boolean shouldUpdateBeforeRendering() {
		return false;
	}

	/**
	 * Shorthand function for simple components.
	 * If the entry at idx has the format "[textA]: [textB]", an IcoTextComponent is
	 * added as such:
	 * [ico] [string] [textB.formatted(fmt)]
	 */
	public final void addSimpleIcoText(ItemStack ico, String string, Formatting fmt, int idx) {
		Text txt = simpleEntryText(idx, string, fmt);
		this.addComponent(Components.iconTextComponent(ico, txt));
	}

	public final void addSimpleIcoText(ItemStack ico, String string, Formatting fmt, String content) {
		Text txt = simpleEntryText(content, string, fmt);
		this.addComponent(Components.iconTextComponent(ico, txt));
	}

	public final void addSimpleIconTranslatableText(ItemStack icon, @Translatable String translationKey, Formatting formatting, String content) {
		Text text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addComponent(new IcoTextComponent(icon, text));
	}

	public final void addSimpleIconTranslatableText(ItemStack icon, @Translatable String translationKey, Formatting formatting, Text content) {
		Text text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addComponent(new IcoTextComponent(icon, text));
	}

	@Override
	public final void renderWidget(DrawContext context, float delta) {
		if (shouldUpdateBeforeRendering()) update();
		renderInternal(context, this.components);
	}

	@Override
	public void renderConfig(DrawContext context, float delta) {
		// TODO do not pack every time maybe
		List<Component> configComponents = getConfigComponents();
		this.pack(configComponents);
		this.renderInternal(context, configComponents);
	}

	private void renderInternal(DrawContext context, List<Component> components) {
		MatrixStack ms = context.getMatrices();

		ms.push();

		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground && backgroundOpacity > 0) {
			int textBackgroundColor = ColorHelper.fromFloats(backgroundOpacity, 0, 0, 0);
			context.fill(1, 0, w - 1, h, textBackgroundColor);
			context.fill(0, 1, 1, h - 1, textBackgroundColor);
			context.fill(w - 1, 1, w, h - 1, textBackgroundColor);
		}
		// move above background (if exists)
		ms.translate(0, 0, 10);

		int strHeightHalf = txtRend.fontHeight / 2;
		int strWidth = txtRend.getWidth(title);
		int strAreaWidth = strWidth + 4;

		if (drawBorder) {
			context.drawText(txtRend, title, 8, 2, this.color, false);
		} else {
			context.drawText(txtRend, title, (w - strWidth) / 2, 2, this.color, false);
		}

		// Only draw borders if not in minimal mode
		if (!SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal() && drawBorder) {
			this.drawHLine(context, 2, 1 + strHeightHalf, 4);
			this.drawHLine(context, 2 + strAreaWidth + 4, 1 + strHeightHalf, w - 4 - 4 - strAreaWidth);
			this.drawHLine(context, 2, h - 2, w - 4);

			this.drawVLine(context, 1, 2 + strHeightHalf, h - 4 - strHeightHalf);
			this.drawVLine(context, w - 2, 2 + strHeightHalf, h - 4 - strHeightHalf);
		}

		int yOffs = BORDER_SZE_N + (drawBorder ? 0 : -2);

		for (Component c : components) {
			c.render(context, BORDER_SZE_W + (drawBorder ? 0 : -3), yOffs);
			yOffs += c.getHeight() + Component.PAD_L;
		}
		// pop manipulations above
		ms.pop();
	}

	/**
	 * Calculate the size of this widget.
	 * <b>Must be called before returning from the widget constructor and after all
	 * components are added!</b>
	 */
	private void pack(List<Component> components) {
		h = 0;
		w = 0;
		for (Component c : components) {
			h += c.getHeight() + Component.PAD_L;
			w = Math.max(w, c.getWidth() + Component.PAD_S);
		}

		h -= Component.PAD_L / 2; // less padding after lowest/last component
		h += BORDER_SZE_N + BORDER_SZE_S - 2 + (drawBorder ? 0 : -6);
		w += BORDER_SZE_E + BORDER_SZE_W + (drawBorder ? 0 : -6);

		// min width is dependent on title
		w = Math.max(w, BORDER_SZE_W + BORDER_SZE_E + (drawBorder ? 0 : -6) + txtRend.getWidth(title) + 4 + 4 + 1);
		// update the positions so it doesn't wait for the next tick or something
	}

	@Override
	public @NotNull Information getInformation() {
		return information;
	}

	@Override
	public void getOptions(List<WidgetOption<?>> options) {
		super.getOptions(options);
		options.add(new BooleanOption("draw_border", Text.literal("Draw Border"), () -> drawBorder, b -> drawBorder = b));
		options.add(new FloatOption("background_opacity", Text.literal("Background Opacity"), () -> backgroundOpacity, b -> backgroundOpacity = b));
	}

	private void drawHLine(DrawContext context, int xpos, int ypos, int width) {
		context.fill(xpos, ypos, xpos + width, ypos + 1, this.color);
	}

	private void drawVLine(DrawContext context, int xpos, int ypos, int height) {
		context.fill(xpos, ypos, xpos + 1, ypos + height, this.color);
	}

	/**
	 * If the entry at idx has the format "[textA]: [textB]", the following is
	 * returned:
	 * [entryName] [textB.formatted(contentFmt)]
	 */
	public static Text simpleEntryText(int idx, String entryName, Formatting contentFmt) {

		String src = PlayerListManager.strAt(idx);

		if (src == null) {
			return null;
		}

		int cidx = src.indexOf(':');
		if (cidx == -1) {
			return null;
		}

		src = src.substring(src.indexOf(':') + 1);
		return simpleEntryText(src, entryName, contentFmt);
	}

	/**
	 * @return [entryName] [entryContent.formatted(contentFmt)]
	 */
	public static Text simpleEntryText(String entryContent, String entryName, Formatting contentFmt) {
		return Text.literal(entryName).append(Text.literal(entryContent).formatted(contentFmt));
	}

	public static Text simpleEntryTranslatableText(String translationKey, String content, Formatting contentFormatting) {
		return Text.translatable(translationKey, Text.literal(content).formatted(contentFormatting));
	}

	public static Text simpleEntryTranslatableText(String translationKey, Text content, Formatting contentFormatting) {
		return Text.translatable(translationKey, content.copy().formatted(contentFormatting));
	}

	/**
	 * @return the entry at idx as unformatted Text
	 */
	public static Text plainEntryText(int idx) {
		String str = PlayerListManager.strAt(idx);
		if (str == null) {
			return null;
		}
		return Text.of(str);
	}
}
