package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

@RegisterWidget
public class PowderMiningWidget extends HudWidget {
	private static final Minecraft CLIENT = Minecraft.getInstance();

	public PowderMiningWidget() {
		super(new Information("powder_mining_tracker", Component.translatable("skyblocker.powderTracker"), l -> l == Location.CRYSTAL_HOLLOWS));
	}

	@Override
	public void renderWidget(GuiGraphics context, float delta) {
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
			context.drawString(CLIENT.font, text, 0, tempY, CommonColors.WHITE);

			tempY += 10;
			int width = CLIENT.font.width(text);
			if (width > maxWidth) maxWidth = width;
		}
		tempY += 10;
		context.drawString(CLIENT.font, Component.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).withStyle(ChatFormatting.GOLD), 0, tempY, CommonColors.WHITE);

		w = maxWidth;
		h = tempY + 10;
	}

	@Override
	protected void renderWidgetConfig(GuiGraphics context, float delta) {
		renderWidget(context, delta);
	}
}
