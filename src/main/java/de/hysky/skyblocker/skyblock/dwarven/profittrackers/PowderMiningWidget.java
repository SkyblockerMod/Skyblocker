package de.hysky.skyblocker.skyblock.dwarven.profittrackers;

import de.hysky.skyblocker.annotations.RegisterWidget;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
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
	private static final Set<Location> LOCATIONS = Set.of(Location.CRYSTAL_HOLLOWS);

	public PowderMiningWidget() {
		super("powder_mining_tracker");
	}

	@Override
	public void renderWidget(GuiGraphics context, int mouseX, int mouseY, float delta) {
		var set = PowderMiningTracker.getShownRewards().object2IntEntrySet();
		if (set.isEmpty()) {
			setDimensions(0, 0);
			return;
		}

		int tempY = y;
		int maxWidth = 0;

		for (Object2IntMap.Entry<Component> entry : set) {
			Component price = Component.literal(Formatters.INTEGER_NUMBERS.format(entry.getIntValue())).withColor(CommonColors.WHITE);
			Component text = entry.getKey().copy().append(" ").append(price);
			context.drawString(CLIENT.font, text, x, tempY, CommonColors.WHITE);

			tempY += 10;
			int width = CLIENT.font.width(text);
			if (width > maxWidth) maxWidth = width;
		}
		tempY += 10;
		context.drawString(CLIENT.font, Component.translatable("skyblocker.powderTracker.profit", Formatters.DOUBLE_NUMBERS.format(PowderMiningTracker.getProfit())).withStyle(ChatFormatting.GOLD), x, tempY, CommonColors.WHITE);

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
	public Component getDisplayName() {
		return Component.translatable("skyblocker.powderTracker");
	}
}
