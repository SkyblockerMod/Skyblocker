package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.mixins.accessors.CheckboxAccessor;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.LayerConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

class CopyToPopup extends AbstractPopupScreen {
	private final LinearLayout layout = LinearLayout.vertical().spacing(2);
	private final Set<Location> selectedLocations;
	private final PositionedWidget copiedWidget;
	private final Location location;
	private final WidgetManager.ScreenLayer layer;
	private boolean copyPosition = true;
	private ScrollableLayout scrollable;

	CopyToPopup(Screen backgroundScreen, PositionedWidget copiedWidget, Location location, WidgetManager.ScreenLayer layer) {
		super(Component.literal("Edit hidden widgets"), backgroundScreen);
		this.copiedWidget = copiedWidget;
		this.selectedLocations = WidgetManager.getCopyTracker().get(layer)
				.get(copiedWidget.widget.getInternalID())
				.flatMap(s -> s.whereHas(location))
				.map(EnumSet::copyOf)
				.orElseGet(() -> EnumSet.noneOf(Location.class));
		this.location = location;
		this.layer = layer;

		this.layout.defaultCellSetting().alignHorizontallyCenter();
	}

	@Override
	protected void init() {
		layout.addChild(Checkbox.builder(Component.literal("Copy Position"), font)
				.selected(selectedLocations.isEmpty()) // automatically select if it's empty
				.tooltip(Tooltip.create(Component.literal("If unchecked will not copy the position and only affect locations where the widget is already present.")))
				.onValueChange((_, value) -> copyPosition = value).build()
		);
		layout.addChild(new StringWidget(Component.literal("Target locations").withStyle(ChatFormatting.BOLD), font), settings -> settings.paddingVertical(4));
		LinearLayout content = LinearLayout.vertical().spacing(2);

		List<Checkbox> checkboxes = new ArrayList<>();
		Checkbox selectAll = content.addChild(Checkbox.builder(Component.literal("Select All").withStyle(ChatFormatting.BOLD), font)
				.maxWidth(200)
				.selected(selectedLocations.containsAll(WidgetManager.ALLOWED_LOCATIONS))
				.onValueChange((_, value) -> {
					if (value) {
						selectedLocations.clear();
						selectedLocations.addAll(WidgetManager.ALLOWED_LOCATIONS);
					} else {
						selectedLocations.clear();
					}
					checkboxes.forEach(checkbox -> ((CheckboxAccessor) checkbox).setSelected(value));
				})
				.build());
		for (Location location : WidgetManager.ALLOWED_LOCATIONS) {
			if (location == this.location) continue;
			checkboxes.add(content.addChild(
					Checkbox.builder(Component.literal(location.toString()), font)
							.maxWidth(200)
							.selected(selectedLocations.contains(location))
							.onValueChange((_, value) -> {
								if (value) {
									selectedLocations.add(location);
								} else {
									selectedLocations.remove(location);
								}

								((CheckboxAccessor) selectAll).setSelected(checkboxes.stream().allMatch(Checkbox::selected));
							})
							.build()
			));
		}
		content.arrangeElements();
		int maxHeight = Math.min(200, height - 150);
		this.scrollable = new ScrollableLayout(minecraft, content, maxHeight);
		scrollable.setMaxHeight(maxHeight);
		scrollable.setMinWidth(150);
		layout.addChild(scrollable);
		layout.addChild(SpacerElement.height(10));
		layout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> {
			apply();
			onClose();
		}).build());
		layout.addChild(Button.builder(CommonComponents.GUI_CANCEL, _ -> onClose()).build());
		layout.visitWidgets(this::addRenderableWidget);
		super.init();
	}

	private void apply() {
		JsonObject widgetConfig = new JsonObject();
		copiedWidget.widget.save(widgetConfig);
		for (Location loc : selectedLocations) {
			LayerConfig config = WidgetManager.getScreenConfig(loc).get(layer);
			if (copyPosition) {
				config.widgets().put(copiedWidget.widget.getInternalID(), new WidgetConfig(widgetConfig, copiedWidget.rule));
			} else {
				config.widgets().computeIfPresent(copiedWidget.widget.getInternalID(), (_, oldConfig) -> new WidgetConfig(Optional.of(widgetConfig), oldConfig.position()));
			}
		}
		selectedLocations.add(location);
		WidgetManager.getCopyTracker().get(layer).getOrCreate(copiedWidget.widget.getInternalID()).track(selectedLocations);
	}

	@Override
	protected void repositionElements() {
		super.repositionElements();
		layout.arrangeElements();
		layout.setPosition((width - layout.getWidth()) / 2, (height - layout.getHeight()) / 2);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractBackground(graphics, mouseX, mouseY, a);
		extractPopupBackground(graphics, layout.getX(), layout.getY(), layout.getWidth(), layout.getHeight());
		graphics.fill(scrollable.getX(), scrollable.getY(), scrollable.getX() + scrollable.getWidth(), scrollable.getY() + scrollable.getHeight(), ARGB.black(0.1f));
	}
}
