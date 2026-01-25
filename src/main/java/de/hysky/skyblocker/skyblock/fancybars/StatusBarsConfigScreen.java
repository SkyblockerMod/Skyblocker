package de.hysky.skyblocker.skyblock.fancybars;

import com.mojang.blaze3d.platform.cursor.CursorTypes;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanMutablePair;
import it.unimi.dsi.fastutil.objects.ObjectBooleanPair;
import it.unimi.dsi.fastutil.objects.ObjectObjectMutablePair;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.Window;
import de.hysky.skyblocker.skyblock.fancybars.BarPositioner.BarLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class StatusBarsConfigScreen extends Screen {
	private static final Identifier HOTBAR_TEXTURE = Identifier.withDefaultNamespace("hud/hotbar");
	private static final int HOTBAR_WIDTH = 182;
	private static final float RESIZE_THRESHOLD = 0.75f;
	private static final int BAR_MINIMUM_WIDTH = 30;
	// prioritize left and right cuz they are much smaller than up and down
	private static final ScreenDirection[] DIRECTION_CHECK_ORDER = new ScreenDirection[]{ScreenDirection.LEFT, ScreenDirection.RIGHT, ScreenDirection.UP, ScreenDirection.DOWN};

	private final Map<ScreenRectangle, Pair<StatusBar, BarLocation>> rectToBar = new HashMap<>();
	/**
	 * Contains the hovered bar and a boolean that is true if hovering the right side or false otherwise.
	 */
	private final ObjectBooleanPair<@Nullable StatusBar> resizeHover = new ObjectBooleanMutablePair<>(null, false);
	private final Pair<@Nullable StatusBar, @Nullable StatusBar> resizedBars = ObjectObjectMutablePair.of(null, null);

	private @Nullable StatusBar cursorBar = null;
	private ScreenPosition cursorOffset = new ScreenPosition(0, 0);
	private BarLocation currentInsertLocation = new BarLocation(null, 0, 0);

	private boolean resizing = false;
	private EditBarWidget editBarWidget;

	public StatusBarsConfigScreen() {
		super(Component.nullToEmpty("Status Bars Config"));
	}


	@Override
	public void render(GuiGraphics context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		context.blitSprite(RenderPipelines.GUI_TEXTURED, HOTBAR_TEXTURE, width / 2 - HOTBAR_WIDTH / 2, height - 22, HOTBAR_WIDTH, 22);
		editBarWidget.render(context, mouseX, mouseY, delta);

		Window window = minecraft.getWindow();
		int scaleFactor = window.calculateScale(0, minecraft.isEnforceUnicode()) - window.getGuiScale() + 3;
		if ((scaleFactor & 2) == 0) scaleFactor++;

		ScreenRectangle mouseRect = new ScreenRectangle(new ScreenPosition(mouseX - scaleFactor / 2, mouseY - scaleFactor / 2), scaleFactor, scaleFactor);

		if (cursorBar != null) {
			cursorBar.renderCursor(context, mouseX + cursorOffset.x(), mouseY + cursorOffset.y(), delta);
			boolean inserted = false;
			boolean updatePositions = false;
			rectLoop:
			for (ScreenRectangle screenRect : rectToBar.keySet()) {
				for (ScreenDirection direction : DIRECTION_CHECK_ORDER) {
					boolean overlaps = screenRect.getBorder(direction).step(direction).overlaps(mouseRect);

					if (overlaps) {
						Pair<StatusBar, BarLocation> barPair = rectToBar.get(screenRect);
						BarLocation barSnap = barPair.right();
						if (barSnap.barAnchor() == null) break;
						if (direction.getAxis().equals(ScreenAxis.VERTICAL)) {
							int neighborInsertY = getNeighborInsertY(barSnap, !direction.isPositive());
							inserted = true;
							if (!currentInsertLocation.equals(barSnap.barAnchor(), barSnap.x(), neighborInsertY)) {
								if (cursorBar.anchor != null)
									FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
								FancyStatusBars.barPositioner.addRow(barSnap.barAnchor(), neighborInsertY);
								FancyStatusBars.barPositioner.addBar(barSnap.barAnchor(), neighborInsertY, cursorBar);
								currentInsertLocation = BarLocation.of(cursorBar);
								updatePositions = true;
							}
						} else {
							int neighborInsertX = getNeighborInsertX(barSnap, direction.isPositive());
							inserted = true;
							if (!currentInsertLocation.equals(barSnap.barAnchor(), neighborInsertX, barSnap.y())) {
								if (cursorBar.anchor != null)
									FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
								FancyStatusBars.barPositioner.addBar(barSnap.barAnchor(), barSnap.y(), neighborInsertX, cursorBar);
								currentInsertLocation = BarLocation.of(cursorBar);
								updatePositions = true;
							}
						}
						break rectLoop;
					}
				}
			}
			if (updatePositions) {
				FancyStatusBars.updatePositions(true);
				return;
			}
			// check for hovering empty anchors
			for (BarPositioner.BarAnchor barAnchor : BarPositioner.BarAnchor.allAnchors()) {
				ScreenRectangle anchorHitbox = barAnchor.getAnchorHitbox(barAnchor.getAnchorPosition(width, height));
				if (FancyStatusBars.barPositioner.getRowCount(barAnchor) != 0) {
					// this fixes flickering
					if (FancyStatusBars.barPositioner.getRowCount(barAnchor) == 1) {
						LinkedList<StatusBar> row = FancyStatusBars.barPositioner.getRow(barAnchor, 0);
						if (row.size() == 1 && row.getFirst() == cursorBar && anchorHitbox.overlaps(mouseRect)) inserted = true;
					}
					continue;
				}

				context.fill(anchorHitbox.left(), anchorHitbox.top(), anchorHitbox.right(), anchorHitbox.bottom(), 0x99FFFFFF);
				if (anchorHitbox.overlaps(mouseRect)) {
					inserted = true;
					if (currentInsertLocation.barAnchor() == barAnchor) continue;
					if (cursorBar.anchor != null)
						FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
					FancyStatusBars.barPositioner.addRow(barAnchor);
					FancyStatusBars.barPositioner.addBar(barAnchor, 0, cursorBar);
					currentInsertLocation = BarLocation.of(cursorBar);
					FancyStatusBars.updatePositions(true);
				}
			}
			if (!inserted) {
				if (cursorBar.anchor != null) FancyStatusBars.barPositioner.removeBar(cursorBar.anchor, cursorBar.gridY, cursorBar);
				currentInsertLocation = BarLocation.NULL;
				FancyStatusBars.updatePositions(true);
				cursorBar.setX(width + 5);
			}
		} else { // Not dragging around a bar
			if (resizing) { // actively resizing one or 2 bars
				int middleX; // the point between the 2 bars

				StatusBar rightBar = resizedBars.right();
				StatusBar leftBar = resizedBars.left();
				boolean hasRight = rightBar != null;
				boolean hasLeft = leftBar != null;
				BarPositioner.BarAnchor barAnchor;
				if (!hasRight) {
					barAnchor = leftBar.anchor;
					middleX = leftBar.getX() + leftBar.getWidth();
				} else {
					barAnchor = rightBar.anchor;
					middleX = rightBar.getX();
				}

				if (barAnchor != null) { // If is on an anchor
					BarPositioner.SizeRule sizeRule = barAnchor.getSizeRule();
					boolean doResize = true;

					float widthPerSize;
					if (sizeRule.isTargetSize())
						widthPerSize = (float) sizeRule.totalWidth() / sizeRule.targetSize();
					else
						widthPerSize = sizeRule.widthPerSize();

					// resize towards the left
					if (mouseX < middleX) {
						if (middleX - mouseX > widthPerSize / RESIZE_THRESHOLD) {
							if (hasRight) {
								if (rightBar.size + 1 > sizeRule.maxSize()) doResize = false;
							}
							if (hasLeft) {
								if (leftBar.size - 1 < sizeRule.minSize()) doResize = false;
							}

							if (doResize) {
								if (hasRight) rightBar.size++;
								if (hasLeft) leftBar.size--;
								FancyStatusBars.updatePositions(true);
							}
						}
					} else { // towards the right
						if (mouseX - middleX > widthPerSize / RESIZE_THRESHOLD) {
							if (hasRight) {
								if (rightBar.size - 1 < sizeRule.minSize()) doResize = false;
							}
							if (hasLeft) {
								if (leftBar.size + 1 > sizeRule.maxSize()) doResize = false;
							}

							if (doResize) {
								if (hasRight) rightBar.size--;
								if (hasLeft) leftBar.size++;
								FancyStatusBars.updatePositions(true);
							}
						}
					}
				} else { // Freely moving around
					if (hasLeft) {
						leftBar.setWidth(Math.max(BAR_MINIMUM_WIDTH, mouseX - leftBar.getX()));
					} else if (hasRight) {
						int endX = rightBar.getX() + rightBar.getWidth();
						rightBar.setX(Math.min(endX - BAR_MINIMUM_WIDTH, mouseX));
						rightBar.setWidth(endX - rightBar.getX());
					}
				}

			} else { // hovering bars
				rectLoop:
				for (ScreenRectangle screenRect : rectToBar.keySet()) {
					for (ScreenDirection direction : new ScreenDirection[]{ScreenDirection.LEFT, ScreenDirection.RIGHT}) {
						boolean overlaps = screenRect.getBorder(direction).step(direction).overlaps(mouseRect);

						if (overlaps && !editBarWidget.isMouseOver(mouseX, mouseY)) {
							Pair<StatusBar, BarLocation> barPair = rectToBar.get(screenRect);
							BarLocation barLocation = barPair.right();
							StatusBar bar = barPair.left();
							if (!bar.enabled) break;
							boolean right = direction.equals(ScreenDirection.RIGHT);
							if (barLocation.barAnchor() != null) {
								if (barLocation.barAnchor().getSizeRule().isTargetSize() && !FancyStatusBars.barPositioner.hasNeighbor(barLocation.barAnchor(), barLocation.y(), barLocation.x(), right)) {
									break;
								}
								if (!barLocation.barAnchor().getSizeRule().isTargetSize() && barLocation.x() == 0 && barLocation.barAnchor().isRight() != right)
									break;
							}
							resizeHover.first(bar);
							resizeHover.right(right);
							context.requestCursor(CursorTypes.RESIZE_EW);
							break rectLoop;
						} else {
							resizeHover.first(null);
						}
					}
				}
			}
		}
	}

	private static int getNeighborInsertX(BarLocation barLocation, boolean right) {
		BarPositioner.BarAnchor barAnchor = barLocation.barAnchor();
		int gridX = barLocation.x();
		if (barAnchor == null) return 0;
		if (right) {
			return barAnchor.isRight() ? gridX + 1 : gridX;
		} else {
			return barAnchor.isRight() ? gridX : gridX + 1;
		}
	}

	private static int getNeighborInsertY(BarLocation barLocation, boolean up) {
		BarPositioner.BarAnchor barAnchor = barLocation.barAnchor();
		int gridY = barLocation.y();
		if (barAnchor == null) return 0;
		if (up) {
			return barAnchor.isUp() ? gridY + 1 : gridY;
		} else {
			return barAnchor.isUp() ? gridY : gridY + 1;
		}
	}

	@Override
	protected void init() {
		super.init();
		FancyStatusBars.updatePositions(true);
		editBarWidget = new EditBarWidget(0, 0, this);
		editBarWidget.visible = false;
		addWidget(editBarWidget); // rendering separately to have it above hotbar
		Collection<StatusBar> values = FancyStatusBars.statusBars.values();
		values.forEach(this::setup);
		updateScreenRects();
		this.addRenderableWidget(Button.builder(Component.literal("?"),
						button -> minecraft.setScreen(new PopupScreen.Builder(this, Component.translatable("skyblocker.bars.config.explanationTitle"))
								.addButton(Component.translatable("gui.ok"), PopupScreen::onClose)
								.setMessage(Component.translatable("skyblocker.bars.config.explanation"))
								.build()))
				.bounds(width - 20, (height - 15) / 2, 15, 15)
				.build());
	}

	private void setup(StatusBar statusBar) {
		this.addRenderableWidget(statusBar);
		statusBar.setOnClick(this::onBarClick);
	}

	@Override
	public void removed() {
		super.removed();
		FancyStatusBars.statusBars.values().forEach(statusBar -> statusBar.setOnClick(null));
		if (cursorBar != null) cursorBar.inMouse = false;
		FancyStatusBars.updatePositions(false);
		FancyStatusBars.saveBarConfig();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	private void onBarClick(StatusBar statusBar, MouseButtonEvent click) {
		if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			cursorOffset = new ScreenPosition((int) (statusBar.getX() - click.x()), (int) (statusBar.getY() - click.y()));
			cursorBar = statusBar;
			cursorBar.inMouse = true;
			cursorBar.enabled = true;
			currentInsertLocation = BarLocation.of(cursorBar);
			if (statusBar.anchor != null)
				FancyStatusBars.barPositioner.removeBar(statusBar.anchor, statusBar.gridY, statusBar);
			FancyStatusBars.updatePositions(true);
			cursorBar.setX(width + 5); // send it to limbo lol
			updateScreenRects();
		} else if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
			int x = (int) Math.min(click.x() - 1, width - editBarWidget.getWidth());
			int y = (int) Math.min(click.y() - 1, height - editBarWidget.getHeight());
			editBarWidget.visible = true;
			editBarWidget.setStatusBar(statusBar);
			editBarWidget.setX(x);
			editBarWidget.setY(y);
		}
	}

	private void updateScreenRects() {
		rectToBar.clear();
		FancyStatusBars.statusBars.values().forEach(statusBar1 -> {
			if (!statusBar1.enabled) return;
			rectToBar.put(
					new ScreenRectangle(new ScreenPosition(statusBar1.getX(), statusBar1.getY()), statusBar1.getWidth(), statusBar1.getHeight()),
					Pair.of(statusBar1, BarLocation.of(statusBar1)));
		});
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent click) {
		if (cursorBar != null) {
			cursorBar.inMouse = false;
			if (currentInsertLocation == BarLocation.NULL) {
				cursorBar.x = (float) ((click.x() + cursorOffset.x()) / width);
				cursorBar.y = (float) ((click.y() + cursorOffset.y()) / height);
				cursorBar.width = Math.clamp(cursorBar.width, (float) BAR_MINIMUM_WIDTH / width, 1);
			}
			currentInsertLocation = BarLocation.NULL;
			cursorBar = null;
			FancyStatusBars.updatePositions(true);
			updateScreenRects();
			return true;
		} else if (resizing) {
			resizing = false;

			// update x and width if bar has no anchor
			StatusBar bar = null;
			if (resizedBars.left() != null) bar = resizedBars.left();
			else if (resizedBars.right() != null) bar = resizedBars.right();
			if (bar != null && bar.anchor == null) {
				bar.x = (float) bar.getX() / width;
				bar.width = (float) bar.getWidth() / width;
			}
			resizedBars.left(null);
			resizedBars.right(null);
			updateScreenRects();
			return true;
		}
		return super.mouseReleased(click);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		StatusBar first = resizeHover.first();
		// want the right click thing to have priority
		if (!editBarWidget.isMouseOver(click.x(), click.y()) && click.button() == 0 && first != null) {
			BarPositioner.BarAnchor barAnchor = first.anchor;
			if (barAnchor != null) {
				if (resizeHover.rightBoolean()) {
					resizedBars.left(first);

					if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, true)) {
						resizedBars.right(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? 1 : -1)));
					} else resizedBars.right(null);
				} else {
					resizedBars.right(first);

					if (FancyStatusBars.barPositioner.hasNeighbor(barAnchor, first.gridY, first.gridX, false)) {
						resizedBars.left(FancyStatusBars.barPositioner.getBar(barAnchor, first.gridY, first.gridX + (barAnchor.isRight() ? -1 : 1)));
					} else resizedBars.left(null);
				}
			} else { // if they have no anchor no need to do any checking
				if (resizeHover.rightBoolean()) {
					resizedBars.left(first);
					resizedBars.right(null);
				} else {
					resizedBars.right(first);
					resizedBars.left(null);
				}
			}
			resizing = true;
			return true;
		}
		return super.mouseClicked(click, doubled);
	}
}
