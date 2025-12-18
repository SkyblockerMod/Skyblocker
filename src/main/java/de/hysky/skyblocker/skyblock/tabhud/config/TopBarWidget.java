package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.common.collect.Lists;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.AbstractLayout;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

class TopBarWidget extends AbstractContainerWidget {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private static final int HEIGHT = 15;
	private final CustomDropdownWidget<Location> locationDropdown;
	private final CustomDropdownWidget<WidgetManager.ScreenLayer> screenLayerDropdown;
	private final Layout layout;
	private final List<AbstractWidget> widgets;

	// TODO translatable :)
	TopBarWidget(int width, WidgetsConfigScreen parent) {
		super(0, 0, width, HEIGHT, Component.literal("hi"));

		layout = new Layout();

		LinearLayout leftButtons = LinearLayout.horizontal();
		StyledButtonWidget optionsButton = new StyledButtonWidget(60, HEIGHT, Component.literal("Options"), button -> parent.openPopup(GlobalOptionsScreen::new));
		optionsButton.setTooltip(Tooltip.create(Component.literal("Global options that affect everywhere.")));
		StyledButtonWidget helpButton = new StyledButtonWidget(60, HEIGHT, Component.literal("(?) Help"), button -> parent.openPopup(screen -> new PopupScreen.Builder(screen, Component.literal("Help"))
				.setMessage(Component.literal("""
						Use right click to add widgets and edit their options.
						You can delete a widget by pressing the delete key.

						Widgets are per island. To have a widget show up everywhere select "Everywhere" in the island dropdown (there's only 2 dropdowns you should be able to find it). Widget options apply to every location with a few exceptions.

						You can hold SHIFT to snap to other widgets.
						"""))
				.addButton(CommonComponents.GUI_OK, PopupScreen::onClose)
				.build()));
		leftButtons.addChild(optionsButton);
		leftButtons.addChild(helpButton);
		layout.add(leftButtons);

		List<Location> locations = Lists.newArrayList(Location.values());
		// move UNKNOWN to be first
		locations.remove(Location.UNKNOWN);
		locations.addFirst(Location.UNKNOWN);
		locationDropdown = new CustomDropdownWidget<>(width / 2 - 100 - 5, 0, 100, 200, locations, parent::setCurrentLocation, Utils.getLocation());
		locationDropdown.setFormatter(location -> location == Location.UNKNOWN ? Component.literal("Everywhere").withStyle(ChatFormatting.YELLOW) : Component.literal(location.toString()));
		screenLayerDropdown = new CustomDropdownWidget<>(width / 2 + 5, 0, 100, 200, List.of(WidgetManager.ScreenLayer.values()), parent::setCurrentScreenLayer, WidgetManager.ScreenLayer.HUD);

		LinearLayout dropdownsLayout = LinearLayout.horizontal().spacing(2);
		dropdownsLayout.addChild(locationDropdown);
		dropdownsLayout.addChild(screenLayerDropdown);
		layout.add(dropdownsLayout);

		/*ToggleButtonWidget snappingToggle = new ToggleButtonWidget(80, HEIGHT, Text.literal("Snapping"), b -> parent.snapping = b);
		snappingToggle.setState(true);
		snappingToggle.setTooltip(Tooltip.of(Text.literal("Automatically snap widgets to other widgets")));*/
		ToggleButtonWidget autoAnchorToggle = new ToggleButtonWidget(100, HEIGHT, Component.literal("Auto Screen Anchor"), b -> parent.autoAnchor = b);
		autoAnchorToggle.setState(true);
		autoAnchorToggle.setTooltip(Tooltip.create(Component.literal("Automatically change the anchor of the widget based on the position")));

		LinearLayout rightButtons = LinearLayout.horizontal();
		//rightButtons.add(snappingToggle);
		rightButtons.addChild(autoAnchorToggle);
		layout.add(rightButtons);

		widgets = List.of(optionsButton, helpButton, locationDropdown, screenLayerDropdown, autoAnchorToggle);
		layout.arrangeElements();
	}

	@Override
	public int getHeight() {
		return Math.max(super.getHeight(), Math.max(locationDropdown.getHeight(), screenLayerDropdown.getHeight()));
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return widgets;
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		layout.arrangeElements();
	}

	@Override
	protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() - 2, getY() - 2, getWidth() + 4, HEIGHT + 2);
		for (AbstractWidget widget : widgets) {
			widget.render(context, mouseX, mouseY, deltaTicks);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (!visible) return false;
		if (this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput builder) {}

	@Override
	protected int contentHeight() {
		return 0;
	}

	@Override
	protected double scrollRate() {
		return 0;
	}

	static void drawButtonBorder(GuiGraphics context, int x, int y, int y2) {
		context.vLine(x - 1, y, y2, ARGB.color(15, -1));
		context.vLine(x, y, y2, ARGB.color(100, 0));
		context.vLine(x + 1, y, y2, ARGB.color(15, -1));
	}

	private class Layout extends AbstractLayout {
		private final List<LayoutElement> widgets = new ArrayList<>(4);

		Layout() {
			super(0, 0, 0, 0);
		}

		private void add(LayoutElement widget) {
			widgets.add(widget);
		}

		@Override
		public void visitChildren(Consumer<LayoutElement> consumer) {
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
		public void arrangeElements() {
			super.arrangeElements();
			LayoutElement first = widgets.getFirst();
			first.setPosition(getX(), getY());
			int low = first.getX() + first.getWidth();
			LayoutElement last = widgets.getLast();
			last.setPosition(getX() + getWidth() - last.getWidth(), getY());
			int high = last.getX();
			for (int i = 1; i < widgets.size() - 1; i++) {
				LayoutElement widget = widgets.get(i);
				widget.setY(getY());
				FrameLayout.alignInDimension(0, getWidth(), widget.getWidth(), widget::setX, 0.5f);
				if (widget.getX() + widget.getWidth() > high) {
					FrameLayout.alignInDimension(low, high - low, widget.getWidth(), widget::setX, 0.5f);
				}
			}
		}
	}
}
