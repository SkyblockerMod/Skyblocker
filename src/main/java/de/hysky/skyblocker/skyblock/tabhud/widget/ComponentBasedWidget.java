package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.IcoTextComponent;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for a component based Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
public abstract class ComponentBasedWidget extends HudWidget {
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final TextRenderer txtRend = MinecraftClient.getInstance().textRenderer;

	private String lastError = null;
	private static final List<Component> ERROR_COMPONENTS = List.of(new PlainTextComponent(Text.literal("An error occurred! Please check logs.").withColor(0xFFFF0000)));

	private final ArrayList<Component> components = new ArrayList<>();

	private int prevW = 0, prevH = 0;

	static final int BORDER_SZE_N = txtRend.fontHeight + 4;
	static final int BORDER_SZE_S = 4;
	static final int BORDER_SZE_W = 4;
	static final int BORDER_SZE_E = 4;
	static final int DEFAULT_COL_BG_BOX = 0xc00c0c0c;
	// More transparent background for minimal style
	static final int MINIMAL_COL_BG_BOX = 0x64000000;

	private final int color;
	private final Text title;

	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param title      title
	 * @param colorValue the colour
	 * @param internalID the internal ID, for config, positioning depending on other widgets, all that good stuff
	 */
	public ComponentBasedWidget(MutableText title, Integer colorValue, String internalID) {
		super(internalID);
		this.title = title;
		this.color = 0xff000000 | colorValue;
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
		this.pack();
	}

	public abstract void updateContent();

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
	public final void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		MatrixStack ms = context.getMatrices();

		ms.push();

		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground) {
			GameOptions options = MinecraftClient.getInstance().options;
			int textBackgroundColor = options.getTextBackgroundColor(SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal() ? MINIMAL_COL_BG_BOX : DEFAULT_COL_BG_BOX);
			context.fill(x + 1, y, x + w - 1, y + h, textBackgroundColor);
			context.fill(x, y + 1, x + 1, y + h - 1, textBackgroundColor);
			context.fill(x + w - 1, y + 1, x + w, y + h - 1, textBackgroundColor);
		}
		// move above background (if exists)
		ms.translate(0, 0, 100);

		int strHeightHalf = txtRend.fontHeight / 2;
		int strAreaWidth = txtRend.getWidth(title) + 4;

		context.drawText(txtRend, title, x + 8, y + 2, this.color, false);

		// Only draw borders if not in minimal mode
		if (!SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal()) {
			this.drawHLine(context, x + 2, y + 1 + strHeightHalf, 4);
			this.drawHLine(context, x + 2 + strAreaWidth + 4, y + 1 + strHeightHalf, w - 4 - 4 - strAreaWidth);
			this.drawHLine(context, x + 2, y + h - 2, w - 4);

			this.drawVLine(context, x + 1, y + 2 + strHeightHalf, h - 4 - strHeightHalf);
			this.drawVLine(context, x + w - 2, y + 2 + strHeightHalf, h - 4 - strHeightHalf);
		}

		int yOffs = y + BORDER_SZE_N;

		for (Component c : components) {
			c.render(context, x + BORDER_SZE_W, yOffs);
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
	private void pack() {
		h = 0;
		w = 0;
		for (Component c : components) {
			h += c.getHeight() + Component.PAD_L;
			w = Math.max(w, c.getWidth() + Component.PAD_S);
		}

		h -= Component.PAD_L / 2; // less padding after lowest/last component
		h += BORDER_SZE_N + BORDER_SZE_S - 2;
		w += BORDER_SZE_E + BORDER_SZE_W;

		// min width is dependent on title
		w = Math.max(w, BORDER_SZE_W + BORDER_SZE_E + txtRend.getWidth(title) + 4 + 4 + 1);
		// update the positions so it doesn't wait for the next tick or something
		if (h != prevH || w != prevW) ScreenBuilder.markDirty();
		prevW = w;
		prevH = h;
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
