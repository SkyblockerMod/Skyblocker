package de.hysky.skyblocker.skyblock.tabhud.config;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.gui.RangedSliderWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CyclingButtonWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.gui.widget.ThreePartsLayoutWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

// TODO translatable
class GlobalOptionsScreen extends Screen {
	private final Supplier<UIAndVisualsConfig.HudConf> CONFIG = () -> SkyblockerConfigManager.get().uiAndVisuals.hud;
	private final Tooltip STYLE_TOOLTIP = Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[0]").append("\n")
			.append(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[1]")).append("\n")
			.append(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[2]")).append("\n")
			.append(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style.@Tooltip[3]")));
	private final Text YES = ScreenTexts.YES.copy().formatted(Formatting.GREEN);
	private final Text NO = ScreenTexts.NO.copy().formatted(Formatting.RED);
	private final ThreePartsLayoutWidget layout = new ThreePartsLayoutWidget(this);
	private final Screen parent;

	GlobalOptionsScreen(Screen parent) {
		super(Text.literal("HUD and TAB options"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		layout.addHeader(new TextWidget(title, textRenderer));
		GridWidget.Adder body = layout.addBody(new GridWidget().setSpacing(2)).createAdder(2);
		layout.addFooter(ButtonWidget.builder(ScreenTexts.DONE, b -> close()).build());

		UIAndVisualsConfig.HudConf conf = CONFIG.get();
		body.add(CyclingButtonWidget.<UIAndVisualsConfig.HudStyle>builder(style -> Text.translatable(style.toString()))
				.values(UIAndVisualsConfig.HudStyle.values())
				.initially(conf.style)
				.tooltip(ignored -> STYLE_TOOLTIP)
				.build(
						0,
						0,
						ButtonWidget.DEFAULT_WIDTH * 2 + 2,
						ButtonWidget.DEFAULT_HEIGHT,
						Text.translatable("skyblocker.config.uiAndVisuals.tabHud.style"),
						(button, value) -> conf.style = value),
				2);
		body.add(RangedSliderWidget.builder()
						.optionFormatter(Text.literal("Global Scale"), Formatters.INTEGER_NUMBERS) // FIXME add % when chat rules thing is merged
						.defaultValue(conf.hudScale)
						.step(1)
						.minMax(10, 200)
						.callback(d -> conf.hudScale = (int) Math.round(d))
				.build());
		// TODO turn these two into per widget things maybe?
		body.add(CyclingButtonWidget.onOffBuilder(YES, NO)
				.initially(conf.displayIcons)
				.build(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.displayIcons"), (button, value) -> conf.displayIcons = value)
		);
		body.add(CyclingButtonWidget.onOffBuilder(YES, NO)
						.initially(conf.compactWidgets)
						.tooltip(ignored -> Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets.@Tooltip")))
				.build(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.compactWidgets"), (button, value) -> conf.compactWidgets = value)
		);
		body.add(CyclingButtonWidget.onOffBuilder(YES, NO)
				.initially(conf.fancyWidgetsList)
				.tooltip(ignored -> Tooltip.of(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList.@Tooltip")))
				.build(Text.translatable("skyblocker.config.uiAndVisuals.tabHud.fancyWidgetsList"), (button, value) -> conf.fancyWidgetsList = value)
		);
		layout.forEachChild(this::addDrawableChild);
		refreshWidgetPositions();
	}

	@Override
	protected void refreshWidgetPositions() {
		layout.refreshPositions();
	}


	@Override
	public void close() {
		client.setScreen(parent);
	}

	@Override
	public void removed() {
		super.removed();
		SkyblockerConfigManager.update(ignored -> {});
	}
}
