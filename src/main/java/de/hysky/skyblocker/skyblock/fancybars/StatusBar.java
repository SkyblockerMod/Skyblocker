package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jspecify.annotations.Nullable;

import java.awt.Color;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.StringRepresentable;

public class StatusBar implements LayoutElement, Renderable, GuiEventListener, NarratableEntry {
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final Identifier BAR_BACK = SkyblockerMod.id("bars/bar_back");

	public static final int ICON_SIZE = 9;

	private final Identifier icon;
	private final StatusBarType type;
	private Color[] colors;
	private @Nullable Color textColor;

	public Color[] getColors() {
		return colors;
	}

	public boolean hasOverflow() {
		return type.hasOverflow();
	}

	public boolean hasMax() {
		return type.hasMax();
	}

	public @Nullable Color getTextColor() {
		return textColor;
	}

	public void setTextColor(@Nullable Color textColor) {
		this.textColor = textColor;
	}

	public Component getName() {
		return type.getName();
	}

	private @Nullable OnClick onClick = null;
	public int gridX = 0;
	public int gridY = 0;
	public float x = 0;
	public float y = 0;
	public float width = 0;
	public BarPositioner.@Nullable BarAnchor anchor = null;

	public int size = 1;

	public float fill = 0;
	public float overflowFill = 0;
	public boolean inMouse = false;
	/**
	 * Used to hide the bar dynamically, like the oxygen bar
	 */
	public boolean visible = true;
	public boolean enabled = true;

	private Object value = "???";
	private @Nullable Object max = "???";
	private @Nullable Object overflow = "???";

	private int renderX = 0;
	private int renderY = 0;
	private int renderWidth = 0;

	private IconPosition iconPosition = IconPosition.LEFT;
	private TextPosition textPosition = TextPosition.BAR_CENTER;

	public boolean showMax = false;
	public boolean showOverflow = false;

	public StatusBar(StatusBarType type) {
		this.icon = SkyblockerMod.id("bars/icons/" + type.getSerializedName());
		this.colors = type.getColors();
		this.textColor = type.getTextColor();
		this.type = type;
	}

