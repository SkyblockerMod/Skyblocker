package de.hysky.skyblocker.skyblock.tabhud.config;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.PositionedWidget;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.WidgetManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractContainerWidget;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.layouts.SpacerElement;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

class SidePanelWidget extends AbstractContainerWidget {
	private static final Identifier TEXTURE = SkyblockerMod.id("menu_outer_space");
	private static final int TOP_MARGIN = 16; // this is kinda horribly used but this widget will only be used here so it is whatever.
	private static final int SCROLLBAR_AREA = SCROLLBAR_WIDTH + 1; // 1 for padding

	private final Minecraft client = Minecraft.getInstance();
	private final List<AbstractWidget> optionWidgets = new ArrayList<>();

	private LinearLayout layout = LinearLayout.vertical();
	boolean rightSide = false;
	private int targetX = 0;
	private float animation = 0.0f;
	private int animationStart = 0;
	private int animationEnd = 0;
	private boolean isOpen = false;

	private final WidgetsConfigurationScreen configScreen;

	private @Nullable PositionedWidget positionedWidget;

	SidePanelWidget(int width, int height, WidgetsConfigurationScreen configScreen) {
		super(0, TOP_MARGIN, width, height - TOP_MARGIN, Component.literal("Side Panel"), defaultSettings(5));
		visible = false;
		this.configScreen = configScreen;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return optionWidgets;
	}

	@Override
	protected int contentHeight() {
		return layout.getHeight();
	}

	private boolean isNotVisible(int top, int bottom) {
		return !(bottom - this.scrollAmount() >= this.getY()) || !(top - this.scrollAmount() <= this.getY() + this.getHeight());
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float deltaTicks) {
		if (animation >= 0) {
			if (animation < 1.0f) {
				setX(animationStart + (int) ((animationEnd - animationStart) * animation));
				animation += Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks() * 50 * 7.5f / 1000.f;
			} else {
				setX(animationEnd);
				animation = -1f;
			}
		}
		context.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, getX() - 4, getY() - 4 - TOP_MARGIN, getWidth() + 8, getHeight() + 8 + TOP_MARGIN);
		context.enableScissor(this.getX(), this.getY(), this.getRight(), this.getBottom());

		for (AbstractWidget clickableWidget : this.optionWidgets) {
			if (isNotVisible(clickableWidget.getY(), clickableWidget.getBottom())) continue;
			clickableWidget.extractRenderState(context, mouseX, mouseY, deltaTicks);
		}
		context.disableScissor();
		this.extractScrollbar(context, mouseX, mouseY);
	}

	public void open() {
		if (isOpen()) return;
		visible = true;
		animation = 0.0f;
		animationEnd = targetX;
		animationStart = targetX + (rightSide ? (getWidth() + 8) : (-getWidth() - 8));
		isOpen = true;
	}

	public void open(PositionedWidget positionedWidget, boolean rightSide) {
		this.positionedWidget = positionedWidget;
		HudWidget hudWidget = positionedWidget.widget;
		layout = LinearLayout.vertical().spacing(5);
		layout.defaultCellSetting().alignHorizontallyCenter();
		optionWidgets.clear();
		add(new StringWidget(0, 15, hudWidget.getInformation().displayName().copy().withStyle(ChatFormatting.UNDERLINE), client.font) {
			@Override
			public void setWidth(int width) {
				setMaxWidth(width, TextOverflow.SCROLLING);
			}
		});
		if (!positionedWidget.fromTab) {
			add(Button.builder(Component.translatable("skyblocker.config.hud.widget.remove"), _ -> configScreen.removeWidget(positionedWidget)).build());
		}

		add(Button.builder(Component.literal("Apply Everywhere"), _ -> Arrays.stream(Location.values())
				.filter(loc -> loc != Location.UNKNOWN)
				.map(WidgetManager::getScreenConfig)
				.flatMap(ScreenConfig::allLayers)
				.forEach(layerConfig -> {
					JsonObject configJson = new JsonObject();
					hudWidget.save(configJson);
					layerConfig.widgets.computeIfPresent(hudWidget.getInternalID(), (_, config) -> new WidgetConfig(Optional.of(configJson), config.position()));
				})).tooltip(Tooltip.create(Component.literal("Applies the options of this widget to all other instances everywhere"))).build());

		add(Button.builder(Component.literal("Copy Everywhere"), _ -> Arrays.stream(Location.values())
				.filter(loc -> loc != Location.UNKNOWN)
				.filter(loc -> hudWidget.getInformation().available().test(loc))
				.map(WidgetManager::getScreenConfig)
				.map(c -> c.get(configScreen.getCurrentScreenLayer()))
				.forEach(layerConfig -> {
					JsonObject configJson = new JsonObject();
					hudWidget.save(configJson);
					layerConfig.widgets.put(hudWidget.getInternalID(), new WidgetConfig(configJson, positionedWidget.rule));
				})).tooltip(Tooltip.create(Component.literal("Copies this widget to all locations on the same layer"))).build());

		layout.addChild(SpacerElement.height(10));

		int availableWidth = getWidth() - SCROLLBAR_AREA;

		if (!positionedWidget.fromTab) {
			add(new PositionRuleWidget(configScreen, positionedWidget));
			layout.addChild(SpacerElement.height(10));
		}

		List<AbstractWidget> collector = new ArrayList<>();
		hudWidget.getOptionWidgets(new OptionWidgetCollector(collector));
		collector.forEach(this::add);

		layout.addChild(SpacerElement.height(10));

		// Position everything
		for (AbstractWidget widget : optionWidgets) {
			widget.setWidth(availableWidth);
		}

		layout.setPosition(getX(), getY() - (int) scrollAmount());
		layout.arrangeElements();
		int openX = rightSide ? configScreen.width - getWidth() : 0;
		if (isOpen() && (openX != targetX || rightSide != this.rightSide)) {
			isOpen = false;
		}
		this.rightSide = rightSide;
		targetX = openX;
		open();
	}

	public void close() {
		if (!isOpen()) return;
		animationStart = getX();
		animationEnd = getX() + (rightSide ? (getWidth() + 8) : (-getWidth() - 8));
		animation = 0;
		isOpen = false;
	}

	public boolean isOpen() {
		return isOpen;
	}

	@Override
	public void setScrollAmount(double scrollY) {
		super.setScrollAmount(scrollY);
		layout.setY(getY() - (int) scrollAmount());
	}

	@Override
	public void setX(int x) {
		super.setX(x);
		if (rightSide) {
			layout.setX(x);
		} else {
			layout.setX(x + SCROLLBAR_AREA);
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) return true;
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void setWidth(int width) {
		super.setWidth(width);
		for (AbstractWidget widget : optionWidgets) {
			widget.setWidth(getWidth() - SCROLLBAR_AREA); // remove 6 for scrollbar and one for a liiiitle padding
		}
		layout.arrangeElements();
	}

	@Override
	public void setHeight(int height) {
		super.setHeight(height - TOP_MARGIN);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	public @Nullable PositionedWidget getPositionedWidget() {
		return positionedWidget;
	}

	private <T extends AbstractWidget> T add(T widget) {
		optionWidgets.add(widget);
		return layout.addChild(widget);
	}


	@Override
	protected int scrollBarX() {
		return rightSide ? super.scrollBarX() : 0;
	}
}
