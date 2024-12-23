package de.hysky.skyblocker.skyblock.fancybars;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.render.RenderLayer;
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


   /* public static final Codec<StatusBar> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.INT.fieldOf("size").forGetter(bar -> bar.size),
                    Codec.INT.fieldOf("x").forGetter(bar -> bar.gridX),
                    Codec.INT.fieldOf("y").forGetter(bar -> bar.gridY),
                    Codec.STRING.listOf().fieldOf("colors").xmap(
                                    strings -> strings.stream().map(s -> Integer.parseInt(s, 16)).map(Color::new).toArray(Color[]::new),
                                    colors -> Arrays.stream(colors).map(color -> Integer.toHexString(color.getRGB())).toList())
                            .forGetter(StatusBar::getColors),
                    Codec.STRING.optionalFieldOf("text_color").xmap(
                                    s -> {
                                        if (s.isPresent()) {
                                            return Optional.of(new Color(Integer.parseInt(s.get(), 16)));
                                        } else return Optional.empty();
                                    },
                                    o -> o.map(object -> Integer.toHexString(((Color) object).getRGB())))
                            .forGetter(bar -> {
                                if (bar.getTextColor() != null) {
                                    return Optional.of(bar.getTextColor());
                                } else return Optional.empty();
                            }),
                    Codec.BOOL.optionalFieldOf("show_text", true).forGetter(StatusBar::showText),
                    Codec.STRING.fieldOf("icon_position").xmap(
                            IconPosition::valueOf,
                            Enum::toString
                    ).forGetter(bar -> bar.iconPosition)
            )

            .apply(instance, ));*/

	private final Identifier icon;

	public Color[] getColors() {
		return colors;
	}

	public boolean hasOverflow() {
		return hasOverflow;
	}

	public @Nullable Color getTextColor() {
		return textColor;
	}

	private Color[] colors;
	private final boolean hasOverflow;

	public void setTextColor(@Nullable Color textColor) {
		this.textColor = textColor;
	}

	private @Nullable Color textColor;

	public Text getName() {
		return name;
	}

	private final Text name;

	private @Nullable OnClick onClick = null;
	public int gridX = 0;
	public int gridY = 0;
	public @Nullable BarPositioner.BarAnchor anchor = null;

	public int size = 1;
	private int width = 0;

	public float fill = 0;
	public float overflowFill = 0;
	public boolean inMouse = false;

	private Object value = "???";

	private int x = 0;
	private int y = 0;

	private IconPosition iconPosition = IconPosition.LEFT;
	private TextPosition textPosition = TextPosition.BAR_CENTER;

	public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor, Text name) {
		this.icon = icon;
		this.colors = colors;
		this.hasOverflow = hasOverflow;
		this.textColor = textColor;
		this.name = name;
	}

	public StatusBar(Identifier icon, Color[] colors, boolean hasOverflow, @Nullable Color textColor) {
		this(icon, colors, hasOverflow, textColor, Text.empty());
	}

	private int transparency(int color) {
		if (inMouse) return (color & 0x00FFFFFF) | 0x44_000000;
		return color;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		if (width <= 0) return;
		int transparency = transparency(-1);
		switch (iconPosition) {
			case LEFT -> context.drawGuiTexture(RenderLayer::getGuiTextured, icon, x, y, 9, 9, transparency);
			case RIGHT -> context.drawGuiTexture(RenderLayer::getGuiTextured, icon, x + width - 9, y, 9, transparency);
		}

		int barWith = iconPosition.equals(IconPosition.OFF) ? width : width - 10;
		int barX = iconPosition.equals(IconPosition.LEFT) ? x + 10 : x;
		context.drawGuiTexture(RenderLayer::getGuiTextured, BAR_BACK, barX, y + 1, barWith, 7, transparency);
		RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * fill), 5, transparency(colors[0].getRGB()));


		if (hasOverflow && overflowFill > 0) {
			RenderHelper.renderNineSliceColored(context, BAR_FILL, barX + 1, y + 2, (int) ((barWith - 2) * Math.min(overflowFill, 1)), 5, transparency(colors[1].getRGB()));
		}
		//context.drawText(MinecraftClient.getInstance().textRenderer, gridX + " " + gridY + " s:" + size , x, y-9, Colors.WHITE, true);
		if (showText()) {
			context.getMatrices().push();
			context.getMatrices().translate(0, 0, 100);
			renderText(context);
			context.getMatrices().pop();
		}
	}

	public void updateValues(float fill, float overflowFill, Object text) {
		this.value = text;
		this.fill = fill;
		this.overflowFill = overflowFill;
	}

	public void renderText(DrawContext context) {
		if (!showText()) return;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		int barWith = iconPosition.equals(IconPosition.OFF) ? width : width - 10;
		int barX = iconPosition.equals(IconPosition.LEFT) ? x + 11 : x;
		String text = this.value.toString();
		int textWidth = textRenderer.getWidth(text);
		int x;
		switch (textPosition) {
			case RIGHT -> x = barX + barWith - textWidth;
			case CENTER -> x = this.x + (width - textWidth) / 2;
			case BAR_CENTER -> x = barX + (barWith - textWidth) / 2;
			case null, default -> x = barX; // Put on the left by default because I said so.
		}
		int y = this.y - 3;

		context.draw(vertex -> textRenderer.drawWithOutline(
				Text.of(text).asOrderedText(),
				x,
				y,
				transparency((textColor == null ? colors[0] : textColor).getRGB()),
				transparency(0),
				context.getMatrices().peek().getPositionMatrix(),
				vertex,
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
				.append("name", name)
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
			if (colors1.size() < 2 && hasOverflow) {
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
		return object;
	}
}
