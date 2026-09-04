package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;
import org.apache.commons.text.WordUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.skyblock.profileviewer2.LoadingInformation;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.AccessoryPowerCalculator;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.ProfileItemStorage;
import de.hysky.skyblocker.utils.CollectionUtils;
import de.hysky.skyblocker.utils.Formatters;

public final class AccessoryPowerWidget extends BasicInfoBoxWidget {
	private static final int OFFSET = 2;
	private static final int TEXT_EXTRA_SPACING = 1;
	private final List<Component> mainLines;
	private final Pair<List<Component>, List<Component>> statsHalves;

	public AccessoryPowerWidget(int width, LoadingInformation info, ProfileItemStorage itemStorage) {
		int accessoryPower = AccessoryPowerCalculator.calculateAccessoryPower(info.member(), itemStorage.bags().accessories());
		List<Component> mainLines = buildLines(info, accessoryPower);
		List<Component> statLines = AccessoryPowerCalculator.getPowerStats(accessoryPower, info.member().accessoryBagStorage.selectedPower);
		Pair<List<Component>, List<Component>> statsHalves = CollectionUtils.halve(statLines);

		int lineHeight = Minecraft.getInstance().font.lineHeight;
		int mainLinesHeight = (lineHeight + TEXT_EXTRA_SPACING) * mainLines.size();
		int statLinesHeight = (lineHeight + TEXT_EXTRA_SPACING) * Math.max(statsHalves.left().size(), statsHalves.right().size());

		int height = (OFFSET * 2) + mainLinesHeight + statLinesHeight;

		super(width, height);
		this.mainLines = mainLines;
		this.statsHalves = statsHalves;
	}

	private static List<Component> buildLines(LoadingInformation info, int accessoryPower) {
		List<Component> lines = new ArrayList<>();

		Component apText = Component.empty()
				.append(Component.literal("AP: ").withStyle(ChatFormatting.GOLD))
				.append(Formatters.INTEGER_NUMBERS.format(accessoryPower));
		lines.add(apText);

		Component powerText = Component.empty()
				.append(Component.literal("Power: ").withStyle(ChatFormatting.GREEN))
				.append(WordUtils.capitalizeFully(info.member().accessoryBagStorage.selectedPower));
		lines.add(powerText);

		// Space between info and stats title
		lines.add(Component.empty());

		// Stats title
		lines.add(Component.literal("Power Stats").withStyle(ChatFormatting.BOLD));

		return List.copyOf(lines);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		int x = this.getX() + OFFSET;
		int y = this.getY() + OFFSET;
		final int textYStep = font.lineHeight + TEXT_EXTRA_SPACING;

		for (Component line : this.mainLines) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}

		final int statsY = y;

		// Left half
		for (Component line : this.statsHalves.left()) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}

		// Right half
		x = this.getX() + (this.getWidth() / 2);
		y = statsY;

		for (Component line : this.statsHalves.right()) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}
	}
}
