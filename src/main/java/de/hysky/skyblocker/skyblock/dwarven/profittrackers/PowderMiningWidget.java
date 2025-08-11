package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.Location;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;

import java.util.Set;

@RegisterWidget
public class PowderMiningWidget extends HudWidget {
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Set<Location> LOCATIONS = Set.of(Location.CRYSTAL_HOLLOWS);

	public PowderMiningWidget() {
		super("powder_mining_tracker");
	}

	@Override
	public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
		var set = PowderMiningTracker.getShownRewards().object2IntEntrySet();
		if (set.isEmpty()) {
			setDimensions(0, 0);
			return;
		}

		int tempY = y;
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
		context.drawTextWithShadow(CLIENT.textRenderer, Text.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).formatted(Formatting.GOLD), x, tempY, Colors.WHITE);

		setDimensions(maxWidth, tempY - y + 10);
	}


	@Override
	public Set<Location> availableLocations() {
		return LOCATIONS;
	}

	@Override
	public void setEnabledIn(Location location, boolean enabled) {
		if (!LOCATIONS.contains(location)) return;
		SkyblockerConfigManager.get().mining.crystalHollows.enablePowderTracker = enabled;
	}

	@Override
	public boolean isEnabledIn(Location location) {
		if (!LOCATIONS.contains(location)) return false;
		return SkyblockerConfigManager.get().mining.crystalHollows.enablePowderTracker;
	}

	@Override
	public void update() {}

	@Override
	public Text getDisplayName() {
		return Text.translatable("skyblocker.powderTracker");
	}
}
