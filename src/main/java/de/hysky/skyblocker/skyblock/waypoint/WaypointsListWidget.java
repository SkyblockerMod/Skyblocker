package de.hysky.skyblocker.skyblock.waypoint;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.FrameLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.mixins.accessors.CheckboxAccessor;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.render.gui.CyclingIconButtonWidget;
import de.hysky.skyblocker.utils.render.gui.NoopInput;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

public class WaypointsListWidget extends ContainerObjectSelectionList<WaypointsListWidget.AbstractWaypointEntry> {
	private static final ResourceLocation DELETE_ICON = SkyblockerMod.id("trash_can");
	private static final int ICON_WIDTH = 12, ICON_HEIGHT = 15;
	private static final ResourceLocation INSERT_TEXTURE = SkyblockerMod.id("insert_button");
	private static final ResourceLocation INSERT_HIGHLIGHTED_TEXTURE = SkyblockerMod.id("insert_button_highlighted");

	private final AbstractWaypointsScreen<?> screen;
	private Location island;
	private List<WaypointGroup> waypoints;
	private InsertPosition insertPosition = null;
	private final Set<WaypointGroup> collapsedGroups = new ReferenceOpenHashSet<>(); // use identity hash code

	public WaypointsListWidget(Minecraft client, AbstractWaypointsScreen<?> screen, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		this.screen = screen;
		setIsland(screen.island);
	}

	@Override
	public int getRowWidth() {
		return 340;
	}

	@Override
	protected int scrollBarX() {
		return super.scrollBarX();
	}

	Optional<WaypointGroupEntry> getGroup() {
		if (getSelected() instanceof WaypointGroupEntry groupEntry) {
			return Optional.of(groupEntry);
		} else if (getSelected() instanceof WaypointEntry waypointEntry) {
			return Optional.of(waypointEntry.groupEntry);
		}
		return Optional.empty();
	}

	void setIsland(Location island) {
		this.island = island;
		waypoints = (List<WaypointGroup>) screen.waypoints.get(island);
		collapsedGroups.clear();
		collapsedGroups.addAll(waypoints);
		updateEntries();
	}

	void addWaypointGroupAfterSelected() {
		WaypointGroupEntry groupEntry = new WaypointGroupEntry();
		Optional<WaypointGroupEntry> selectedGroupEntryOptional = getGroup();
		int index = waypoints.size();
		if (selectedGroupEntryOptional.isPresent()) {
			WaypointGroupEntry selectedGroupEntry = selectedGroupEntryOptional.get();
			index = waypoints.indexOf(selectedGroupEntry.group) + 1;
		}
		waypoints.add(index, groupEntry.group);
		this.updateEntries();
	}

