package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.tabhud.screenbuilder.ScreenConfig;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.AbstractPopupScreen;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.function.Supplier;

class GlobalOptionsScreen extends Screen {
	private final Supplier<UIAndVisualsConfig.TabHudConf> CONFIG = () -> SkyblockerConfigManager.get().uiAndVisuals.tabHud;
	private final Tooltip STYLE_TOOLTIP = Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[0]").append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[1]")).append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[2]")).append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[3]")));
	private final Component YES = CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN);
	private final Component NO = CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED);
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final WidgetsConfigurationScreen parent;

	GlobalOptionsScreen(WidgetsConfigurationScreen parent) {
		super(Component.translatable("skyblocker.config.hud.globalOptionsScreen.title"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.addToHeader(new StringWidget(title, font));
		LinearLayout body = layout.addToContents(LinearLayout.vertical().spacing(5));
		body.addChild(new StringWidget(Component.literal("Global Options"), font));
		GridLayout.RowHelper globalOptions = body.addChild(new GridLayout().spacing(2)).createRowHelper(2);
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, _ -> onClose()).build());

		UIAndVisualsConfig.TabHudConf conf = CONFIG.get();
		globalOptions.addChild(CycleButton.builder(style -> Component.translatable(style.toString()), conf.style)
						.withValues(UIAndVisualsConfig.TabHudStyle.values())
						.withTooltip(_ -> STYLE_TOOLTIP)
						.create(
								0,
								0,
								Button.DEFAULT_WIDTH * 2 + 2,
								Button.DEFAULT_HEIGHT,
								Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style"),
								(_, value) -> conf.style = value),
				2);
		globalOptions.addChild(RangedSliderWidget.builder()
				.optionFormatter(Component.translatable("skyblocker.config.hud.globalScale"), d -> Component.literal(Formatters.INTEGER_NUMBERS.format(d) + '%'))
				.defaultValue(conf.tabHudScale)
				.step(1)
				.minMax(10, 200)
				.callback(d -> conf.tabHudScale = (int) Math.round(d))
				.build());
		// TODO turn these two into per widget things maybe?
		globalOptions.addChild(CycleButton.booleanBuilder(YES, NO, conf.displayIcons)
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.displayIcons"), (_, value) -> conf.displayIcons = value)
		);
		globalOptions.addChild(CycleButton.booleanBuilder(YES, NO, conf.compactWidgets)
				.withTooltip(_ -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets"), (_, value) -> conf.compactWidgets = value)
		);
		globalOptions.addChild(CycleButton.booleanBuilder(YES, NO, conf.enableFancyWidgetsList)
				.withTooltip(_ -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList"), (_, value) -> conf.enableFancyWidgetsList = value)
		);

		body.addChild(new StringWidget(Component.literal(parent.getCurrentLocation() + "'s options"), font));
		GridLayout.RowHelper screenOptions = body.addChild(new GridLayout().spacing(2)).createRowHelper(2);
		screenOptions.addChild(Button.builder(Component.literal("Edit visible Fancy TAB widgets"), _ -> minecraft.setScreen(new HiddenWidgetsPopup(this, parent.getScreenConfig()))).build(), 2).active = SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableFancyTab;

		layout.visitWidgets(this::addRenderableWidget);
		repositionElements();
	}

	@Override
	protected void repositionElements() {
		layout.arrangeElements();
	}


	@Override
	public void onClose() {
		minecraft.setScreen(parent);
	}

	@Override
	public void removed() {
		super.removed();
		SkyblockerConfigManager.update(_ -> {}); // FIXME
	}

	private static class HiddenWidgetsPopup extends AbstractPopupScreen {
		private final ScreenConfig screenConfig;
		private final LinearLayout layout = LinearLayout.vertical().spacing(10);

		private HiddenWidgetsPopup(Screen backgroundScreen, ScreenConfig screenConfig) {
			super(Component.literal("Edit hidden widgets"), backgroundScreen);
			this.screenConfig = screenConfig;
		}

		@Override
		protected void init() {
			LinearLayout content = LinearLayout.vertical().spacing(2);

			for (String widgetId : PlayerListManager.getCurrentWidgets()) {
				HudWidget widget = PlayerListManager.getTabWidget(widgetId);
				if (widget == null) continue;
				content.addChild(
						Checkbox.builder(widget.getInformation().displayName(), font)
								.maxWidth(200)
								.selected(screenConfig.hiddenTabWidgets.contains(widget.getInternalID()))
								.onValueChange((_, value) -> {
									if (value) {
										screenConfig.hiddenTabWidgets.add(widget.getInternalID());
									} else {
										screenConfig.hiddenTabWidgets.remove(widget.getInternalID());
									}
								})
								.build()
				);
			}
			content.arrangeElements();
			int maxHeight = Math.min(200, height - 50);
			ScrollableLayout scrollable = new ScrollableLayout(minecraft, content, maxHeight);
			scrollable.setMaxHeight(maxHeight);
			scrollable.setMinWidth(150);
			layout.addChild(scrollable);
			layout.addChild(Button.builder(CommonComponents.GUI_DONE, _ -> onClose()).build());
			layout.visitWidgets(this::addRenderableWidget);
			super.init();
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
		}
	}
}
