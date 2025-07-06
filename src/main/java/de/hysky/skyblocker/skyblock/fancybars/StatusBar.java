package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.StatusBarTracker;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.function.Consumer;

public class StatusBar implements Widget, Drawable, Element, Selectable {

	private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
	private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

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

	public Text getName() {
		return type.getName();
	}

	private @Nullable OnClick onClick = null;
	public int gridX = 0;
	public int gridY = 0;
	public @Nullable BarPositioner.BarAnchor anchor = null;

	public int size = 1;
	private int width = 0;

	public float fill = 0;
	public float overflowFill = 0;
	public boolean inMouse = false;
	/**
	 * Used to hide the bar dynamically, like the oxygen bar
	 */
	public boolean visible = true;

	private Object value = "???";
	private @Nullable Object max = "???";
	private @Nullable Object overflow = "???";

	private int x = 0;
	private int y = 0;

	private IconPosition iconPosition = IconPosition.LEFT;
	private TextPosition textPosition = TextPosition.BAR_CENTER;

	public boolean showMax = false;
	public boolean showOverflow = false;

	public StatusBar(StatusBarType type) {
		this.icon = Identifier.of(SkyblockerMod.NAMESPACE, "bars/icons/" + type.asString());
		this.colors = type.getColors();
		this.textColor = type.getTextColor();
		this.type = type;
	}

	protected int transparency(int color) {
		if (inMouse) return (color & 0x00FFFFFF) | 0x44_000000;
		return color;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (width <= 0) return;
		int transparency = transparency(-1);
		switch (iconPosition) {
			case LEFT -> context.drawGuiTexture(RenderLayer::getGuiTextured, icon, x, y, 9, 9, transparency);
			case RIGHT -> context.drawGuiTexture(RenderLayer::getGuiTextured, icon, x + width - 9, y, 9, 9, transparency);
		}

		int barWith = iconPosition.equals(IconPosition.OFF) ? width : width - 10;
		int barX = iconPosition.equals(IconPosition.LEFT) ? x + 10 : x;
		context.drawGuiTexture(RenderLayer::getGuiTextured, BAR_BACK, barX, y + 1, barWith, 7, transparency);
		drawBarFill(context, barX, barWith);
		//context.drawText(MinecraftClient.getInstance().textRenderer, gridX + " " + gridY + " s:" + size , x, y-9, Colors.WHITE, true);
		if (showText()) {
			context.getMatrices().push();
			context.getMatrices().translate(0, 0, 100);
			renderText(context);
			context.getMatrices().pop();
		}
	}

