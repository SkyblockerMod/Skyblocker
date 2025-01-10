package de.hysky.skyblocker.skyblock.tabhud.config.preview;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.DungeonsTabPlaceholder;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenBuilder;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.PositionRule;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.pipeline.WidgetPositioner;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.TabHudWidget;
import de.hysky.skyblocker.utils.EnumUtils;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.render.gui.DropdownWidget;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.ScreenPos;
import net.minecraft.client.gui.ScreenRect;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.Tab;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.number.BlankNumberFormat;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class PreviewTab implements Tab {
	public static final int RIGHT_SIDE_WIDTH = 120;

	final MinecraftClient client;
	private final PreviewWidget previewWidget;
	final WidgetsConfigurationScreen parent;
	private final WidgetOptionsScrollable widgetOptions;
	private final Mode mode;
	private final ButtonWidget restorePositioning;
	private ScreenMaster.ScreenLayer currentScreenLayer = ScreenMaster.ScreenLayer.MAIN_TAB;
	private final ButtonWidget[] layerButtons;
	private final TextWidget textWidget;
	final ScoreboardObjective placeHolderObjective;
	final DropdownWidget<Location> locationDropdown;

	public PreviewTab(MinecraftClient client, WidgetsConfigurationScreen parent, Mode mode) {
		this.client = client;
		this.parent = parent;
		this.mode = mode;
		this.textWidget = new TextWidget(
				Text.literal("This tab is specifically for dungeons, as it currently doesn't have hypixel's system"),
				client.textRenderer
		);

		previewWidget = new PreviewWidget(this);
		widgetOptions = new WidgetOptionsScrollable();
		widgetOptions.setWidth(RIGHT_SIDE_WIDTH - 10);

		ScreenMaster.ScreenLayer[] values = ScreenMaster.ScreenLayer.values();
		layerButtons = new ButtonWidget[3];
		for (int i = 0; i < 3; i++) {
			ScreenMaster.ScreenLayer screenLayer = values[i];
			layerButtons[i] = ButtonWidget.builder(Text.literal(screenLayer.toString()), button -> {
						this.currentScreenLayer = screenLayer;
						for (ButtonWidget layerButton : this.layerButtons) {
							layerButton.active = !layerButton.equals(button);
						}
					})
					.size(RIGHT_SIDE_WIDTH - 20, 15)
					.build();
			if (screenLayer == currentScreenLayer) layerButtons[i].active = false;
		}

		restorePositioning = ButtonWidget.builder(Text.literal("Restore Positioning"), button -> {
					ScreenMaster.getScreenBuilder(getCurrentLocation()).restorePositioningFromBackup();
					updateWidgets();
					onHudWidgetSelected(previewWidget.selectedWidget);
				})
				.width(100)
				.tooltip(Tooltip.of(Text.literal("Reset positions to before you opened this screen!")))
				.build();

		placeHolderObjective = new ScoreboardObjective(
				new Scoreboard(),
				"temp",
				ScoreboardCriterion.DUMMY,
				Text.literal("SKYBLOCK"),
				ScoreboardCriterion.RenderType.INTEGER,
				true,
				BlankNumberFormat.INSTANCE
		);
		Scoreboard scoreboard = placeHolderObjective.getScoreboard();
		scoreboard.getOrCreateScore(createHolder(Text.literal("Random text!")), placeHolderObjective).setScore(0);
		scoreboard.getOrCreateScore(createHolder(Text.literal("To fill in")), placeHolderObjective).setScore(-1);
		scoreboard.getOrCreateScore(createHolder(Text.literal("The place!")), placeHolderObjective).setScore(-2);
		scoreboard.getOrCreateScore(createHolder(Text.literal("...")), placeHolderObjective).setScore(-3);
		scoreboard.getOrCreateScore(createHolder(Text.literal("yea")), placeHolderObjective).setScore(-4);
		scoreboard.getOrCreateScore(createHolder(Text.literal("so how's your")), placeHolderObjective).setScore(-5);
		scoreboard.getOrCreateScore(createHolder(Text.literal("day? great that's")), placeHolderObjective).setScore(-6);
		scoreboard.getOrCreateScore(createHolder(Text.literal("nice to hear.")), placeHolderObjective).setScore(-7);
		scoreboard.getOrCreateScore(createHolder(Text.literal("this should be")), placeHolderObjective).setScore(-8);
		scoreboard.getOrCreateScore(createHolder(Text.literal("enough lines bye")), placeHolderObjective).setScore(-9);
		scoreboard.getOrCreateScore(createHolder(Text.literal("NEVER GONNA GIVE Y-")), placeHolderObjective).setScore(-10);

		ScreenMaster.getScreenBuilder(getCurrentLocation()).positionWidgets(client.getWindow().getScaledWidth(), client.getWindow().getScaledHeight(), true);
		locationDropdown = parent.createLocationDropdown(location -> updateWidgets());
	}

	private ScoreHolder createHolder(Text name) {
		return new ScoreHolder() {
			@Override
			public String getNameForScoreboard() {
				return name.getString().replace(' ', '_');
			}

			@Nullable
			@Override
			public Text getDisplayName() {
				return name;
			}
		};
	}

	public void goToLayer(ScreenMaster.ScreenLayer layer) {
		if (layer == ScreenMaster.ScreenLayer.DEFAULT) layer = ScreenMaster.ScreenLayer.HUD;
		layerButtons[layer.ordinal()].onPress();
	}

	@Override
	public Text getTitle() {
		return Text.literal(mode == Mode.DUNGEON ? "Dungeons Editing" : "Preview");
	}

	@Override
	public void forEachChild(Consumer<ClickableWidget> consumer) {
		if (mode == Mode.EDITABLE_LOCATION) consumer.accept(locationDropdown);
		consumer.accept(previewWidget);
		for (ButtonWidget layerButton : layerButtons) {
			consumer.accept(layerButton);
		}
		consumer.accept(widgetOptions);
		consumer.accept(restorePositioning);
		if (mode == Mode.DUNGEON) consumer.accept(textWidget);
	}

	@Override
	public void refreshGrid(ScreenRect tabArea) {
		float ratio = Math.min((tabArea.height() - 35) / (float) (parent.height), (tabArea.width() - RIGHT_SIDE_WIDTH - 5) / (float) (parent.width));

		previewWidget.setPosition(5, tabArea.getTop() + 5);
		previewWidget.setWidth((int) (parent.width * ratio));
		previewWidget.setHeight((int) (parent.height * ratio));
		previewWidget.ratio = ratio;
		updateWidgets();

		int startY = tabArea.getTop() + 10;
		if (mode == Mode.EDITABLE_LOCATION) {
			locationDropdown.setWidth(RIGHT_SIDE_WIDTH - 5);
			locationDropdown.setPosition(tabArea.getRight() - locationDropdown.getWidth() - 2, startY);
			locationDropdown.setMaxHeight(Math.min(180, tabArea.height() - 20));
			startY += DropdownWidget.ENTRY_HEIGHT + 4 + 10;
		}

		for (int i = 0; i < layerButtons.length; i++) {
			ButtonWidget layerButton = layerButtons[i];
			layerButton.setPosition(tabArea.width() - layerButton.getWidth() - 10, startY + i * 15);
		}
		int optionsY = startY + layerButtons.length * 15 + 5;
		widgetOptions.setPosition(tabArea.width() - widgetOptions.getWidth() - 5, optionsY);
		widgetOptions.setHeight(tabArea.height() - optionsY - 5);
		textWidget.setWidth(tabArea.width());
		textWidget.setPosition(0, tabArea.getBottom() - 9);
		restorePositioning.setPosition(10, tabArea.getBottom() - 25);

		forEachChild(clickableWidget -> clickableWidget.visible = parent.isPreviewVisible() || parent.noHandler);
	}

	private void updatePlayerListFromPreview() {
		if (mode == Mode.DUNGEON) {
			PlayerListManager.updateDungeons(DungeonsTabPlaceholder.get());
			return;
		}
		if (!parent.isPreviewVisible() || parent.getHandler() == null) return;
		List<Text> lines = new ArrayList<>();

		// Preview doesn't include any players, so adding this as default
		lines.add(Text.literal("Players (6)"));
		lines.add(Text.literal("[PIG").formatted(Formatting.LIGHT_PURPLE)
				.append(Text.literal("+++").formatted(Formatting.AQUA))
				.append(Text.literal("] Technoblade").formatted(Formatting.LIGHT_PURPLE))
		);
		lines.add(Text.literal("Kevinthegreat1"));
		lines.add(Text.literal("AzureAaron"));
		lines.add(Text.literal("LifeIsAParadox"));
		lines.add(Text.literal("Rime"));
		lines.add(Text.literal("Vic is a Cat"));
		lines.add(Text.literal("that's right i "));
		lines.add(Text.literal("don't care about"));
		lines.add(Text.literal("spaces MWAHAHA"));
		lines.add(Text.literal("[MVP--] sixteencharacter"));

		for (int i = 3; i <= 5; i++) {
			ItemStack stack = parent.getHandler().getSlot(i).getStack();

			for (Text text : ItemUtils.getLore(stack)) {
				MutableText mutableText = Text.empty();
				AtomicBoolean foundSquare = new AtomicBoolean(false);

				text.visit((style, asString) -> {
					if (!asString.startsWith("⬛")) {
						mutableText.append(Text.literal(asString).fillStyle(style));
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
			PlayerListEntry playerListEntry = new PlayerListEntry(new GameProfile(UUID.randomUUID(), ""), false);
			playerListEntry.setDisplayName(line);
			return playerListEntry;
		}).toList());
	}

	void updateWidgets() {
		ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
		updatePlayerListFromPreview();
		float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
		screenBuilder.positionWidgets((int) (parent.width / scale), (int) (parent.height / scale), true);
	}

	public enum Mode {
		NORMAL,
		DUNGEON,
		EDITABLE_LOCATION
	}

	void onHudWidgetSelected(@Nullable HudWidget hudWidget) {
		widgetOptions.clearWidgets();
		if (hudWidget == null) return;
		ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
		PositionRule positionRule = screenBuilder.getPositionRule(hudWidget.getInternalID());
		int width = widgetOptions.getWidth() - 6;

		// Normal hud widgets don't have auto.
		if (positionRule == null && !(hudWidget instanceof TabHudWidget)) {
			screenBuilder.setPositionRule(hudWidget.getInternalID(), PositionRule.DEFAULT);
			positionRule = PositionRule.DEFAULT;
		}

		// TODO localization

		widgetOptions.addWidget(new TextWidget(width, 9, hudWidget.getDisplayName().copy().formatted(Formatting.BOLD, Formatting.UNDERLINE), client.textRenderer));
		if (positionRule == null) {
			widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Auto"), button -> {
						PositionRule rule = new PositionRule(
								"screen",
								PositionRule.Point.DEFAULT,
								PositionRule.Point.DEFAULT,
								hudWidget.getX() - 5,
								hudWidget.getY() - 5,
								ScreenMaster.ScreenLayer.DEFAULT);
						screenBuilder.setPositionRule(hudWidget.getInternalID(), rule);
						updateWidgets();
						onHudWidgetSelected(hudWidget);
					})
					.width(width)
					.build());
		} else {
			// Normal hud widgets don't have auto.
			if (hudWidget instanceof TabHudWidget) {
				widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Positioning: Custom"), button -> {
							screenBuilder.setPositionRule(hudWidget.getInternalID(), null);
							updateWidgets();
							onHudWidgetSelected(hudWidget);
						})
						.width(width)
						.build());
			}

			String ye = "Layer: " + positionRule.screenLayer().toString();

			widgetOptions.addWidget(ButtonWidget.builder(Text.literal(ye), button -> {
				ScreenBuilder builder = ScreenMaster.getScreenBuilder(getCurrentLocation());
				PositionRule rule = builder.getPositionRuleOrDefault(hudWidget.getInternalID());
				ScreenMaster.ScreenLayer newLayer = EnumUtils.cycle(rule.screenLayer());

				PositionRule newRule = new PositionRule(
						rule.parent(),
						rule.parentPoint(),
						rule.thisPoint(),
						rule.relativeX(),
						rule.relativeY(),
						newLayer
				);
				builder.setPositionRule(hudWidget.getInternalID(), newRule);
				button.setMessage(Text.literal("Layer: " + newRule.screenLayer().toString()));
				updateWidgets();
				if (newLayer != ScreenMaster.ScreenLayer.DEFAULT) {
					layerButtons[newLayer.ordinal()].onPress();
				}

			}).width(width).build());

			Text parentName;
			HudWidget parent;
			if (positionRule.parent().equals("screen")) {
				parentName = Text.literal("Screen");
			} else if ((parent = ScreenMaster.widgetInstances.get(positionRule.parent())) == null) {
				parentName = Text.literal("Unloaded Widget");
			} else {
				parentName = parent.getDisplayName();
			}

			widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Parent: ").append(parentName), button -> {
				this.previewWidget.pickParent = true;
				button.setMessage(Text.literal("Click on a widget"));
			}).width(width).build());

			widgetOptions.addWidget(new AnchorSelectionWidget(width, Text.literal("This anchor"), false));
			widgetOptions.addWidget(new AnchorSelectionWidget(width, Text.literal("Parent anchor"), true));

			// apply to all locations
			if (mode == Mode.DUNGEON) return;
			// padding thing
			widgetOptions.addWidget(new ClickableWidget(0, 0, width, 20, Text.empty()) {
				@Override
				protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
				}

				@Override
				protected void appendClickableNarrations(NarrationMessageBuilder builder) {
				}
			});
		}

		widgetOptions.addWidget(ButtonWidget.builder(Text.literal("Apply Everywhere"), button -> {
					if (this.previewWidget.selectedWidget == null) return;
					PositionRule toCopy = ScreenMaster.getScreenBuilder(getCurrentLocation()).getPositionRule(this.previewWidget.selectedWidget.getInternalID());
					if (toCopy == null && !(this.previewWidget.selectedWidget instanceof TabHudWidget)) return;
					for (Location value : Location.values()) {
						if (value == getCurrentLocation() || value == Location.DUNGEON || value == Location.UNKNOWN) continue;
						ScreenMaster.getScreenBuilder(value).setPositionRule(
								this.previewWidget.selectedWidget.getInternalID(),
								toCopy
						);
					}
					button.setMessage(Text.literal("Applied!"));
					Scheduler.INSTANCE.schedule(() -> button.setMessage(Text.literal("Apply Everywhere")), 15);
				}).width(width).tooltip(Tooltip.of(Text.literal("Apply positioning to all locations. This cannot be restored!"))).build()
		);
	}

	public Location getCurrentLocation() {
		return mode == Mode.DUNGEON ? Location.DUNGEON : parent.getCurrentLocation();
	}

	public ScreenMaster.ScreenLayer getCurrentScreenLayer() {
		return currentScreenLayer;
	}

	private static class WidgetOptionsScrollable extends ScrollableWidget {

		private final List<ClickableWidget> widgets = new ArrayList<>();
		private int height = 0;

		public WidgetOptionsScrollable() {
			super(0, 0, 0, 0, Text.literal("Widget Options Scrollable"));
		}

		@Override
		protected int getContentsHeightWithPadding() {
			return height;
		}

		@Override
		protected double getDeltaYPerScroll() {
			return 6;
		}

		protected boolean isNotVisible(int i, int j) {
			return !((double) j - this.getScrollY() >= (double) this.getY()) || !((double) i - this.getScrollY() <= (double) (this.getY() + this.height));
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			height = 0;
			for (ClickableWidget widget : widgets) {
				widget.setX(getX() + 1);
				widget.setY(getY() + 1 + height);

				height += widget.getHeight() + 1;
				if (isNotVisible(widget.getY(), widget.getBottom())) continue;
				widget.render(context, mouseX, mouseY + (int) getScrollY(), delta);

			}
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			for (ClickableWidget widget : widgets) {
				if (isNotVisible(widget.getY(), widget.getBottom())) continue;
				if (widget.mouseClicked(mouseX, mouseY + getScrollY(), button)) return true;
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		@Override
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}

		void clearWidgets() {
			widgets.clear();
		}

		void addWidget(ClickableWidget clickableWidget) {
			widgets.add(clickableWidget);
		}
	}

	private class AnchorSelectionWidget extends ClickableWidget {
		private final boolean other;
		private @Nullable PositionRule.Point hoveredPoint = null;

		public AnchorSelectionWidget(int width, Text text, boolean other) {
			super(0, 0, width, 40, text);
			this.other = other;
		}

		@Override
		protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
			hoveredPoint = null;
			context.drawText(client.textRenderer, getMessage(), getX(), getY(), Colors.WHITE, true);
			context.getMatrices().push();
			context.getMatrices().translate(getX(), getY() + 10, 0.f);
			// Rectangle thing
			int x = getWidth() / 6;
			int w = (int) (4 * getWidth() / 6f);
			int y = 5; // 30 / 6
			int h = 20;

			context.drawBorder(x, y + 1, w, h, Colors.WHITE);
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					int squareX = x + (i * getWidth()) / 3;
					int squareY = y + (j * 30) / 3;
					boolean selectedAnchor = false;
					if (previewWidget.selectedWidget != null) {
						String internalID = previewWidget.selectedWidget.getInternalID();
						PositionRule positionRule = ScreenMaster.getScreenBuilder(getCurrentLocation()).getPositionRule(internalID);
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

					context.fill(squareX - 1, squareY - 1, squareX + 2, squareY + 2, hoveredAnchor ? Colors.RED : selectedAnchor ? Colors.YELLOW : Colors.WHITE);
				}
			}
			context.getMatrices().pop();
		}

		@Override
		public void onClick(double mouseX, double mouseY) {
			HudWidget affectedWidget = previewWidget.selectedWidget;
			if (hoveredPoint != null && affectedWidget != null) {
				ScreenBuilder screenBuilder = ScreenMaster.getScreenBuilder(getCurrentLocation());
				String internalID = affectedWidget.getInternalID();
				PositionRule oldRule = screenBuilder.getPositionRuleOrDefault(internalID);
				// Get the x, y of the parent's point
				float scale = SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudScale / 100.f;
				ScreenPos startPos = WidgetPositioner.getStartPosition(oldRule.parent(), (int) (parent.width / scale), (int) (parent.height / scale), other ? hoveredPoint : oldRule.parentPoint());
				if (startPos == null) startPos = new ScreenPos(0, 0);
				// Same but for the affected widget
				PositionRule.Point thisPoint = other ? oldRule.thisPoint() : hoveredPoint;
				ScreenPos endPos = new ScreenPos(
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
		protected void appendClickableNarrations(NarrationMessageBuilder builder) {
		}
	}
}
