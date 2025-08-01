package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
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
		if (CLIENT.currentScreen instanceof WidgetsConfigurationScreen) {
			if (!set.isEmpty() && this.w == 0) {
				calculateWidgetSize();
			}
		}

		int newY = y;
		for (Object2IntMap.Entry<Text> entry : set) {
			context.drawTextWithShadow(CLIENT.textRenderer, entry.getKey(), x, newY, Colors.WHITE);
			context.drawTextWithShadow(CLIENT.textRenderer, Text.of(Formatters.INTEGER_NUMBERS.format(entry.getIntValue())), x + 5 + CLIENT.textRenderer.getWidth(entry.getKey()), newY, Colors.WHITE);
			newY += 10;
		}
		context.drawTextWithShadow(CLIENT.textRenderer, Text.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).formatted(Formatting.GOLD), x, newY + 10, Colors.WHITE);
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
	public void update() {
	}

	private void calculateWidgetSize() {
		var set = PowderMiningTracker.getShownRewards().object2IntEntrySet();
		if (set.isEmpty()) {
			this.w = 0;
			this.h = 0;
			return;
		}

		int maxWidth = 0;
		for (Object2IntMap.Entry<Text> entry : set) {
			Text valueText = Text.of(Formatters.INTEGER_NUMBERS.format(entry.getIntValue()));
			Text line = entry.getKey().copy().append(valueText);
			int lineWidth = CLIENT.textRenderer.getWidth(line) + 5;
			if (lineWidth > maxWidth) {
				maxWidth = lineWidth;
			}
		}
		this.w = maxWidth;
		this.h = 10 * (set.size() + 2);
	}

	@Override
	public Text getDisplayName() {
		return Text.translatable("skyblocker.powderTracker");
	}
}
