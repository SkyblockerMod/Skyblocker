package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

// TODO translatable
class GlobalOptionsScreen extends Screen {
	private final Supplier<UIAndVisualsConfig.HudConf> CONFIG = () -> SkyblockerConfigManager.get().uiAndVisuals.hud;
	private final Tooltip STYLE_TOOLTIP = Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[0]").append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[1]")).append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[2]")).append("\n")
			.append(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[3]")));
	private final Component YES = CommonComponents.GUI_YES.copy().withStyle(ChatFormatting.GREEN);
	private final Component NO = CommonComponents.GUI_NO.copy().withStyle(ChatFormatting.RED);
	private final HeaderAndFooterLayout layout = new HeaderAndFooterLayout(this);
	private final Screen parent;

	GlobalOptionsScreen(Screen parent) {
		super(Component.literal("HUD and TAB options"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.addToHeader(new StringWidget(title, font));
		GridLayout.RowHelper body = layout.addToContents(new GridLayout().spacing(2)).createRowHelper(2);
		layout.addToFooter(Button.builder(CommonComponents.GUI_DONE, b -> onClose()).build());

		UIAndVisualsConfig.HudConf conf = CONFIG.get();
		body.addChild(CycleButton.builder(style -> Component.translatable(style.toString()), conf.style)
				.withValues(UIAndVisualsConfig.HudStyle.values())
				.withTooltip(ignored -> STYLE_TOOLTIP)
				.create(
						0,
						0,
						Button.DEFAULT_WIDTH * 2 + 2,
						Button.DEFAULT_HEIGHT,
						Component.translatable("skyblocker.config.uiAndVisuals.tabHud.style"),
						(button, value) -> conf.style = value),
				2);
		body.addChild(RangedSliderWidget.builder()
						.optionFormatter(Component.literal("Global Scale"), Formatters.INTEGER_NUMBERS) // FIXME add % when chat rules thing is merged
						.defaultValue(conf.hudScale)
						.step(1)
						.minMax(10, 200)
						.callback(d -> conf.hudScale = (int) Math.round(d))
				.build());
		// TODO turn these two into per widget things maybe?
		body.addChild(CycleButton.booleanBuilder(YES, NO, conf.displayIcons)
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.displayIcons"), (button, value) -> conf.displayIcons = value)
		);
		body.addChild(CycleButton.booleanBuilder(YES, NO, conf.compactWidgets)
				.withTooltip(ignored -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets"), (button, value) -> conf.compactWidgets = value)
		);
		body.addChild(CycleButton.booleanBuilder(YES, NO, conf.fancyWidgetsList)
				.withTooltip(ignored -> Tooltip.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList.@Tooltip")))
				.create(Component.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList"), (button, value) -> conf.fancyWidgetsList = value)
		);
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
		SkyblockerConfigManager.update(ignored -> {});
	}
}
