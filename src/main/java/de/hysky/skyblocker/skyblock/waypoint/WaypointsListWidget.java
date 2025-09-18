package de.hysky.skyblocker.skyblock.waypoint;

import com.google.common.collect.ImmutableList;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.mixins.accessors.CheckboxWidgetAccessor;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.ARGBTextInput;
import de.hysky.skyblocker.utils.render.gui.ColorPickerWidget;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.WaypointGroup;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.navigation.NavigationDirection;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;

import java.util.*;

public class WaypointsListWidget extends ElementListWidget<WaypointsListWidget.AbstractWaypointEntry> {
	private static final Identifier INSERT_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "insert_button");
	private static final Identifier INSERT_HIGHLIGHTED_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "insert_button_highlighted");

	private final AbstractWaypointsScreen<?> screen;
	private Location island;
	private List<WaypointGroup> waypoints;
	private InsertPosition insertPosition = null;
	private final Set<WaypointGroup> collapsedGroups = new ReferenceOpenHashSet<>(); // use identity hash code

	public WaypointsListWidget(MinecraftClient client, AbstractWaypointsScreen<?> screen, int width, int height, int y, int itemHeight) {
		super(client, width, height, y, itemHeight);
		this.screen = screen;
		setIsland(screen.island);
	}

	@Override
	public int getRowWidth() {
		return 350;
	}

	@Override
	protected int getScrollbarX() {
		return super.getScrollbarX();
	}

	Optional<WaypointGroupEntry> getGroup() {
		if (getSelectedOrNull() instanceof WaypointGroupEntry groupEntry) {
			return Optional.of(groupEntry);
		} else if (getSelectedOrNull() instanceof WaypointEntry waypointEntry) {
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
		int entryIndex = children().size();
		if (selectedGroupEntryOptional.isPresent()) {
			WaypointGroupEntry selectedGroupEntry = selectedGroupEntryOptional.get();
			index = waypoints.indexOf(selectedGroupEntry.group) + 1;
			entryIndex = children().indexOf(selectedGroupEntry) + 1;
			while (entryIndex < children().size() && !(children().get(entryIndex) instanceof WaypointGroupEntry)) {
				entryIndex++;
			}
		}
		waypoints.add(index, groupEntry.group);
		children().add(entryIndex, groupEntry);
	}

	@Override
	protected void renderList(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
		super.renderList(context, mouseX, mouseY, deltaTicks);
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
					case WaypointGroupEntry waypointGroupEntry when !collapsedGroups.contains(waypointGroupEntry.group)-> {
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
					AbstractWaypointEntry above = getNeighboringEntry(NavigationDirection.UP, ignored -> true, hoveredEntry);
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
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, hovering ? INSERT_HIGHLIGHTED_TEXTURE : INSERT_TEXTURE, getRowLeft(), insertButtonY - 5, 48, 11);
		if (Debug.debugEnabled()) context.drawText(client.textRenderer, String.valueOf(position), getX(), getY(), -1, true);
		if (hovering) insertPosition = new InsertPosition(groupEntry, position);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (insertPosition != null) {
			WaypointEntry entry = new WaypointEntry(insertPosition.groupEntry);
			insertPosition.groupEntry.group.waypoints().add(insertPosition.position, entry.waypoint);
			children().add(insertPosition.position + children().indexOf(insertPosition.groupEntry) + 1, entry);
		}
		return super.mouseClicked(mouseX, mouseY, button);
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
		for (Entry<AbstractWaypointEntry> entry : children()) {
			if (entry instanceof WaypointGroupEntry groupEntry && groupEntry.enabled.isChecked() != groupEntry.shouldBeChecked()) {
				((CheckboxWidgetAccessor) groupEntry.enabled).setChecked(!groupEntry.enabled.isChecked());
			} else if (entry instanceof WaypointEntry waypointEntry && waypointEntry.enabled.isChecked() != screen.isEnabled(waypointEntry.waypoint)) {
				waypointEntry.enabled.onPress();
			}
		}
	}

	private BlockPos getDefaultPos() {
		return client.crosshairTarget instanceof BlockHitResult blockHitResult && client.crosshairTarget.getType() == HitResult.Type.BLOCK ? blockHitResult.getBlockPos() : client.player != null ? client.player.getBlockPos() : BlockPos.ORIGIN;
	}

	protected abstract static class AbstractWaypointEntry extends ElementListWidget.Entry<AbstractWaypointEntry> {
	}

	protected class WaypointGroupEntry extends AbstractWaypointEntry {
		private WaypointGroup group;
		private final List<ClickableWidget> children;
		private final CheckboxWidget enabled;
		private final SimplePositioningWidget layout = new SimplePositioningWidget(getRowWidth(), itemHeight);

		public WaypointGroupEntry() {
			this(new WaypointGroup("New Group", island, new ArrayList<>()), false);
		}

		public WaypointGroupEntry(WaypointGroup initialGroup, boolean collapsed) {
			this.group = initialGroup;
			//After this point do not use the initialGroup parameter (especially in lambdas!)
			//doing so will result in any changes made not being saved if the group is replaced such as by editing its name
			//or checking the ordered tick box as those methods replace the instance and the lambdas capture the instance on creation
			//and will thus write to the old group instance rather than the latest one
			DirectionalLayoutWidget leftLayout = DirectionalLayoutWidget.horizontal().spacing(4);
			leftLayout.getMainPositioner().alignVerticalCenter();
			layout.add(leftLayout, Positioner::alignLeft);
			DirectionalLayoutWidget rightLayout = DirectionalLayoutWidget.horizontal().spacing(4);
			rightLayout.getMainPositioner().alignVerticalCenter();
			layout.add(rightLayout, Positioner::alignRight);

			Text arrow = Text.of(collapsed ? "▲" : "▼");
			ButtonWidget collapseWaypoint = ButtonWidget.builder(arrow, button -> {
				if (collapsed) collapsedGroups.remove(group);
				else collapsedGroups.add(group);
				updateEntries();
			}).size(11, 11).build();
			leftLayout.add(collapseWaypoint);

			enabled = CheckboxWidget.builder(Text.empty(), client.textRenderer).checked(shouldBeChecked()).callback((checkbox, checked) -> group.waypoints().forEach(waypoint -> screen.enabledChanged(waypoint, checked))).build();
			leftLayout.add(enabled);

			TextFieldWidget nameField = new TextFieldWidget(client.textRenderer, 70, 20, Text.literal("Name"));
			nameField.setText(group.name());
			nameField.setChangedListener(this::updateName);
			leftLayout.add(nameField);

			CheckboxWidget ordered = CheckboxWidget.builder(Text.literal("Ordered"), client.textRenderer).checked(group.ordered()).callback((checkbox, checked) -> updateOrdered(checked)).build();
			leftLayout.add(ordered);

			ButtonWidget buttonNewWaypoint = ButtonWidget.builder(Text.translatable("skyblocker.waypoints.new"), ignored -> {
				WaypointEntry waypointEntry = new WaypointEntry(this);
				int entryIndex;
				if (getSelectedOrNull() instanceof WaypointEntry selectedWaypointEntry && selectedWaypointEntry.groupEntry == this) {
					entryIndex = WaypointsListWidget.this.children().indexOf(selectedWaypointEntry) + 1;
				} else {
					entryIndex = WaypointsListWidget.this.children().indexOf(this) + 1;
					while (entryIndex < WaypointsListWidget.this.children().size() && !(WaypointsListWidget.this.children().get(entryIndex) instanceof WaypointGroupEntry)) {
						entryIndex++;
					}
				}
				group.waypoints().add(waypointEntry.waypoint);
				WaypointsListWidget.this.children().add(entryIndex, waypointEntry);
				if (collapsed) {
					collapsedGroups.remove(group);
					updateEntries();
				}
			}).width(72).build();
			rightLayout.add(buttonNewWaypoint);

			ButtonWidget buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), ignored -> {
				int entryIndex = WaypointsListWidget.this.children().indexOf(this) + 1;
				while (entryIndex < WaypointsListWidget.this.children().size() && !(WaypointsListWidget.this.children().get(entryIndex) instanceof WaypointGroupEntry)) {
					WaypointsListWidget.this.children().remove(entryIndex);
				}
				WaypointsListWidget.this.children().remove(this);
				waypoints.remove(group);
			}).width(38).build();
			rightLayout.add(buttonDelete);

			layout.refreshPositions();
			children = List.of(enabled, nameField, ordered, buttonNewWaypoint, buttonDelete, collapseWaypoint);
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
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

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			layout.setPosition(x, y);
			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, tickDelta);
			}
		}
	}

	// Allow to use the scroll wheel in the argb text input
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		return this.hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent() || super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	protected class WaypointEntry extends AbstractWaypointEntry {
		private final WaypointGroupEntry groupEntry;
		private NamedWaypoint waypoint;
		private final List<ClickableWidget> children;
		private final ButtonWidget buttonUp;
		private final ButtonWidget buttonDown;
		private final CheckboxWidget enabled;
		private final ColorPickerButton colorPickerButton;

		private final SimplePositioningWidget layout = new SimplePositioningWidget(getRowWidth(), itemHeight);

		public WaypointEntry(WaypointGroupEntry groupEntry) {
			this(groupEntry, groupEntry.group.createWaypoint(getDefaultPos()));
		}

		public WaypointEntry(WaypointGroupEntry groupEntry, NamedWaypoint initialWaypoint) {
			this.groupEntry = groupEntry;
			this.waypoint = initialWaypoint;
			//Do not use the initialWaypoint parameter after here for the same reasons as the group one

			DirectionalLayoutWidget leftLayout = DirectionalLayoutWidget.horizontal();
			leftLayout.getMainPositioner().alignVerticalCenter();
			layout.add(leftLayout, Positioner::alignLeft);
			leftLayout.add(EmptyWidget.ofWidth(6));

			buttonUp = ButtonWidget.builder(Text.of("↑"), button -> {
				this.shiftWaypointIndex(-1);
			}).size(11, 11).build();
			leftLayout.add(buttonUp);
			buttonDown = ButtonWidget.builder(Text.of("↓"), button -> {
				this.shiftWaypointIndex(1);
			}).size(11, 11).build();
			leftLayout.add(buttonDown);
			enabled = CheckboxWidget.builder(Text.literal(""), client.textRenderer).checked(screen.isEnabled(waypoint)).callback((checkbox, checked) -> screen.enabledChanged(waypoint, checked)).build();
			leftLayout.add(enabled, p -> p.marginLeft(4));
			TextFieldWidget nameField = new TextFieldWidget(client.textRenderer, 65, 20, Text.literal("Name"));
			nameField.setText(waypoint.getName().getString());
			nameField.setChangedListener(this::updateName);
			leftLayout.add(nameField, p -> p.marginLeft(2));

			leftLayout.add(new TextWidget(Text.literal("X:"), client.textRenderer), p -> p.marginLeft(2));
			TextFieldWidget xField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("X"));
			xField.setText(Integer.toString(waypoint.pos.getX()));
			xField.setTextPredicate(this::checkInt);
			xField.setChangedListener(this::updateX);
			leftLayout.add(xField);

			leftLayout.add(new TextWidget(Text.literal("Y:"), client.textRenderer), p -> p.marginLeft(2));
			TextFieldWidget yField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("Y"));
			yField.setText(Integer.toString(waypoint.pos.getY()));
			yField.setTextPredicate(this::checkInt);
			yField.setChangedListener(this::updateY);
			leftLayout.add(yField);

			leftLayout.add(new TextWidget(Text.literal("Z:"), client.textRenderer), p -> p.marginLeft(2));
			TextFieldWidget zField = new TextFieldWidget(client.textRenderer, 26, 20, Text.literal("Z"));
			zField.setText(Integer.toString(waypoint.pos.getZ()));
			zField.setTextPredicate(this::checkInt);
			zField.setChangedListener(this::updateZ);
			leftLayout.add(zField);

			ARGBTextInput colorField = new ARGBTextInput(0, 0, client.textRenderer, true, true);
			colorPickerButton = leftLayout.add(new ColorPickerButton(colorField, this::updateColor), p -> p.marginLeft(2));
			int color = ColorHelper.fromFloats(waypoint.alpha, waypoint.colorComponents[0], waypoint.colorComponents[1], waypoint.colorComponents[2]);
			colorPickerButton.color = ColorHelper.fullAlpha(color);
			colorField.setARGBColor(color);
			colorField.setHeight(20);
			colorField.setOnChange(this::updateColor);
			leftLayout.add(colorField);

			ButtonWidget buttonDelete = ButtonWidget.builder(Text.translatable("selectServer.deleteButton"), button -> {
				groupEntry.group.waypoints().remove(waypoint);
				WaypointsListWidget.this.children().remove(this);
			}).width(38).build();
			layout.add(buttonDelete, Positioner::alignRight);
			layout.refreshPositions();
			ImmutableList.Builder<ClickableWidget> builder = ImmutableList.builder();
			layout.forEachChild(builder::add);
			children = builder.build();
		}

		@Override
		public List<? extends Selectable> selectableChildren() {
			return children;
		}

		@Override
		public List<? extends Element> children() {
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
			colorPickerButton.color = ColorHelper.fullAlpha(colorInt);
		}

		private int parseEmptiableInt(String value) throws NumberFormatException {
			return value.isEmpty() || value.equals("-") ? 0 : Integer.parseInt(value);
		}

		@Override
		public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			layout.setPosition(x, y);
			boolean showButtons = hovered && mouseY >= buttonUp.getY() - 1 && mouseY <= buttonUp.getBottom();
			buttonUp.visible = showButtons;
			buttonDown.visible = showButtons;
			for (ClickableWidget child : children) {
				child.render(context, mouseX, mouseY, tickDelta);
			}
		}
	}

	private class ColorPickerButton extends PressableWidget {
		private final ARGBTextInput textInput;
		private final IntConsumer colorConsumer;
		private int color;

		ColorPickerButton(ARGBTextInput textInput, IntConsumer colorConsumer) {
			super(0, 0, 20, 20, Text.empty());
			this.textInput = textInput;
			this.colorConsumer = colorConsumer;
		}

		@Override
		public void onPress() {
			ColorPickerWidget widget = new ColorPickerWidget(0, 0, 200, 100);
			widget.setOnColorChange((color, mouseRelease) -> {
				int argb = ColorHelper.withAlpha(ColorHelper.getAlpha(textInput.getARGBColor()), color);
				textInput.setARGBColor(argb);
				this.color = color;
				if (mouseRelease) colorConsumer.accept(argb);
			});
			widget.setRGBColor(textInput.getARGBColor());
			screen.setPopup(widget, getX(), getBottom());
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float deltaTicks) {
			int padding = 1;
			context.fill(getX() + padding, getY() + padding, getRight() - padding, getBottom() - padding, isHovered() ? Colors.WHITE : Colors.BLACK);
			context.fill(getX() + padding + 1, getY() + padding + 1, getRight() - padding - 1, getBottom() - padding - 1, this.color);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {

		}
	}

	private record InsertPosition(WaypointGroupEntry groupEntry, int position) {}
}