	protected int transparency(int color) {
		if (inMouse) return (color & 0x00FFFFFF) | 0x44_000000;
		return color;
	}

	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		renderBar(context);
		if (enabled) renderText(context);
	}

	protected Identifier getIcon() {
		return icon;
	}

	public void renderBar(GuiGraphics context) {
		if (renderWidth <= 0) return;
		int transparency = transparency(-1);
		switch (iconPosition) {
			case LEFT -> context.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(), renderX, renderY, ICON_SIZE, ICON_SIZE, transparency);
			case RIGHT -> context.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(), renderX + renderWidth - ICON_SIZE, renderY, ICON_SIZE, ICON_SIZE, transparency);
		}

		int barWidth = iconPosition.equals(IconPosition.OFF) ? renderWidth : renderWidth - ICON_SIZE - 1;
		int barX = iconPosition.equals(IconPosition.LEFT) ? renderX + ICON_SIZE + 1 : renderX;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACK, barX, renderY + 1, barWidth, 7, transparency);
		drawBarFill(context, barX, barWidth);
		//context.drawText(MinecraftClient.getInstance().textRenderer, gridX + " " + gridY + " s:" + size , x, y-9, Colors.WHITE, true);
	}

	protected void drawBarFill(GuiGraphics context, int barX, int barWith) {
		HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, renderY + 2, (int) ((barWith - 2) * fill), 5, transparency(colors[0].getRGB()));

		if (hasOverflow() && overflowFill > 0) {
			HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, renderY + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(colors[1].getRGB()));
		}
	}

	public void updateValues(float fill, float overflowFill, Object text, @Nullable Object max, @Nullable Object overflow) {
		this.value = text;
		this.fill = Math.clamp(fill, 0, 1);
		this.overflowFill = Math.clamp(overflowFill, 0, 1);
		this.max = max;
		this.overflow = overflow;
	}

	public void updateWithResource(StatusBarTracker.Resource resource) {
		updateValues(resource.value() / (float) resource.max(), resource.overflow() / (float) resource.max(), resource.value(), resource.max(), resource.overflow() > 0 ? resource.overflow() : null);
	}

	public void renderText(GuiGraphics context) {
		if (!showText()) return;
		Font textRenderer = Minecraft.getInstance().font;
		int barWidth = iconPosition.equals(IconPosition.OFF) ? renderWidth : renderWidth - ICON_SIZE - 1;
		int barX = iconPosition.equals(IconPosition.LEFT) ? renderX + ICON_SIZE + 2 : renderX;
		String stringValue = this.value.toString();
		MutableComponent text = Component.literal(stringValue).withStyle(style -> style.withColor((textColor == null ? colors[0] : textColor).getRGB()));

		if (hasMax() && showMax && max != null) {
			text.append("/").append(max.toString());
		}
		if (hasOverflow() && showOverflow && overflow != null) {
			MutableComponent literal = Component.literal(" + ").withStyle(style -> style.withColor(colors[1].getRGB()));
			literal.append(overflow.toString());
			text.append(literal);
		}

		int textWidth = textRenderer.width(text);
		int x;
		switch (textPosition) {
			case RIGHT -> x = barX + barWidth - textWidth;
			case CENTER -> x = this.renderX + (renderWidth - textWidth) / 2;
			case BAR_CENTER -> x = barX + (barWidth - textWidth) / 2;
			default -> x = barX; // Put on the left by default because I said so.
		}
		int y = this.renderY - 3;

		int color = transparency((textColor == null ? colors[0] : textColor).getRGB());
		int outlineColor = transparency(CommonColors.BLACK);

		HudHelper.drawOutlinedText(context, Component.translationArg(text), x, y, color, outlineColor);
	}

	public void renderCursor(GuiGraphics context, int mouseX, int mouseY, float delta) {
		int temp_x = renderX;
		int temp_y = renderY;
		boolean temp_ghost = inMouse;

		renderX = mouseX;
		renderY = mouseY;
		inMouse = false;

		render(context, mouseX, mouseY, delta);

		renderX = temp_x;
		renderY = temp_y;
		inMouse = temp_ghost;
	}

	// GUI shenanigans

	@Override
	public void setX(int x) {
		this.renderX = x;
	}

	@Override
	public void setY(int y) {
		this.renderY = y;
	}

	@Override
	public int getX() {
		return renderX;
	}

	@Override
	public int getY() {
		return renderY;
	}

	@Override
	public int getWidth() {
		return renderWidth;
	}

	public void setWidth(int width) {
		this.renderWidth = width;
	}

	@Override
	public int getHeight() {
		return 9;
	}

	@Override
	public ScreenRectangle getRectangle() {
		return LayoutElement.super.getRectangle();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= renderX && mouseX <= renderX + getWidth() && mouseY >= renderY && mouseY <= renderY + getHeight();
	}

	@Override
	public void visitWidgets(Consumer<AbstractWidget> consumer) {
	}

	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return NarrationPriority.NONE;
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (!isMouseOver(click.x(), click.y())) return false;
		if (onClick != null) {
			onClick.onClick(this, click);
		}
		return true;
	}

	public void setOnClick(@Nullable OnClick onClick) {
		this.onClick = onClick;
	}

	@Override
	public void updateNarration(NarrationElementOutput builder) {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", getName())
				.append("gridX", gridX)
				.append("gridY", gridY)
				.append("size", size)
				.append("x", renderX)
				.append("y", renderY)
				.append("width", renderWidth)
				.append("anchor", anchor)
				.toString();
	}

	public IconPosition getIconPosition() {
		return iconPosition;
	}

	public void setIconPosition(IconPosition iconPosition) {
		this.iconPosition = iconPosition;
	}

	public boolean showText() {
		return textPosition != TextPosition.OFF;
	}

	public TextPosition getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(TextPosition textPosition) {
		this.textPosition = textPosition;
	}

	public enum IconPosition implements StringRepresentable {
		LEFT,
		RIGHT,
		OFF;

		@Override
		public String getSerializedName() {
			return name();
		}

		@Override
		public String toString() {
			return I18n.get("skyblocker.bars.config.commonPosition." + name());
		}
	}

	public enum TextPosition implements StringRepresentable {
		LEFT,
		CENTER,
		BAR_CENTER,
		RIGHT,
		OFF;

		@Override
		public String getSerializedName() {
			return name();
		}

		@Override
		public String toString() {
			if (this == CENTER || this == BAR_CENTER) return I18n.get("skyblocker.bars.config.textPosition." + name());
			return I18n.get("skyblocker.bars.config.commonPosition." + name());
		}
	}

	@FunctionalInterface
	public interface OnClick {
		void onClick(StatusBar statusBar, MouseButtonEvent click);
	}

	public void loadFromJson(JsonObject object) {
		// Make colors optional, so it's easy to reset to default
		if (object.has("colors")) {
			JsonArray colors1 = object.get("colors").getAsJsonArray();
			if (colors1.size() < 2 && hasOverflow()) {
				throw new IllegalStateException("Missing second color of bar that has overflow");
			}
			Color[] newColors = new Color[colors1.size()];
			for (int i = 0; i < colors1.size(); i++) {
				JsonElement jsonElement = colors1.get(i);
				newColors[i] = new Color(Integer.parseInt(jsonElement.getAsString(), 16));
			}
			this.colors = newColors;
		}

		if (object.has("text_color")) this.textColor = new Color(Integer.parseInt(object.get("text_color").getAsString(), 16));

		String maybeAnchor = object.get("anchor").getAsString().trim();
		this.anchor = maybeAnchor.equals("null") ? null : BarPositioner.BarAnchor.valueOf(maybeAnchor);
		if (!object.has("enabled")) {
			enabled = anchor != null;
		} else enabled = object.get("enabled").getAsBoolean();
		if (anchor != null) {
			this.size = object.get("size").getAsInt();
			this.gridX = object.get("x").getAsInt();
			this.gridY = object.get("y").getAsInt();
		} else {
			this.width = object.get("size").getAsFloat();
			this.x = object.get("x").getAsFloat();
			this.y = object.get("y").getAsFloat();
		}
		// these are optional too, why not
		if (object.has("icon_position")) this.iconPosition = IconPosition.valueOf(object.get("icon_position").getAsString().trim());
		// backwards compat teehee
		if (object.has("show_text")) this.textPosition = object.get("show_text").getAsBoolean() ? TextPosition.BAR_CENTER : TextPosition.OFF;
		if (object.has("text_position")) this.textPosition = TextPosition.valueOf(object.get("text_position").getAsString().trim());
		if (object.has("show_max")) this.showMax = object.get("show_max").getAsBoolean();
		if (object.has("show_overflow")) this.showOverflow = object.get("show_overflow").getAsBoolean();
	}

	public JsonObject toJson() {
		JsonObject object = new JsonObject();
		JsonArray colors1 = new JsonArray();
		for (Color color : colors) {
			colors1.add(Integer.toHexString(color.getRGB()).substring(2));
		}
		object.add("colors", colors1);
		if (textColor != null) {
			object.addProperty("text_color", Integer.toHexString(textColor.getRGB()).substring(2));
		}
		if (anchor != null) {
			object.addProperty("anchor", anchor.toString());
		} else object.addProperty("anchor", "null");
		if (anchor != null) {
			object.addProperty("x", gridX);
			object.addProperty("y", gridY);
			object.addProperty("size", size);
		} else {
			object.addProperty("size", width);
			object.addProperty("x", x);
			object.addProperty("y", y);
		}
		object.addProperty("icon_position", iconPosition.getSerializedName());
		object.addProperty("text_position", textPosition.getSerializedName());
		object.addProperty("show_max", showMax);
		object.addProperty("show_overflow", showOverflow);
		object.addProperty("enabled", enabled);
		return object;
	}

	public static class ManaStatusBar extends StatusBar {

		public ManaStatusBar(StatusBarType type) {
			super(type);
		}

		@Override
		protected void drawBarFill(GuiGraphics context, int barX, int barWith) {
			if (hasOverflow() && overflowFill > 0) {
				if (overflowFill > fill && SkyblockerConfigManager.get().uiAndVisuals.bars.intelligenceDisplay == UIAndVisualsConfig.IntelligenceDisplay.IN_FRONT) {
					HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(getColors()[1].getRGB()));
					HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
				} else {
					HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
					HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(getColors()[1].getRGB()));
				}
			} else {
				HudHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
			}
		}

		@Override
		public void updateValues(float fill, float overflowFill, Object text, @Nullable Object max, @Nullable Object overflow) {
			super.updateValues(fill, overflowFill, StatusBarTracker.isManaEstimated() ? "~" + text : text, max, overflow);
		}
	}

	public static class ExperienceStatusBar extends StatusBar {
		private static final Identifier CLOCK_ICON = SkyblockerMod.id("bars/icons/rift_time");
		public ExperienceStatusBar(StatusBarType type) {
			super(type);
		}

		@Override
		protected Identifier getIcon() {
			return Utils.isInTheRift() ? CLOCK_ICON : super.getIcon();
		}

		@Override
		public void updateValues(float fill, float overflowFill, Object text, @Nullable Object max, @Nullable Object overflow) {
			if (Utils.isInTheRift() && text instanceof Integer time) {
				text = time < 60 ? time + "s" : String.format("%dm%02ds", time / 60, time % 60);
			}
			super.updateValues(fill, overflowFill, text, max, overflow);
		}
	}
}
