package de.hysky.skyblocker.skyblock.tabhud.config.preview;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.DungeonsTabPlaceholder;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.HudHelper;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.render.gui.NoopInput;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractScrollArea;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class PreviewTab implements Tab {
	public static final int RIGHT_SIDE_WIDTH = 120;

	final Minecraft client;
	private final PreviewWidget previewWidget;
	final WidgetsConfigurationScreen parent;
	private final WidgetOptionsScrollable widgetOptions;
	private final Mode mode;
	private final Button restorePositioning;
	private WidgetManager.ScreenLayer currentScreenLayer = WidgetManager.ScreenLayer.MAIN_TAB;
	private final Button[] layerButtons;
	private final StringWidget textWidget;
	final Objective placeHolderObjective;
	final DropdownWidget<Location> locationDropdown;

	public PreviewTab(Minecraft client, WidgetsConfigurationScreen parent, Mode mode) {
		this.client = client;
		this.parent = parent;
		this.mode = mode;
		this.textWidget = new StringWidget(
				Component.literal("This tab is specifically for dungeons, as it currently doesn't have hypixel's system"),
				client.font
		);

		previewWidget = new PreviewWidget(this);
		widgetOptions = new WidgetOptionsScrollable();
		widgetOptions.setWidth(RIGHT_SIDE_WIDTH - 10);

		WidgetManager.ScreenLayer[] values = WidgetManager.ScreenLayer.values();
		layerButtons = new Button[3];
		for (int i = 0; i < 3; i++) {
			WidgetManager.ScreenLayer screenLayer = values[i];
			layerButtons[i] = Button.builder(Component.literal(screenLayer.toString()), button -> {
						this.currentScreenLayer = screenLayer;
						for (Button layerButton : this.layerButtons) {
							layerButton.active = !layerButton.equals(button);
						}
					})
					.size(RIGHT_SIDE_WIDTH - 20, 15)
					.build();
			if (screenLayer == currentScreenLayer) layerButtons[i].active = false;
		}

		restorePositioning = Button.builder(Component.literal("Restore Positioning"), button -> {
					WidgetManager.getScreenBuilder(getCurrentLocation()).restorePositioningFromBackup();
					updateWidgets();
					onHudWidgetSelected(previewWidget.selectedWidget);
				})
				.width(100)
				.tooltip(Tooltip.create(Component.literal("Reset positions to before you opened this screen!")))
				.build();

		placeHolderObjective = new Objective(
				new Scoreboard(),
				"temp",
				ObjectiveCriteria.DUMMY,
				Component.literal("SKYBLOCK"),
				ObjectiveCriteria.RenderType.INTEGER,
				true,
				BlankFormat.INSTANCE
		);
		Scoreboard scoreboard = placeHolderObjective.getScoreboard();
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("Random text!")), placeHolderObjective).set(0);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("To fill in")), placeHolderObjective).set(-1);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("The place!")), placeHolderObjective).set(-2);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("...")), placeHolderObjective).set(-3);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("yea")), placeHolderObjective).set(-4);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("so how's your")), placeHolderObjective).set(-5);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("day? great that's")), placeHolderObjective).set(-6);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("nice to hear.")), placeHolderObjective).set(-7);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("this should be")), placeHolderObjective).set(-8);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("enough lines bye")), placeHolderObjective).set(-9);
		scoreboard.getOrCreatePlayerScore(createHolder(Component.literal("NEVER GONNA GIVE Y-")), placeHolderObjective).set(-10);

		locationDropdown = parent.createLocationDropdown(location -> updateWidgets());
		updateWidgets();
	}

	private ScoreHolder createHolder(Component name) {
		return new ScoreHolder() {
			@Override
			public String getScoreboardName() {
				return name.getString().replace(' ', '_');
			}

			@Override
			public @Nullable Component getDisplayName() {
				return name;
			}
		};
	}

	public void goToLayer(WidgetManager.ScreenLayer layer) {
		if (layer == WidgetManager.ScreenLayer.DEFAULT) layer = WidgetManager.ScreenLayer.HUD;
		layerButtons[layer.ordinal()].onPress(NoopInput.INSTANCE);
	}

	@Override
	public Component getTabTitle() {
		return Component.literal(mode == Mode.DUNGEON ? "Dungeons Editing" : "Preview");
	}

	@Override
	public void visitChildren(Consumer<AbstractWidget> consumer) {
		if (mode == Mode.EDITABLE_LOCATION) consumer.accept(locationDropdown);
		consumer.accept(previewWidget);
		for (Button layerButton : layerButtons) {
			consumer.accept(layerButton);
		}
		consumer.accept(widgetOptions);
		consumer.accept(restorePositioning);
		if (mode == Mode.DUNGEON) consumer.accept(textWidget);
	}

	@Override
	public void doLayout(ScreenRectangle tabArea) {
		float ratio = Math.min((tabArea.height() - 35) / (float) (parent.height), (tabArea.width() - RIGHT_SIDE_WIDTH - 5) / (float) (parent.width));

		previewWidget.setPosition(5, tabArea.top() + 5);
		previewWidget.setWidth((int) (parent.width * ratio));
		previewWidget.setHeight((int) (parent.height * ratio));
		previewWidget.ratio = ratio;
		updateWidgets();

		int startY = tabArea.top() + 10;
		if (mode == Mode.EDITABLE_LOCATION) {
			locationDropdown.setWidth(RIGHT_SIDE_WIDTH - 5);
			locationDropdown.setPosition(tabArea.right() - locationDropdown.getWidth() - 2, startY);
			locationDropdown.setMaxHeight(Math.min(180, tabArea.height() - 20));
			startY += DropdownWidget.ENTRY_HEIGHT + 4 + 10;
		}

		for (int i = 0; i < layerButtons.length; i++) {
			Button layerButton = layerButtons[i];
			layerButton.setPosition(tabArea.width() - layerButton.getWidth() - 10, startY + i * 15);
		}
		int optionsY = startY + layerButtons.length * 15 + 5;
		widgetOptions.setPosition(tabArea.width() - widgetOptions.getWidth() - 5, optionsY);
		widgetOptions.setHeight(tabArea.height() - optionsY - 5);
		textWidget.setPosition((tabArea.width() - textWidget.getWidth()) / 2, tabArea.bottom() - 9);
		restorePositioning.setPosition(10, tabArea.bottom() - 25);

		visitChildren(clickableWidget -> clickableWidget.visible = mode == Mode.DUNGEON || parent.isPreviewVisible() || parent.noHandler);
		locationDropdownOpened(locationDropdown.isOpen());
	}

	private void updatePlayerListFromPreview() {
		if (mode == Mode.DUNGEON) {
			PlayerListManager.updateDungeons(DungeonsTabPlaceholder.get());
			return;
		}
		if (!parent.isPreviewVisible() || parent.getHandler() == null) return;
		List<Component> lines = new ArrayList<>();

		// Preview doesn't include any players, so adding this as default
		lines.add(Component.literal("Players (6)"));
		lines.add(Component.literal("[PIG").withStyle(ChatFormatting.LIGHT_PURPLE)
				.append(Component.literal("+++").withStyle(ChatFormatting.AQUA))
				.append(Component.literal("] Technoblade").withStyle(ChatFormatting.LIGHT_PURPLE))
		);
		lines.add(Component.literal("Kevinthegreat1"));
		lines.add(Component.literal("AzureAaron"));
		lines.add(Component.literal("LifeIsAParadox"));
		lines.add(Component.literal("Rime"));
		lines.add(Component.literal("Vic is a Cat"));
		lines.add(Component.literal("that's right i "));
		lines.add(Component.literal("don't care about"));
		lines.add(Component.literal("spaces MWAHAHA"));
		lines.add(Component.literal("[MVP--] sixteencharacter"));

		for (int i = 3; i <= 5; i++) {
			ItemStack stack = parent.getHandler().getSlot(i).getItem();

			for (Component text : ItemUtils.getLore(stack)) {
				MutableComponent mutableText = Component.empty();
				AtomicBoolean foundSquare = new AtomicBoolean(false);

				text.visit((style, asString) -> {
					if (!asString.startsWith("⬛")) {
						mutableText.append(Component.literal(asString).withStyle(style));
					} else foundSquare.set(true);
					return Optional.empty();
				}, Style.EMPTY);

				if (foundSquare.get()) {
					lines.add(mutableText);
					//System.out.println(mutableText.getString());
					//System.out.println(mutableText);
				}
			}
		}
		PlayerListManager.updateWidgetsFrom(lines.stream().map(line -> {
			PlayerInfo playerListEntry = new PlayerInfo(new GameProfile(UUID.randomUUID(), ""), false);
			playerListEntry.setTabListDisplayName(line);
			return playerListEntry;
		}).toList());
	}

	public void updateWidgets() {
		ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(getCurrentLocation());
		updatePlayerListFromPreview();
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
		screenBuilder.updateWidgetLists(true);
		screenBuilder.updateWidgets(currentScreenLayer);
		screenBuilder.positionWidgets((int) (parent.width / scale), (int) (parent.height / scale));
	}

	public enum Mode {
		NORMAL,
		DUNGEON,
		EDITABLE_LOCATION
	}

	void onHudWidgetSelected(@Nullable HudWidget hudWidget) {
		widgetOptions.clearWidgets();
		if (hudWidget == null) return;
		if (locationDropdown.isOpen()) locationDropdown.mouseClicked(new MouseButtonEvent(locationDropdown.getX(), locationDropdown.getY(), new MouseButtonInfo(0, 0)), false);
		ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(getCurrentLocation());
		PositionRule positionRule = screenBuilder.getPositionRule(hudWidget.getInternalID());
		int width = widgetOptions.getWidth() - 6;

		// Normal hud widgets don't have auto.
		if (positionRule == null && !(hudWidget instanceof TabHudWidget)) {
			screenBuilder.setPositionRule(hudWidget.getInternalID(), PositionRule.DEFAULT);
			positionRule = PositionRule.DEFAULT;
		}

		// TODO localization

		widgetOptions.addWidget(new StringWidget(width, 9, hudWidget.getDisplayName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE), client.font));
		if (positionRule == null) {
			widgetOptions.addWidget(Button.builder(Component.literal("Positioning: Auto"), button -> {
						PositionRule rule = new PositionRule(
								"screen",
								PositionRule.Point.DEFAULT,
								PositionRule.Point.DEFAULT,
								hudWidget.getX() - 5,
								hudWidget.getY() - 5,
								WidgetManager.ScreenLayer.DEFAULT);
						screenBuilder.setPositionRule(hudWidget.getInternalID(), rule);
						updateWidgets();
						onHudWidgetSelected(hudWidget);
					})
					.width(width)
					.build());
		} else {
			// Normal hud widgets don't have auto.
			if (hudWidget instanceof TabHudWidget) {
				widgetOptions.addWidget(Button.builder(Component.literal("Positioning: Custom"), button -> {
							screenBuilder.setPositionRule(hudWidget.getInternalID(), null);
							updateWidgets();
							onHudWidgetSelected(hudWidget);
						})
						.width(width)
						.build());
			}

			String ye = "Layer: " + positionRule.screenLayer().toString();

			widgetOptions.addWidget(Button.builder(Component.literal(ye), button -> {
				ScreenBuilder builder = WidgetManager.getScreenBuilder(getCurrentLocation());
				PositionRule rule = builder.getPositionRuleOrDefault(hudWidget.getInternalID());
				WidgetManager.ScreenLayer newLayer = EnumUtils.cycle(rule.screenLayer());

				PositionRule newRule = new PositionRule(
						rule.parent(),
						rule.parentPoint(),
						rule.thisPoint(),
						rule.relativeX(),
						rule.relativeY(),
						newLayer
				);
				builder.setPositionRule(hudWidget.getInternalID(), newRule);
				button.setMessage(Component.literal("Layer: " + newRule.screenLayer().toString()));
				updateWidgets();
				if (newLayer != WidgetManager.ScreenLayer.DEFAULT) {
					layerButtons[newLayer.ordinal()].onPress(NoopInput.INSTANCE);
				}

			}).width(width).build());

			Component parentName;
			HudWidget parent;
			if (positionRule.parent().equals("screen")) {
				parentName = Component.literal("Screen");
			} else if ((parent = WidgetManager.widgetInstances.get(positionRule.parent())) == null) {
				parentName = Component.literal("Unloaded Widget");
			} else {
				parentName = parent.getDisplayName();
			}

			widgetOptions.addWidget(Button.builder(Component.literal("Parent: ").append(parentName), button -> {
				this.previewWidget.pickParent = true;
				button.setMessage(Component.literal("Click on a widget"));
			}).width(width).build());

			widgetOptions.addWidget(new AnchorSelectionWidget(width, Component.literal("This anchor"), false));
			widgetOptions.addWidget(new AnchorSelectionWidget(width, Component.literal("Parent anchor"), true));

			// apply to all locations
			if (mode == Mode.DUNGEON) return;
			// padding thing
			widgetOptions.addWidget(new AbstractWidget(0, 0, width, 20, Component.empty()) {
				@Override
				protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
				}

				@Override
				protected void updateWidgetNarration(NarrationElementOutput builder) {
				}
			});
		}

		widgetOptions.addWidget(Button.builder(Component.literal("Apply Everywhere"), button -> {
					if (this.previewWidget.selectedWidget == null) return;
					PositionRule toCopy = WidgetManager.getScreenBuilder(getCurrentLocation()).getPositionRule(this.previewWidget.selectedWidget.getInternalID());
					if (toCopy == null && !(this.previewWidget.selectedWidget instanceof TabHudWidget)) return;
					for (Location value : Location.values()) {
						if (value == getCurrentLocation() || value == Location.DUNGEON || value == Location.UNKNOWN) continue;
						WidgetManager.getScreenBuilder(value).setPositionRule(
								this.previewWidget.selectedWidget.getInternalID(),
								toCopy
						);
					}
					button.setMessage(Component.literal("Applied!"));
					Scheduler.INSTANCE.schedule(() -> button.setMessage(Component.literal("Apply Everywhere")), 15);
				}).width(width).tooltip(Tooltip.create(Component.literal("Apply positioning to all locations. This cannot be restored!"))).build()
		);
	}

	public Location getCurrentLocation() {
		return mode == Mode.DUNGEON ? Location.DUNGEON : parent.getCurrentLocation();
	}

	public WidgetManager.ScreenLayer getCurrentScreenLayer() {
		return currentScreenLayer;
	}

	/**
	 * Hide Layer Buttons when the location dropdown is opened.
	 */
	public void locationDropdownOpened(boolean isOpen) {
		onHudWidgetSelected(null);
		previewWidget.selectedWidget = null;
		for (Button layerButton : layerButtons) {
			layerButton.visible = !isOpen;
		}
	}

	private static class WidgetOptionsScrollable extends AbstractScrollArea {
		private final List<AbstractWidget> widgets = new ArrayList<>();
		private int height = 0;

		private WidgetOptionsScrollable() {
			super(0, 0, 0, 0, Component.literal("Widget Options Scrollable"));
		}

		@Override
		protected int contentHeight() {
			return height;
		}

		@Override
		protected double scrollRate() {
			return 6;
		}

		/**
		 * A widget is not visible if it is half above the top of the frame, or half below.
		 *
		 * @param i Y of the widget
		 * @param j Bottom of the widget
		 * @param h Height of the widget
		 */
		protected boolean isNotVisible(int i, int j, int h) {
			return !((double) j - ((double) h / 2) >= (double) this.getY()) || // Bottom of this widget is out of view, above the frame
					!((double) i + ((double) h / 2) <= (double) (this.getBottom())); // Top of this widget is out of view, below the frame
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			this.renderScrollbar(context, mouseX, mouseY);
			height = 0;
			for (AbstractWidget widget : widgets) {
				widget.setX(getX() + 1);
				widget.setY((int) (getY() + 1 + height - scrollAmount()));

				height += widget.getHeight() + 1;
				if (isNotVisible(widget.getY(), widget.getBottom(), widget.getHeight())) continue;
				widget.render(context, mouseX, mouseY, delta);
			}
		}

		@Override
		public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
			boolean bl = updateScrolling(click);
			for (AbstractWidget widget : widgets) {
				if (isNotVisible(widget.getY(), widget.getBottom(), widget.getHeight())) continue;
				if (widget.mouseClicked(click, doubled)) return true;
			}
			return super.mouseClicked(click, doubled) || bl;
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {
		}

		private void clearWidgets() {
			widgets.clear();
		}

		private void addWidget(AbstractWidget clickableWidget) {
			widgets.add(clickableWidget);
		}
	}

	private class AnchorSelectionWidget extends AbstractWidget {
		private final boolean other;
		private PositionRule.@Nullable Point hoveredPoint = null;

		private AnchorSelectionWidget(int width, Component text, boolean other) {
			super(0, 0, width, 40, text);
			this.other = other;
		}

		@Override
		protected void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
			hoveredPoint = null;
			context.drawString(client.font, getMessage(), getX(), getY(), CommonColors.WHITE, true);
			context.pose().pushMatrix();
			context.pose().translate(getX(), getY() + 10);
			// Rectangle thing
			int x = getWidth() / 6;
			int w = (int) (4 * getWidth() / 6f);
			int y = 5; // 30 / 6
			int h = 20;

			HudHelper.drawBorder(context, x, y + 1, w, h, CommonColors.WHITE);
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int squareX = x + (i * getWidth()) / 3;
					int squareY = y + (j * 30) / 3;
					boolean selectedAnchor = false;
					if (previewWidget.selectedWidget != null) {
						String internalID = previewWidget.selectedWidget.getInternalID();
						PositionRule positionRule = WidgetManager.getScreenBuilder(getCurrentLocation()).getPositionRule(internalID);
						if (positionRule != null) {
							PositionRule.Point point = other ? positionRule.parentPoint() : positionRule.thisPoint();
							selectedAnchor = point.horizontalPoint().ordinal() == i && point.verticalPoint().ordinal() == j;
						}
					}

					boolean hoveredAnchor = mouseX >= getX() + i * getWidth() / 3 &&
							mouseX < getX() + (i + 1) * getWidth() / 3 &&
							mouseY >= getY() + 10 + j * 10 &&
							mouseY < getY() + 10 + (j + 1) * 10;

					if (hoveredAnchor) {
						PositionRule.VerticalPoint[] verticalPoints = PositionRule.VerticalPoint.values();
						PositionRule.HorizontalPoint[] horizontalPoints = PositionRule.HorizontalPoint.values();
						hoveredPoint = new PositionRule.Point(verticalPoints[j], horizontalPoints[i]);
					}

					context.fill(squareX - 1, squareY - 1, squareX + 2, squareY + 2, hoveredAnchor ? CommonColors.RED : selectedAnchor ? CommonColors.YELLOW : CommonColors.WHITE);
				}
			}
			context.pose().popMatrix();
		}

		@Override
		public void onClick(MouseButtonEvent click, boolean doubled) {
			HudWidget affectedWidget = previewWidget.selectedWidget;
			if (hoveredPoint != null && affectedWidget != null) {
				ScreenBuilder screenBuilder = WidgetManager.getScreenBuilder(getCurrentLocation());
				String internalID = affectedWidget.getInternalID();
				PositionRule oldRule = screenBuilder.getPositionRuleOrDefault(internalID);
				// Get the x, y of the parent's point
				float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
				ScreenPosition startPos = WidgetPositioner.getStartPosition(oldRule.parent(), (int) (parent.width / scale), (int) (parent.height / scale), other ? hoveredPoint : oldRule.parentPoint());
				if (startPos == null) startPos = new ScreenPosition(0, 0);
				// Same but for the affected widget
				PositionRule.Point thisPoint = other ? oldRule.thisPoint() : hoveredPoint;
				ScreenPosition endPos = new ScreenPosition(
						(int) (affectedWidget.getX() + thisPoint.horizontalPoint().getPercentage() * affectedWidget.getWidth()),
						(int) (affectedWidget.getY() + thisPoint.verticalPoint().getPercentage() * affectedWidget.getHeight())
				);

				if (other) {
					screenBuilder.setPositionRule(internalID, new PositionRule(
							oldRule.parent(),
							hoveredPoint,
							oldRule.thisPoint(),
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y(),
							oldRule.screenLayer()));
				} else {
					screenBuilder.setPositionRule(internalID, new PositionRule(
							oldRule.parent(),
							oldRule.parentPoint(),
							hoveredPoint,
							endPos.x() - startPos.x(),
							endPos.y() - startPos.y(),
							oldRule.screenLayer()));
				}
			}
			updateWidgets();
		}

		@Override
		protected void updateWidgetNarration(NarrationElementOutput builder) {
		}
	}

	@Override
	public Component getTabExtraNarration() {
		return Component.empty();
	}
}
