package de.hysky.skyblocker.skyblock.tabhud.widget;

import com.demonwav.mcdev.annotations.Translatable;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Component;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.Components;
import de.hysky.skyblocker.skyblock.tabhud.widget.component.PlainTextComponent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Abstract base class for a component based Widget.
 * Widgets are containers for components with a border and a title.
 * Their size is dependent on the components inside,
 * the position may be changed after construction.
 */
public abstract class ComponentBasedWidget extends HudWidget {
	public static final Logger LOGGER = LogUtils.getLogger();

	private static final Font txtRend = Minecraft.getInstance().font;

	private @Nullable String lastError = null;
	private static final List<Component> ERROR_COMPONENTS = List.of(new PlainTextComponent(net.minecraft.network.chat.Component.literal("An error occurred! Please check logs.").withColor(0xFFFF0000)));

	private final ArrayList<Component> components = new ArrayList<>();

	private int prevW = 0, prevH = 0;

	public static final int BORDER_SZE_N = txtRend.lineHeight + 2;
	public static final int BORDER_SZE_S = 4;
	public static final int BORDER_SZE_W = 4;
	public static final int BORDER_SZE_E = 4;
	public static final int DEFAULT_COL_BG_BOX = 0xC00C0C0C;
	// More transparent background for minimal style
	public static final int MINIMAL_COL_BG_BOX = 0x64000000;

	private final int color;
	private final net.minecraft.network.chat.Component title;

	/**
	 * Most often than not this should be instantiated only once.
	 *
	 * @param title      title
	 * @param colorValue the colour
	 * @param internalID the internal ID, for config, positioning depending on other widgets, all that good stuff
	 */
	public ComponentBasedWidget(MutableComponent title, Integer colorValue, String internalID) {
		super(internalID);
		this.title = title;
		this.color = 0xFF000000 | colorValue;
	}

	public void addComponent(Component c) {
		c.setParent(this);
		this.components.add(c);
	}

	public final boolean isEmpty() {
		return this.components.isEmpty();
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
	public final void addSimpleIcoText(@Nullable ItemStack ico, String string, ChatFormatting fmt, int idx) {
		net.minecraft.network.chat.Component txt = simpleEntryText(idx, string, fmt);
		this.addComponent(Components.iconTextComponent(ico, txt));
	}

	public final void addSimpleIcoText(@Nullable ItemStack ico, String string, ChatFormatting fmt, String content) {
		net.minecraft.network.chat.Component txt = simpleEntryText(content, string, fmt);
		this.addComponent(Components.iconTextComponent(ico, txt));
	}

	public final void addSimpleIconTranslatableText(@Nullable ItemStack icon, @Translatable String translationKey, ChatFormatting formatting, String content) {
		net.minecraft.network.chat.Component text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addComponent(Components.iconTextComponent(icon, text));
	}

	public final void addSimpleIconTranslatableText(ItemStack icon, @Translatable String translationKey, ChatFormatting formatting, net.minecraft.network.chat.Component content) {
		net.minecraft.network.chat.Component text = simpleEntryTranslatableText(translationKey, content, formatting);
		this.addComponent(Components.iconTextComponent(icon, text));
	}

	@Override
	public final void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		if (SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground) {
			Options options = Minecraft.getInstance().options;
			int textBackgroundColor = options.getBackgroundColor(SkyblockerConfigManager.get().uiAndVisuals.tabHud.style.isMinimal() ? MINIMAL_COL_BG_BOX : DEFAULT_COL_BG_BOX);
			context.fill(x + 1, y, x + w - 1, y + h, textBackgroundColor);
			context.fill(x, y + 1, x + 1, y + h - 1, textBackgroundColor);
			context.fill(x + w - 1, y + 1, x + w, y + h - 1, textBackgroundColor);
		}

		int strHeightHalf = txtRend.lineHeight / 2;
		int strAreaWidth = txtRend.width(title) + 4;

		context.drawString(txtRend, title, x + 8, y + 2, this.color, false);

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
		w = Math.max(w, BORDER_SZE_W + BORDER_SZE_E + txtRend.width(title) + 4 + 4 + 1);
		// update the positions so it doesn't wait for the next tick or something
		if (h != prevH || w != prevW) ScreenBuilder.markDirty();
		prevW = w;
		prevH = h;
	}

	private void drawHLine(GuiGraphics context, int xpos, int ypos, int width) {
		context.fill(xpos, ypos, xpos + width, ypos + 1, this.color);
	}

	private void drawVLine(GuiGraphics context, int xpos, int ypos, int height) {
		context.fill(xpos, ypos, xpos + 1, ypos + height, this.color);
	}

	/**
	 * If the entry at idx has the format "[textA]: [textB]", the following is
	 * returned:
	 * [entryName] [textB.formatted(contentFmt)]
	 */
	public static net.minecraft.network.chat.@Nullable Component simpleEntryText(int idx, String entryName, ChatFormatting contentFmt) {

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
	public static net.minecraft.network.chat.Component simpleEntryText(String entryContent, String entryName, ChatFormatting contentFmt) {
		return net.minecraft.network.chat.Component.literal(entryName).append(net.minecraft.network.chat.Component.literal(entryContent).withStyle(contentFmt));
	}

	public static net.minecraft.network.chat.Component simpleEntryTranslatableText(String translationKey, String content, ChatFormatting contentFormatting) {
		return net.minecraft.network.chat.Component.translatable(translationKey, net.minecraft.network.chat.Component.literal(content).withStyle(contentFormatting));
	}

	public static net.minecraft.network.chat.Component simpleEntryTranslatableText(String translationKey, net.minecraft.network.chat.Component content, ChatFormatting contentFormatting) {
		return net.minecraft.network.chat.Component.translatable(translationKey, content.copy().withStyle(contentFormatting));
	}
}