	@Override
	protected void renderListItems(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
		super.renderListItems(context, mouseX, mouseY, deltaTicks);
		insertPosition = null;
		int insertButtonY;
		int position;
		WaypointGroupEntry groupEntry;
		AbstractWaypointEntry hoveredEntry = getEntryAtPosition(getRowLeft() + getRowWidth() / 2d, mouseY);
		switch (hoveredEntry) {
			case null -> {
				if (children().isEmpty()) return;
				int rowBottom = getRowBottom(children().size() - 1);
				if (mouseY >= getBottom() || mouseY < rowBottom) return;
				insertButtonY = rowBottom;
				switch (children().getLast()) {
					case WaypointEntry entry -> {
						groupEntry = entry.groupEntry;
						position = children().size() - children().indexOf(groupEntry) - 1;
					}
					case WaypointGroupEntry waypointGroupEntry when !collapsedGroups.contains(waypointGroupEntry.group) -> {
						groupEntry = waypointGroupEntry;
						position = 0;
					}
					case null, default -> {
						return;
					}
				}
			}
			case WaypointEntry waypointEntry -> {
				int i = children().indexOf(waypointEntry);
				int rowTop = getRowTop(i);
				int rowBottom = getRowBottom(i);
				boolean top = Math.abs(mouseY - rowTop) < Math.abs(mouseY - rowBottom);
				groupEntry = waypointEntry.groupEntry;
				if (top) {
					position = i - children().indexOf(groupEntry) - 1;
					insertButtonY = rowTop;
				} else {
					position = i - children().indexOf(groupEntry);
					insertButtonY = rowBottom;
				}
			}
			case WaypointGroupEntry waypointGroupEntry -> {
				int i = children().indexOf(waypointGroupEntry);
				int rowTop = getRowTop(i);
				int rowBottom = getRowBottom(i);
				boolean top = Math.abs(mouseY - rowTop) < Math.abs(mouseY - rowBottom);
				if (top) {
					insertButtonY = rowTop;
					AbstractWaypointEntry above = nextEntry(ScreenDirection.UP, ignored -> true, hoveredEntry);
					switch (above) {
						case WaypointEntry entry -> {
							groupEntry = entry.groupEntry;
							position = children().indexOf(above) - children().indexOf(groupEntry);
						}
						case WaypointGroupEntry group when !collapsedGroups.contains(group.group) -> {
							groupEntry = group;
							position = 0;
						}
						case null, default -> {
							return;
						}
					}
				} else if (!collapsedGroups.contains(waypointGroupEntry.group)) {
					insertButtonY = rowBottom;
					position = 0;
					groupEntry = waypointGroupEntry;
				} else return;
			}
			default -> {
				return;
			}
		}


		int mX = mouseX - getRowLeft();
		if (insertButtonY <= getY() || insertButtonY >= getBottom() || mX > 32) return;
		boolean hovering = isMouseOver(mouseX, mouseY) && Math.abs(mouseY - insertButtonY) <= 6 && mX < 16 && mX >= -8;
		context.blitSprite(RenderPipelines.GUI_TEXTURED, hovering ? INSERT_HIGHLIGHTED_TEXTURE : INSERT_TEXTURE, getRowLeft(), insertButtonY - 5, 48, 11);
		if (Debug.debugEnabled()) context.drawString(minecraft.font, String.valueOf(position), getX(), getY(), -1, true);
		if (hovering) {
			insertPosition = new InsertPosition(groupEntry, position);
			context.requestCursor(CursorTypes.POINTING_HAND);
		}
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
		if (insertPosition != null) {
			WaypointEntry entry = new WaypointEntry(insertPosition.groupEntry);
			insertPosition.groupEntry.group.waypoints().add(insertPosition.position, entry.waypoint);
			updateEntries();
		}
		return super.mouseClicked(click, doubled);
	}

	void updateEntries() {
		clearEntries();
		for (WaypointGroup group : waypoints) {
			boolean collapsed = collapsedGroups.contains(group);
			WaypointGroupEntry groupEntry = new WaypointGroupEntry(group, collapsed);
			addEntry(groupEntry);
			if (collapsed) continue;
			for (NamedWaypoint waypoint : group.waypoints()) {
				addEntry(new WaypointEntry(groupEntry, waypoint));
			}
		}
	}

	void updateButtons() {
		for (net.minecraft.client.gui.components.ContainerObjectSelectionList.Entry<AbstractWaypointEntry> entry : children()) {
			if (entry instanceof WaypointGroupEntry groupEntry && groupEntry.enabled.selected() != groupEntry.shouldBeChecked()) {
				((CheckboxAccessor) groupEntry.enabled).setSelected(!groupEntry.enabled.selected());
			} else if (entry instanceof WaypointEntry waypointEntry && waypointEntry.enabled.selected() != screen.isEnabled(waypointEntry.waypoint)) {
				waypointEntry.enabled.onPress(NoopInput.INSTANCE);
			}
		}
	}

	private BlockPos getDefaultPos() {
		return minecraft.hitResult instanceof BlockHitResult blockHitResult && minecraft.hitResult.getType() == HitResult.Type.BLOCK ? blockHitResult.getBlockPos() : minecraft.player != null ? minecraft.player.blockPosition() : BlockPos.ZERO;
	}

