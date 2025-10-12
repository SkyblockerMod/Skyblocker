package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.PopupScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

class TopBarWidget extends ContainerWidget {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private static final int HEIGHT = 15;
	private final CustomDropdownWidget<Location> locationDropdown;
	private final CustomDropdownWidget<WidgetManager.ScreenLayer> screenLayerDropdown;
	private final Layout layout;
	private final List<ClickableWidget> widgets;

	TopBarWidget(int width, WidgetsConfigScreen parent) {
		super(0, 0, width, HEIGHT, Text.literal("hi"));

		layout = new Layout();

		StyledButtonWidget helpButton = new StyledButtonWidget(60, HEIGHT, Text.literal("Help"), button -> parent.openPopup(screen -> new PopupScreen.Builder(screen, Text.literal("Help"))
				.message(Text.literal("Use right click to add widgets and edit their options.\nYou can delete a widget by pressing the delete key.\n\nWidgets are per island. To have a widget show up everywhere select \"Everywhere\" in the island dropdown (there's only 2 dropdowns you should be able to find it). Widget options apply to every location with a few exceptions."))
				.button(ScreenTexts.OK, PopupScreen::close)
				.build()));
		layout.add(helpButton);

		List<Location> locations = Lists.newArrayList(Location.values());
		// move UNKNOWN to be first
		locations.remove(Location.UNKNOWN);
		locations.addFirst(Location.UNKNOWN);
		locationDropdown = new CustomDropdownWidget<>(width / 2 - 100 - 5, 0, 100, 200, locations, parent::setCurrentLocation, Utils.getLocation());
		locationDropdown.setFormatter(location -> location == Location.UNKNOWN ? Text.literal("Everywhere").formatted(Formatting.YELLOW) : Text.literal(location.toString()));
		screenLayerDropdown = new CustomDropdownWidget<>(width / 2 + 5, 0, 100, 200, List.of(WidgetManager.ScreenLayer.values()), parent::setCurrentScreenLayer, WidgetManager.ScreenLayer.HUD);

		DirectionalLayoutWidget dropdownsLayout = DirectionalLayoutWidget.horizontal().spacing(2);
		dropdownsLayout.add(locationDropdown);
		dropdownsLayout.add(screenLayerDropdown);
		layout.add(dropdownsLayout);

		ToggleButtonWidget snappingToggle = new ToggleButtonWidget(80, HEIGHT, Text.literal("Snapping"), b -> parent.snapping = b);
		snappingToggle.setState(true);
		snappingToggle.setTooltip(Tooltip.of(Text.literal("Automatically snap widgets to other widgets")));
		ToggleButtonWidget autoAnchorToggle = new ToggleButtonWidget(100, HEIGHT, Text.literal("Auto Screen Anchor"), b -> parent.autoAnchor = b);
		autoAnchorToggle.setState(true);
		autoAnchorToggle.setTooltip(Tooltip.of(Text.literal("Automatically change the anchor of the widget based on the position")));

		DirectionalLayoutWidget togglesLayout = DirectionalLayoutWidget.horizontal();
		togglesLayout.add(snappingToggle);
		togglesLayout.add(autoAnchorToggle);
		layout.add(togglesLayout);

		widgets = List.of(helpButton, locationDropdown, screenLayerDropdown, snappingToggle, autoAnchorToggle);
		layout.refreshPositions();
	}

	@Override
	public int getHeight() {
		return Math.max(super.getHeight(), Math.max(locationDropdown.getHeight(), screenLayerDropdown.getHeight()));
	}

	@Override
	public List<? extends Element> children() {
		return widgets;
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		layout.refreshPositions();
	}

	@Override
	protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() - 2, getY() - 2, getWidth() + 4, HEIGHT + 2);
		for (ClickableWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;
		if (this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	protected void appendClickableNarrations(NarrationMessageBuilder builder) {}

	@Override
	protected int getContentsHeightWithPadding() {
		return 0;
	}

	@Override
	protected double getDeltaYPerScroll() {
		return 0;
	}

	static void drawButtonBorder(DrawContext context, int x, int y, int y2) {
		context.drawVerticalLine(x - 1, y, y2, ColorHelper.withAlpha(15, -1));
		context.drawVerticalLine(x, y, y2, ColorHelper.withAlpha(100, 0));
		context.drawVerticalLine(x + 1, y, y2, ColorHelper.withAlpha(15, -1));
	}

	private class Layout extends WrapperWidget {
		private final List<Widget> widgets = new ArrayList<>(4);

		Layout() {
			super(0, 0, 0, 0);
		}

		private void add(Widget widget) {
			widgets.add(widget);
		}

		@Override
		public void forEachElement(Consumer<Widget> consumer) {
			widgets.forEach(consumer);
		}

		@Override
		public int getX() {
			return TopBarWidget.this.getX();
		}

		@Override
		public int getY() {
			return TopBarWidget.this.getY();
		}

		@Override
		public int getWidth() {
			return TopBarWidget.this.getWidth();
		}

		@Override
		public int getHeight() {
			return TopBarWidget.this.getHeight();
		}

		@Override
		public void refreshPositions() {
			super.refreshPositions();
			Widget first = widgets.getFirst();
			first.setPosition(getX(), getY());
			int low = first.getX() + first.getWidth();
			Widget last = widgets.getLast();
			last.setPosition(getX() + getWidth() - last.getWidth(), getY());
			int high = last.getX();
			for (int i = 1; i < widgets.size() - 1; i++) {
				Widget widget = widgets.get(i);
				widget.setY(getY());
				SimplePositioningWidget.setPos(0, getWidth(), widget.getWidth(), widget::setX, 0.5f);
				if (widget.getX() + widget.getWidth() > high) {
					SimplePositioningWidget.setPos(low, high - low, widget.getWidth(), widget::setX, 0.5f);
				}
			}
		}
	}
}
