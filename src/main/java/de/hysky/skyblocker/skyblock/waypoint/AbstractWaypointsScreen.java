package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.cursor.Cursor;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AbstractWaypointsScreen<T extends Screen> extends Screen {
    protected final T parent;
    protected final Multimap<Location, WaypointGroup> waypoints;
    protected Location island;
    protected WaypointsListWidget waypointsListWidget;
    protected DropdownWidget<Location> islandWidget;

	protected final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
	private final PopupContainer popupContainer = new PopupContainer();

    public AbstractWaypointsScreen(Text title, T parent) {
        this(title, parent, MultimapBuilder.enumKeys(Location.class).arrayListValues().build());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<Location, WaypointGroup> waypoints) {
        this(title, parent, waypoints, Utils.getLocation());
    }

    public AbstractWaypointsScreen(Text title, T parent, Multimap<Location, WaypointGroup> waypoints, Location island) {
        super(title);
        this.parent = parent;
        this.waypoints = waypoints;
        this.island = island;
		this.layout.setHeaderHeight(32);
    }

    @Override
    protected void init() {
        super.init();
		layout.addHeader(new TextWidget(title, textRenderer));
		addSelectableChild(popupContainer);
		waypointsListWidget = addDrawableChild(new WaypointsListWidget(client, this, width, height - 120, 32, 24));
    }

    /**
     * This should be called at the end of the implementation's init to ensure that these elements render last.
     */
    protected final void lateInit() {
		layout.forEachChild(this::addDrawableChild);
		// Not using layout due to dynamic height.
    	islandWidget = addDrawableChild(new DropdownWidget<>(client, width - 160, 8, 150, height - 8, Arrays.asList(Location.values()), this::islandChanged, island, (isOpen) -> {}));
		addDrawable(popupContainer);
		refreshWidgetPositions();
    }

	protected void setPopup(Widget w, int x, int y) {
		if (w == null) popupContainer.visible = false;
		else {
			popupContainer.visible = true;
			popupContainer.setWidget(w);
			popupContainer.setPosition(
					Math.clamp(x, 0, width - popupContainer.getWidth()),
					Math.clamp(y, 0, height - popupContainer.getHeight())
			);
		}
	}

	@Override
	protected void refreshWidgetPositions() {
		layout.refreshPositions();
		waypointsListWidget.position(width, layout);
		waypointsListWidget.updateEntries();
		islandWidget.setX(width - islandWidget.getWidth() - 10);
		SimplePositioningWidget.setPos(0, layout.getHeaderHeight(), DropdownWidget.HEADER_HEIGHT, islandWidget::setY, 0.5f);
		islandWidget.setMaxHeight(Math.max(height - islandWidget.getY() - 8, 20));
	}

	@Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (islandWidget.mouseClicked(click, doubled)) {
            return true;
        }
        boolean mouseClicked = super.mouseClicked(click, doubled);
        updateButtons();
        return mouseClicked;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (islandWidget.isMouseOver(mouseX, mouseY) && islandWidget.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    protected void islandChanged(Location location) {
        island = location;
        waypointsListWidget.setIsland(island);
    }

    /**
     * Gets whether the waypoint is enabled in the current screen.
     * Override for custom behavior such as using the checkbox for whether it should be included in the exported waypoints.
     *
     * @return whether the waypoint is enabled in the current screen
     */
    protected boolean isEnabled(NamedWaypoint waypoint) {
        return waypoint.isEnabled();
    }

    /**
     * Called when the enabled state of a waypoint checkbox changes.
     * Override for custom behavior such as updating whether the waypoint should be included in the exported waypoints.
     */
    protected void enabledChanged(NamedWaypoint waypoint, boolean enabled) {
        waypoint.setEnabled(enabled);
    }

    protected void updateButtons() {
        waypointsListWidget.updateButtons();
    }

	private static class PopupContainer extends ContainerWidget {
		private static final int BORDER_SIZE = 4;

		private Widget widget = EmptyWidget.ofWidth(0);
		private final List<ClickableWidget> children = new ArrayList<>(4);
		private final PressableWidget closeButton;

		PopupContainer() {
			super(0, 0, 0, 0, Text.empty());
			this.closeButton = new TexturedButtonWidget(14, 14, new ButtonTextures(
					Identifier.ofVanilla("widget/cross_button"), Identifier.ofVanilla("widget/cross_button_highlighted")),
					b -> visible = false,
					Text.empty()
					);
		}

		@Override
		public int getWidth() {
			return widget.getWidth() + BORDER_SIZE * 2;
		}

		@Override
		public int getHeight() {
			return widget.getHeight() + BORDER_SIZE * 2 + closeButton.getHeight() + 2;
		}

		@Override
		public void setX(int x) {
			super.setX(x);
			closeButton.setX(getRight() - BORDER_SIZE - closeButton.getWidth());
			widget.setX(getX() + BORDER_SIZE);
		}

		@Override
		public void setY(int y) {
			super.setY(y);
			closeButton.setY(getY() + BORDER_SIZE);
			widget.setY(closeButton.getBottom() + 2);
		}

		@Override
		public List<? extends Element> children() {
			return visible ? children : List.of();
		}

		public void setWidget(Widget widget) {
			this.widget = widget;
			children.clear();
			children.add(closeButton);
			widget.forEachChild(children::add);
			closeButton.setX(getRight() - BORDER_SIZE - closeButton.getWidth());
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return 0;
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 0;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			// Set the cursor to default to prevent widgets from below taking over the shape when they cannot be interacted with
			if (this.isHovered()) {
				context.setCursor(Cursor.DEFAULT);
			}

			context.fill(getX(), getY(), getRight(), getBottom(), ColorHelper.withAlpha(0.6f, 0));
			HudHelper.drawBorder(context, getX(), getY(), getWidth(), getHeight(), Colors.WHITE);
			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
			if (!isFocused() &&
					(mouseX <= getX() - 50 || mouseX >= getRight() + 50 || mouseY <= getY() - 50 || mouseY >= getBottom() + 50)) visible = false;
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}
	}
}
