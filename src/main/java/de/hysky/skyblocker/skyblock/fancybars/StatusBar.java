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
        public static final int ICON_SIZE = 9;
        public static final int BAR_HEIGHT = 14;
        public static final int MIN_BAR_HEIGHT = 9;
        private static final int BAR_BORDER_COLOR = 0xFF1A1A1A;
        private static final int BAR_BACKGROUND_COLOR = 0xFF2D2D2D;

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
        public int barHeight = BAR_HEIGHT;
        public int borderRadius = 0;

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

        // Custom sub-element offsets (pixels, relative to bar origin)
        public int textCustomOffX = 0;
        public int textCustomOffY = 0;
        public int iconCustomOffX = 0;
        public int iconCustomOffY = 0;

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
                if (iconPosition == IconPosition.CUSTOM) renderCustomIcon(context);
                if (enabled) renderText(context);
        }

        protected Identifier getIcon() {
                return icon;
        }

        @SuppressWarnings("incomplete-switch")
        public void renderBar(GuiGraphics context) {
                if (renderWidth <= 0) return;
                int transparency = transparency(-1);
                int iconY = renderY + (barHeight - ICON_SIZE) / 2;
                switch (iconPosition) {
                        case LEFT -> context.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(), renderX, iconY, ICON_SIZE, ICON_SIZE, transparency);
                        case RIGHT -> context.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(), renderX + renderWidth - ICON_SIZE, iconY, ICON_SIZE, ICON_SIZE, transparency);
                        // CUSTOM: rendered separately via renderCustomIcon() so it appears above all bars
                }

                boolean iconTakesSpace = iconPosition == IconPosition.LEFT || iconPosition == IconPosition.RIGHT;
                int barWidth = iconTakesSpace ? renderWidth - ICON_SIZE - 1 : renderWidth;
                int barX = iconPosition == IconPosition.LEFT ? renderX + ICON_SIZE + 1 : renderX;

                int r = Math.min(borderRadius, Math.min(barWidth, barHeight) / 2);
                fillRounded(context, barX, renderY, barWidth, barHeight, r, transparency(BAR_BORDER_COLOR));
                fillRounded(context, barX + 1, renderY + 1, barWidth - 2, barHeight - 2, Math.max(0, r - 1), transparency(BAR_BACKGROUND_COLOR));
                drawBarFill(context, barX, barWidth);
        }

        /** Renders the icon at its custom position. Called AFTER all bars render, so it appears on top. */
        public void renderCustomIcon(GuiGraphics context) {
                if (renderWidth <= 0 || iconPosition != IconPosition.CUSTOM) return;
                context.blitSprite(RenderPipelines.GUI_TEXTURED, getIcon(),
                        renderX + iconCustomOffX, renderY + iconCustomOffY, ICON_SIZE, ICON_SIZE, transparency(-1));
        }

        protected void drawBarFill(GuiGraphics context, int barX, int barWidth) {
                int innerW = barWidth - 2;
                int innerH = barHeight - 2;
                int ir = Math.max(0, Math.min(borderRadius, Math.min(barWidth, barHeight) / 2) - 1);
                int fillPx = (int) (innerW * fill);
                if (fillPx > 0) {
                        fillRoundedClipped(context, barX + 1, getY() + 1, innerW, innerH, ir, fillPx, transparency(colors[0].getRGB()));
                }
                if (hasOverflow() && overflowFill > 0) {
                        int overflowPx = (int) (innerW * Math.min(overflowFill, 1));
                        if (overflowPx > 0) {
                                fillRoundedClipped(context, barX + 1, getY() + 1, innerW, innerH, ir, overflowPx, transparency(colors[1].getRGB()));
                        }
                }
        }

        // ────────────── Rounded fill helpers ──────────────

        /**
         * Draws a filled rounded rectangle. When r==0 falls back to a plain fill.
         * Each row is computed from the circle formula so corners are pixel-perfect.
         */
        protected static void fillRounded(GuiGraphics ctx, int x, int y, int w, int h, int r, int color) {
                if (w <= 0 || h <= 0) return;
                if (r <= 0) { ctx.fill(x, y, x + w, y + h, color); return; }
                int cr = Math.min(r, Math.min(w, h) / 2);
                for (int row = 0; row < h; row++) {
                        double py = row + 0.5;
                        int xOff = 0;
                        if (py < cr) {
                                double cy = cr - py;
                                xOff = (int) (cr - Math.sqrt(Math.max(0.0, (double) cr * cr - cy * cy)) + 0.5);
                        } else if (py > h - cr) {
                                double cy = py - (h - cr);
                                xOff = (int) (cr - Math.sqrt(Math.max(0.0, (double) cr * cr - cy * cy)) + 0.5);
                        }
                        int rx1 = x + xOff, rx2 = x + w - xOff;
                        if (rx1 < rx2) ctx.fill(rx1, y + row, rx2, y + row + 1, color);
                }
        }

        /**
         * Draws a rounded rectangle clipped to fillW pixels wide. The right edge is
         * a straight cut when fillW &lt; w, and rounds naturally when fillW &ge; w.
         * Used so bar fills respect rounded corners without over-drawing.
         */
        protected static void fillRoundedClipped(GuiGraphics ctx, int x, int y, int w, int h, int r, int fillW, int color) {
                if (w <= 0 || h <= 0 || fillW <= 0) return;
                int cr = Math.min(r, Math.min(w, h) / 2);
                for (int row = 0; row < h; row++) {
                        double py = row + 0.5;
                        int xOff = 0;
                        if (py < cr) {
                                double cy = cr - py;
                                xOff = (int) (cr - Math.sqrt(Math.max(0.0, (double) cr * cr - cy * cy)) + 0.5);
                        } else if (py > h - cr) {
                                double cy = py - (h - cr);
                                xOff = (int) (cr - Math.sqrt(Math.max(0.0, (double) cr * cr - cy * cy)) + 0.5);
                        }
                        int rx1 = x + xOff;
                        // Right edge: whichever is smaller — the fill level or the rounded right boundary
                        int rx2 = Math.min(x + fillW, x + w - xOff);
                        if (rx1 < rx2) ctx.fill(rx1, y + row, rx2, y + row + 1, color);
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

                boolean iconTakesSpace = iconPosition == IconPosition.LEFT || iconPosition == IconPosition.RIGHT;
                int barWidth = iconTakesSpace ? renderWidth - ICON_SIZE - 1 : renderWidth;
                int barX = iconPosition == IconPosition.LEFT ? renderX + ICON_SIZE + 2 : renderX;

                String stringValue = this.value.toString();
                // Use white text inside the bar for maximum contrast; fall back to type text color
                int textArgb = textColor != null ? textColor.getRGB() : 0xFFFFFFFF;
                MutableComponent text = Component.literal(stringValue).withStyle(style -> style.withColor(textArgb));

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
                int y;
                switch (textPosition) {
                        case RIGHT -> { x = barX + barWidth - textWidth - 2; y = renderY + (barHeight - 9) / 2 + 1; }
                        case BAR_CENTER -> { x = barX + (barWidth - textWidth) / 2; y = renderY + (barHeight - 9) / 2 + 1; }
                        case CUSTOM -> { x = renderX + textCustomOffX; y = renderY + textCustomOffY; }
                        default -> { x = barX + 2; y = renderY + (barHeight - 9) / 2 + 1; } // LEFT is the default
                }

                int color = transparency(textArgb);
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
                return barHeight;
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

        /**
         * Returns the screen rectangle of the text for hit-testing and outline drawing in the config screen.
         * Only meaningful when textPosition == CUSTOM. Includes generous padding for easy clicking.
         */
        public ScreenRectangle getTextHitArea(Font font) {
                int tx = renderX + textCustomOffX;
                int ty = renderY + textCustomOffY;
                String sample = value.toString() + (showMax && max != null ? "/" + max : "");
                int tw = Math.max(24, font.width(sample));
                int th = 9;
                int pad = 8;
                return new ScreenRectangle(tx - pad, ty - pad, tw + pad * 2, th + pad * 2);
        }

        /**
         * Returns the screen rectangle of the icon for hit-testing and outline drawing in the config screen.
         * Only meaningful when iconPosition == CUSTOM. Includes generous padding for easy clicking.
         */
        public ScreenRectangle getIconHitArea() {
                int ix = renderX + iconCustomOffX;
                int iy = renderY + iconCustomOffY;
                int pad = 8;
                return new ScreenRectangle(ix - pad, iy - pad, ICON_SIZE + pad * 2, ICON_SIZE + pad * 2);
        }

        public enum IconPosition implements StringRepresentable {
                LEFT,
                RIGHT,
                OFF,
                CUSTOM;

                @Override
                public String getSerializedName() {
                        return name();
                }

                @Override
                public String toString() {
                        if (this == CUSTOM) return I18n.get("skyblocker.bars.config.commonPosition.CUSTOM");
                        return I18n.get("skyblocker.bars.config.commonPosition." + name());
                }
        }

        public enum TextPosition implements StringRepresentable {
                LEFT,
                CENTER,
                BAR_CENTER,
                RIGHT,
                OFF,
                CUSTOM;

                @Override
                public String getSerializedName() {
                        return name();
                }

                @Override
                public String toString() {
                        return switch (this) {
                                case BAR_CENTER -> I18n.get("skyblocker.bars.config.textPosition.BAR_CENTER");
                                case LEFT -> I18n.get("skyblocker.bars.config.textPosition.LEFT");
                                case RIGHT -> I18n.get("skyblocker.bars.config.textPosition.RIGHT");
                                case CUSTOM -> I18n.get("skyblocker.bars.config.textPosition.CUSTOM");
                                case CENTER -> I18n.get("skyblocker.bars.config.textPosition.BAR_CENTER"); // legacy
                                default -> I18n.get("skyblocker.bars.config.commonPosition." + name());
                        };
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
                if (object.has("text_position")) {
                        TextPosition tp = TextPosition.valueOf(object.get("text_position").getAsString().trim());
                        this.textPosition = tp == TextPosition.CENTER ? TextPosition.BAR_CENTER : tp;
                }
                if (object.has("show_max")) this.showMax = object.get("show_max").getAsBoolean();
                if (object.has("show_overflow")) this.showOverflow = object.get("show_overflow").getAsBoolean();
                if (object.has("bar_height")) this.barHeight = Math.max(MIN_BAR_HEIGHT, object.get("bar_height").getAsInt());
                if (object.has("border_radius")) this.borderRadius = Math.max(0, object.get("border_radius").getAsInt());
                if (object.has("text_custom_off_x")) this.textCustomOffX = object.get("text_custom_off_x").getAsInt();
                if (object.has("text_custom_off_y")) this.textCustomOffY = object.get("text_custom_off_y").getAsInt();
                if (object.has("icon_custom_off_x")) this.iconCustomOffX = object.get("icon_custom_off_x").getAsInt();
                if (object.has("icon_custom_off_y")) this.iconCustomOffY = object.get("icon_custom_off_y").getAsInt();
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
                object.addProperty("bar_height", barHeight);
                object.addProperty("border_radius", borderRadius);
                if (iconPosition == IconPosition.CUSTOM) {
                        object.addProperty("icon_custom_off_x", iconCustomOffX);
                        object.addProperty("icon_custom_off_y", iconCustomOffY);
                }
                if (textPosition == TextPosition.CUSTOM) {
                        object.addProperty("text_custom_off_x", textCustomOffX);
                        object.addProperty("text_custom_off_y", textCustomOffY);
                }
                return object;
        }

        public static class ManaStatusBar extends StatusBar {

                public ManaStatusBar(StatusBarType type) {
                        super(type);
                }

                @Override
                protected void drawBarFill(GuiGraphics context, int barX, int barWith) {
                        int innerW = barWith - 2;
                        int innerH = barHeight - 2;
                        int ir = Math.max(0, Math.min(borderRadius, Math.min(barWith, barHeight) / 2) - 1);
                        int bx = barX + 1;
                        int by = getY() + 1;
                        if (hasOverflow() && overflowFill > 0) {
                                if (overflowFill > fill && SkyblockerConfigManager.get().uiAndVisuals.bars.intelligenceDisplay == UIAndVisualsConfig.IntelligenceDisplay.IN_FRONT) {
                                        int ovPx = (int) (innerW * Math.min(overflowFill, 1));
                                        if (ovPx > 0) fillRoundedClipped(context, bx, by, innerW, innerH, ir, ovPx, transparency(getColors()[1].getRGB()));
                                        int fillPx = (int) (innerW * fill);
                                        if (fillPx > 0) fillRoundedClipped(context, bx, by, innerW, innerH, ir, fillPx, transparency(getColors()[0].getRGB()));
                                } else {
                                        int fillPx = (int) (innerW * fill);
                                        if (fillPx > 0) fillRoundedClipped(context, bx, by, innerW, innerH, ir, fillPx, transparency(getColors()[0].getRGB()));
                                        int ovPx = (int) (innerW * Math.min(overflowFill, 1));
                                        if (ovPx > 0) fillRoundedClipped(context, bx, by, innerW, innerH, ir, ovPx, transparency(getColors()[1].getRGB()));
                                }
                        } else {
                                int fillPx = (int) (innerW * fill);
                                if (fillPx > 0) fillRoundedClipped(context, bx, by, innerW, innerH, ir, fillPx, transparency(getColors()[0].getRGB()));
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
        }
}