	protected abstract static class AbstractWaypointEntry extends ContainerObjectSelectionList.Entry<AbstractWaypointEntry> {
	}

	protected class WaypointGroupEntry extends AbstractWaypointEntry {
		private WaypointGroup group;
		private final List<AbstractWidget> children;
		private final Checkbox enabled;
		private final FrameLayout layout = new FrameLayout(getRowWidth(), defaultEntryHeight);

		public WaypointGroupEntry() {
			this(new WaypointGroup("New Group", island), false);
		}

		public WaypointGroupEntry(WaypointGroup initialGroup, boolean collapsed) {
			this.group = initialGroup;
			//After this point do not use the initialGroup parameter (especially in lambdas!)
			//doing so will result in any changes made not being saved if the group is replaced such as by editing its name
			//or checking the ordered tick box as those methods replace the instance and the lambdas capture the instance on creation
			//and will thus write to the old group instance rather than the latest one
			LinearLayout leftLayout = LinearLayout.horizontal().spacing(4);
			leftLayout.defaultCellSetting().alignVerticallyMiddle();
			layout.addChild(leftLayout, LayoutSettings::alignHorizontallyLeft);
			LinearLayout rightLayout = LinearLayout.horizontal().spacing(4);
			rightLayout.defaultCellSetting().alignVerticallyMiddle();
			layout.addChild(rightLayout, LayoutSettings::alignHorizontallyRight);

			Component arrow = Component.nullToEmpty(collapsed ? "▲" : "▼");
			Button collapseWaypoint = Button.builder(arrow, button -> {
				if (collapsed) collapsedGroups.remove(group);
				else collapsedGroups.add(group);
				updateEntries();
			}).size(11, 11).build();
			leftLayout.addChild(collapseWaypoint);

			enabled = Checkbox.builder(Component.empty(), minecraft.font).selected(shouldBeChecked()).onValueChange((checkbox, checked) -> group.waypoints().forEach(waypoint -> screen.enabledChanged(waypoint, checked))).build();
			leftLayout.addChild(enabled);

			EditBox nameField = new EditBox(minecraft.font, 70, 20, Component.literal("Name"));
			nameField.setValue(group.name());
			nameField.setResponder(this::updateName);
			leftLayout.addChild(nameField);

			CyclingIconButtonWidget<Boolean> orderedWidget = leftLayout.addChild(new CyclingIconButtonWidget<>(
					20,
					20,
					group.ordered(),
					new Boolean[]{Boolean.FALSE, Boolean.TRUE},
					b -> new CyclingIconButtonWidget.Icon(SkyblockerMod.id("waypoints_screen/ordered_" + (b ? "enabled" : "disabled")), 16, 16),
					b -> Tooltip.create(CommonComponents.optionNameValue(Component.translatable("skyblocker.waypoints.groupType"), Component.translatable(b ? "skyblocker.waypoints.groupType.ordered" : "skyblocker.waypoints.groupType.normal").withStyle(ChatFormatting.YELLOW))),
					this::updateOrdered
			));
			CyclingIconButtonWidget<Boolean> throughWallsWidget = leftLayout.addChild(new CyclingIconButtonWidget<>(
					20,
					20,
					group.renderThroughWalls(),
					new Boolean[]{Boolean.FALSE, Boolean.TRUE},
					b -> new CyclingIconButtonWidget.Icon(SkyblockerMod.id("waypoints_screen/through_walls_" + (b ? "enabled" : "disabled")), 15, 16),
					b -> Tooltip.create(CommonComponents.optionNameValue(Component.translatable("skyblocker.waypoints.throughWalls"), (b ? CommonComponents.GUI_YES : CommonComponents.GUI_NO).copy().withStyle(ChatFormatting.YELLOW))),
					this::updateRenderThroughWalls
			));
			CyclingIconButtonWidget<Waypoint.Type> waypointTypeWidget = leftLayout.addChild(new CyclingIconButtonWidget<>(
					20,
					20,
					group.waypointType(),
					Waypoint.Type.values(),
					t -> new CyclingIconButtonWidget.Icon(SkyblockerMod.id("waypoints_screen/waypoint_type_" + t.getSerializedName()), 12, 15),
					t -> Tooltip.create(CommonComponents.optionNameValue(Component.translatable("skyblocker.waypoints.waypointType"), Component.literal(t.toString()).withStyle(ChatFormatting.YELLOW))),
					this::updateWaypointType
			));

			Button buttonNewWaypoint = Button.builder(Component.translatable("skyblocker.waypoints.new"), ignored -> {
				WaypointEntry waypointEntry = new WaypointEntry(this);
				group.waypoints().add(waypointEntry.waypoint);
				WaypointsListWidget.this.updateEntries();
				if (collapsed) {
					collapsedGroups.remove(group);
					updateEntries();
				}
			}).width(72).build();
			rightLayout.addChild(buttonNewWaypoint);

			Component deleteText = Component.translatable("selectServer.deleteButton");
			Button buttonDelete = SpriteIconButton.builder(deleteText, ignored -> {
				waypoints.remove(group);
				updateEntries();
			}, true).size(20, 20).sprite(DELETE_ICON, ICON_WIDTH, ICON_HEIGHT).build();
			buttonDelete.setTooltip(Tooltip.create(deleteText));
			rightLayout.addChild(buttonDelete);

			layout.arrangeElements();
			children = List.of(enabled, nameField, orderedWidget, throughWallsWidget, waypointTypeWidget, buttonNewWaypoint, buttonDelete, collapseWaypoint);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		private boolean shouldBeChecked() {
			return !group.waypoints().isEmpty() && group.waypoints().stream().allMatch(screen::isEnabled);
		}

		private void updateName(String name) {
			int index = waypoints.indexOf(group);
			group = group.withName(name);
			if (index >= 0) {
				waypoints.set(index, group);
			}
		}

		private void updateOrdered(boolean ordered) {
			int index = waypoints.indexOf(group);
			group = group.withOrdered(ordered);
			if (index >= 0) {
				waypoints.set(index, group);
			}
		}

		private void updateRenderThroughWalls(boolean renderThroughWalls) {
			int index = waypoints.indexOf(group);
			group = group.withRenderThroughWalls(renderThroughWalls);
			if (index >= 0) {
				waypoints.set(index, group);
			}
		}

		private void updateWaypointType(Waypoint.Type waypointType) {
			int index = waypoints.indexOf(group);
			group = group.withWaypointType(waypointType);
			if (index >= 0) {
				waypoints.set(index, group);
			}
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			layout.setPosition(this.getX(), this.getY());
			for (AbstractWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}
	}

	// Allow to use the scroll wheel in the argb text input
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent() || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	protected class WaypointEntry extends AbstractWaypointEntry {
		private final WaypointGroupEntry groupEntry;
		private NamedWaypoint waypoint;
		private final List<AbstractWidget> children;
		private final Button buttonUp;
		private final Button buttonDown;
		private final Checkbox enabled;
		private final ColorPickerButton colorPickerButton;

		private final FrameLayout layout = new FrameLayout(getRowWidth(), defaultEntryHeight);

		public WaypointEntry(WaypointGroupEntry groupEntry) {
			this(groupEntry, groupEntry.group.createWaypoint(getDefaultPos()));
		}

		public WaypointEntry(WaypointGroupEntry groupEntry, NamedWaypoint initialWaypoint) {
			this.groupEntry = groupEntry;
			this.waypoint = initialWaypoint;
			//Do not use the initialWaypoint parameter after here for the same reasons as the group one

			LinearLayout leftLayout = LinearLayout.horizontal();
			leftLayout.defaultCellSetting().alignVerticallyMiddle();
			layout.addChild(leftLayout, LayoutSettings::alignHorizontallyLeft);
			leftLayout.addChild(SpacerElement.width(6));

			buttonUp = Button.builder(Component.nullToEmpty("↑"), button -> this.shiftWaypointIndex(-1))
					.size(11, 11).build();
			leftLayout.addChild(buttonUp);
			buttonDown = Button.builder(Component.nullToEmpty("↓"), button -> this.shiftWaypointIndex(1))
					.size(11, 11).build();
			leftLayout.addChild(buttonDown);
			enabled = Checkbox.builder(Component.literal(""), minecraft.font).selected(screen.isEnabled(waypoint)).onValueChange((checkbox, checked) -> screen.enabledChanged(waypoint, checked)).build();
			leftLayout.addChild(enabled, p -> p.paddingLeft(4));
			EditBox nameField = new EditBox(minecraft.font, 65, 20, Component.literal("Name"));
			nameField.setValue(waypoint.getName().getString());
			nameField.setResponder(this::updateName);
			leftLayout.addChild(nameField, p -> p.paddingLeft(2));

			leftLayout.addChild(new StringWidget(Component.literal("X:"), minecraft.font), p -> p.paddingLeft(2));
			EditBox xField = new EditBox(minecraft.font, 26, 20, Component.literal("X"));
			xField.setValue(Integer.toString(waypoint.pos.getX()));
			xField.setFilter(this::checkInt);
			xField.setResponder(this::updateX);
			leftLayout.addChild(xField);

			leftLayout.addChild(new StringWidget(Component.literal("Y:"), minecraft.font), p -> p.paddingLeft(2));
			EditBox yField = new EditBox(minecraft.font, 26, 20, Component.literal("Y"));
			yField.setValue(Integer.toString(waypoint.pos.getY()));
			yField.setFilter(this::checkInt);
			yField.setResponder(this::updateY);
			leftLayout.addChild(yField);

			leftLayout.addChild(new StringWidget(Component.literal("Z:"), minecraft.font), p -> p.paddingLeft(2));
			EditBox zField = new EditBox(minecraft.font, 26, 20, Component.literal("Z"));
			zField.setValue(Integer.toString(waypoint.pos.getZ()));
			zField.setFilter(this::checkInt);
			zField.setResponder(this::updateZ);
			leftLayout.addChild(zField);

			ARGBTextInput colorField = new ARGBTextInput(0, 0, minecraft.font, true, true);
			colorPickerButton = leftLayout.addChild(new ColorPickerButton(colorField, this::updateColor), p -> p.paddingLeft(2));
			int color = ARGB.colorFromFloat(waypoint.alpha, waypoint.colorComponents[0], waypoint.colorComponents[1], waypoint.colorComponents[2]);
			colorPickerButton.color = color;
			colorField.setARGBColor(color);
			colorField.setHeight(20);
			colorField.setOnChange(this::updateColor);
			leftLayout.addChild(colorField);

			Component deleteText = Component.translatable("selectServer.deleteButton");
			Button buttonDelete = SpriteIconButton.builder(deleteText, button -> {
				groupEntry.group.waypoints().remove(waypoint);
				WaypointsListWidget.this.updateEntries();
			}, true).size(20, 20).sprite(DELETE_ICON, ICON_WIDTH, ICON_HEIGHT).build();
			buttonDelete.setTooltip(Tooltip.create(deleteText));
			layout.addChild(buttonDelete, LayoutSettings::alignHorizontallyRight);
			layout.arrangeElements();
			ImmutableList.Builder<AbstractWidget> builder = ImmutableList.builder();
			layout.visitWidgets(builder::add);
			children = builder.build();
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return children;
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return children;
		}

		private void shiftWaypointIndex(int shift) {
			int currentIndex = groupEntry.group.waypoints().indexOf(waypoint);
			int newIndex = Math.clamp(currentIndex + shift, 0, groupEntry.group.waypoints().size() - 1);

			groupEntry.group.waypoints().remove(currentIndex);
			groupEntry.group.waypoints().add(newIndex, waypoint);
			WaypointsListWidget.this.updateEntries();
		}

		private void updateName(String name) {
			if (waypoint.name.getString().equals(name)) return;
			int index = groupEntry.group.waypoints().indexOf(waypoint);
			waypoint = waypoint.withName(name);
			if (index >= 0) {
				groupEntry.group.waypoints().set(index, waypoint);
			}
		}

		private boolean checkInt(String string) {
			try {
				parseEmptiableInt(string);
				return true;
			} catch (NumberFormatException e) {
				return false;
			}
		}

		private void updateX(String xString) {
			updateInt(xString, waypoint.pos.getX(), waypoint::withX);
		}

		private void updateY(String yString) {
			updateInt(yString, waypoint.pos.getY(), waypoint::withY);
		}

		private void updateZ(String zString) {
			updateInt(zString, waypoint.pos.getZ(), waypoint::withZ);
		}

		private void updateInt(String newValueString, int currentValue, Int2ObjectFunction<NamedWaypoint> wither) {
			try {
				int index = groupEntry.group.waypoints().indexOf(waypoint);
				int newValue = parseEmptiableInt(newValueString);
				if (newValue == currentValue) return;
				waypoint = wither.apply(newValue);
				if (index >= 0) {
					groupEntry.group.waypoints().set(index, waypoint);
				}
			} catch (NumberFormatException e) {
				Waypoints.LOGGER.warn("[Skyblocker Waypoints] Failed to parse integer: {}", newValueString, e);
			}
		}

		private void updateColor(int colorInt) {
			int index = groupEntry.group.waypoints().indexOf(waypoint);
			float[] colorComponents = {((colorInt & 0x00FF0000) >> 16) / 255f, ((colorInt & 0x0000FF00) >> 8) / 255f, (colorInt & 0x000000FF) / 255f};
			float alpha = ((colorInt & 0xFF000000) >>> 24) / 255f;
			if (Arrays.equals(waypoint.colorComponents, colorComponents) && waypoint.alpha == alpha) return;
			waypoint = waypoint.withColor(colorComponents, alpha);
			if (index >= 0) {
				groupEntry.group.waypoints().set(index, waypoint);
			}
			colorPickerButton.color = colorInt;
		}

		private int parseEmptiableInt(String value) throws NumberFormatException {
			return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
		}

		@Override
		public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
			layout.setPosition(this.getX(), this.getY());
			boolean showButtons = hovered && mouseY >= buttonUp.getY() - 1 && mouseY <= buttonUp.getBottom();
			buttonUp.visible = showButtons;
			buttonDown.visible = showButtons;
			for (AbstractWidget child : children) {
				child.render(context, mouseX, mouseY, deltaTicks);
			}
		}
	}

