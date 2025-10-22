package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

@RegisterWidget
public class PowderMiningWidget extends HudWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

	public PowderMiningWidget() {
		super(new Information("powder_mining_tracker", Text.translatable("skyblocker.powderTracker"), l -> l == Location.CRYSTAL_HOLLOWS));
	}

	@Override
	public void renderWidget(DrawContext context, float delta) {
		var set = PowderMiningTracker.getShownRewards().object2IntEntrySet();
		if (set.isEmpty()) {
			w = h = 0;
			return;
		}

		int tempY = 0;
		int maxWidth = 0;

		for (Object2IntMap.Entry<Text> entry : set) {
			Text price = Text.literal(Formatters.INTEGER_NUMBERS.format(entry.getIntValue())).withColor(Colors.WHITE);
			Text text = entry.getKey().copy().append(" ").append(price);
			context.drawTextWithShadow(CLIENT.textRenderer, text, x, tempY, Colors.WHITE);

			tempY += 10;
			int width = CLIENT.textRenderer.getWidth(text);
			if (width > maxWidth) maxWidth = width;
		}
		tempY += 10;
		context.drawTextWithShadow(CLIENT.textRenderer, Text.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).formatted(Formatting.GOLD), 0, tempY, Colors.WHITE);

		w = maxWidth;
		h = tempY + 10;
	}

	@Override
	protected void renderWidgetConfig(DrawContext context, float delta) {
		renderWidget(context, delta);
	}
}
