package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.screens.powdertracker.PowderFilterConfigScreen;
import de.hysky.skyblocker.skyblock.tabhud.config.OptionWidgetCollector;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;

@RegisterWidget
public class PowderMiningWidget extends HudWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	public static PowderMiningWidget INSTANCE;

	public PowderMiningWidget() {
		super(new Information("powder_mining_tracker", Component.translatable("skyblocker.powderTracker"), Location.CRYSTAL_HOLLOWS));
		INSTANCE = this;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, float delta) {
		var set = PowderMiningTracker.getShownRewards().object2IntEntrySet();
		if (set.isEmpty()) {
			w = h = 0;
			return;
		}

		int tempY = 0;
		int maxWidth = 0;

		for (Object2IntMap.Entry<Component> entry : set) {
			Component price = Component.literal(Formatters.INTEGER_NUMBERS.format(entry.getIntValue())).withColor(CommonColors.WHITE);
			Component text = entry.getKey().copy().append(" ").append(price);
			graphics.text(CLIENT.font, text, 0, tempY, CommonColors.WHITE);

			tempY += 10;
			int width = CLIENT.font.width(text);
			if (width > maxWidth) maxWidth = width;
		}
		tempY += 10;
		graphics.text(CLIENT.font, Component.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).withStyle(ChatFormatting.GOLD), 0, tempY, CommonColors.WHITE);

		w = maxWidth;
		h = tempY + 10;
	}

	@Override
	protected void extractWidgetRenderStateForConfig(GuiGraphicsExtractor graphics, float delta) {
		extractWidgetRenderState(graphics, delta);
	}

	@Override
	public void getOptionWidgets(OptionWidgetCollector collector) {
		super.getOptionWidgets(collector);
		collector.addWidget(Button.builder(
						Component.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter"),
						_ -> Minecraft.getInstance().gui.setScreen(new PowderFilterConfigScreen(collector.configScreen(), new ObjectImmutableList<>(PowderMiningTracker.getName2IdMap().keySet()))))
				.tooltip(Tooltip.create(Component.translatable("skyblocker.config.mining.crystalHollows.powderTrackerFilter.@Tooltip")))
				.build());
	}
}