	protected void drawBarFill(DrawContext context, int barX, int barWith) {
		RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * fill), 5, transparency(colors[0].getRGB()));


		if (hasOverflow() && overflowFill > 0) {
			RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(colors[1].getRGB()));
		}
	}

	public void updateValues(float fill, float overflowFill, Object text, @Nullable Object max, @Nullable Object overflow) {
		this.value = text;
		this.fill = fill;
		this.overflowFill = overflowFill;
		this.max = max;
		this.overflow = overflow;
	}

	public void updateWithResource(StatusBarTracker.Resource resource) {
		updateValues(resource.value() / (float) resource.max(), resource.overflow() / (float) resource.max(), resource.value(), resource.max(), resource.overflow() > 0 ? resource.overflow() : null);
	}

	public void renderText(DrawContext context) {
		if (!showText()) return;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int barWith = iconPosition.equals(IconPosition.OFF) ? width : width - 10;
		int barX = iconPosition.equals(IconPosition.LEFT) ? x + 11 : x;
		String stringValue = this.value.toString();
		MutableText text = Text.literal(stringValue).styled(style -> style.withColor((textColor == null ? colors[0] : textColor).getRGB()));

		if (hasMax() && showMax && max != null) {
			text.append("/").append(max.toString());
		}
		if (hasOverflow() && showOverflow && overflow != null) {
			MutableText literal = Text.literal(" + ").styled(style -> style.withColor(colors[1].getRGB()));
			literal.append(overflow.toString());
			text.append(literal);
		}

		int textWidth = textRenderer.getWidth(text);
		int x;
		switch (textPosition) {
			case RIGHT -> x = barX + barWith - textWidth;
			case CENTER -> x = this.x + (width - textWidth) / 2;
			case BAR_CENTER -> x = barX + (barWith - textWidth) / 2;
			case null, default -> x = barX; // Put on the left by default because I said so.
		}
		int y = this.y - 3;

		context.draw(consumers -> textRenderer.drawWithOutline(
				text.asOrderedText(),
				x,
				y,
				transparency(-1),
				transparency(0),
				context.getMatrices().peek().getPositionMatrix(),
				consumers,
				15728880
				));
	}

	public void renderCursor(DrawContext context, int mouseX, int mouseY, float delta) {
		int temp_x = x;
		int temp_y = y;
		int temp_width = width;
		boolean temp_ghost = inMouse;

		x = mouseX;
		y = mouseY;
		width = 100;
		inMouse = false;

		render(context, mouseX, mouseY, delta);

		x = temp_x;
		y = temp_y;
		width = temp_width;
		inMouse = temp_ghost;
	}

	// GUI shenanigans

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public int getHeight() {
		return 9;
	}

	@Override
	public ScreenRect getNavigationFocus() {
		return Widget.super.getNavigationFocus();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return mouseX >= x && mouseX <= x + getWidth() && mouseY >= y && mouseY <= y + getHeight();
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
	}

	@Override
	public void setFocused(boolean focused) {
	}

	@Override
	public boolean isFocused() {
		return false;
	}

	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (!isMouseOver(mouseX, mouseY)) return false;
		if (onClick != null) {
			onClick.onClick(this, button, (int) mouseX, (int) mouseY);
		}
		return true;
	}

	public void setOnClick(@Nullable OnClick onClick) {
		this.onClick = onClick;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("name", getName())
				.append("gridX", gridX)
				.append("gridY", gridY)
				.append("size", size)
				.append("x", x)
				.append("y", y)
				.append("width", width)
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

	public enum IconPosition implements StringIdentifiable {
		LEFT,
		RIGHT,
		OFF;

		@Override
		public String asString() {
			return name();
		}

		@Override
		public String toString() {
			return I18n.translate("skyblocker.bars.config.commonPosition." + name());
		}
	}

	public enum TextPosition implements StringIdentifiable {
		LEFT,
		CENTER,
		BAR_CENTER,
		RIGHT,
		OFF;

		@Override
		public String asString() {
			return name();
		}

		@Override
		public String toString() {
			if (this == CENTER || this == BAR_CENTER) return I18n.translate("skyblocker.bars.config.textPosition." + name());
			return I18n.translate("skyblocker.bars.config.commonPosition." + name());
		}
	}

	@FunctionalInterface
	public interface OnClick {

		void onClick(StatusBar statusBar, int button, int mouseX, int mouseY);
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
		this.size = object.get("size").getAsInt();
		this.gridX = object.get("x").getAsInt();
		this.gridY = object.get("y").getAsInt();
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
		object.addProperty("size", size);
		if (anchor != null) {
			object.addProperty("anchor", anchor.toString());
		} else object.addProperty("anchor", "null");
		object.addProperty("x", gridX);
		object.addProperty("y", gridY);
		object.addProperty("icon_position", iconPosition.asString());
		object.addProperty("text_position", textPosition.asString());
		object.addProperty("show_max", showMax);
		object.addProperty("show_overflow", showOverflow);
		return object;
	}

	public static class ManaStatusBar extends StatusBar {

		public ManaStatusBar(StatusBarType type) {
			super(type);
		}

		@Override
		protected void drawBarFill(DrawContext context, int barX, int barWith) {
			if (hasOverflow() && overflowFill > 0) {
				if (overflowFill > fill && SkyblockerConfigManager.get().uiAndVisuals.bars.intelligenceDisplay == UIAndVisualsConfig.IntelligenceDisplay.IN_FRONT) {
					RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(getColors()[1].getRGB()));
					RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
				} else {
					RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
					RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(getColors()[1].getRGB()));
				}
			} else {
				RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, getY() + 2, (int) ((barWith - 2) * fill), 5, transparency(getColors()[0].getRGB()));
			}
		}
	}
}