	private class ColorPickerButton extends AbstractButton {
		private final ARGBTextInput textInput;
		private final IntConsumer colorConsumer;
		private int color;

		ColorPickerButton(ARGBTextInput textInput, IntConsumer colorConsumer) {
			super(0, 0, 20, 20, Component.empty());
			this.textInput = textInput;
			this.colorConsumer = colorConsumer;
		}

		@Override
		public void onPress(InputWithModifiers input) {
			ColorPickerWidget widget = new ColorPickerWidget(0, 0, 200, 110, true);
			widget.setOnColorChange((color, mouseRelease) -> {
				textInput.setARGBColor(color);
				this.color = color;
				if (mouseRelease) colorConsumer.accept(color);
			});
			widget.setARGBColor(textInput.getARGBColor());
			screen.setPopup(widget, getX(), getBottom());
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float deltaTicks) {
			int padding = 1;
			context.fill(getX() + padding, getY() + padding, getRight() - padding, getBottom() - padding, isHovered() ? CommonColors.WHITE : CommonColors.BLACK);
			context.fill(getX() + padding + 1, getY() + padding + 1, getRight() - padding - 1, getBottom() - padding - 1, this.color);

			if (this.isHovered()) {
				context.requestCursor(CursorTypes.POINTING_HAND);
			}
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {

		}
	}

	private record InsertPosition(WaypointGroupEntry groupEntry, int position) {}
}
