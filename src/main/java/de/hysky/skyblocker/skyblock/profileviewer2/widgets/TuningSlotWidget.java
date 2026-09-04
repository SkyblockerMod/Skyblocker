package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

import de.hysky.skyblocker.skyblock.profileviewer2.model.AccessoryBagStorage.Tuning.TuningSlot;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.AccessoryPowerCalculator;
import de.hysky.skyblocker.utils.CollectionUtils;

public final class TuningSlotWidget extends BasicInfoBoxWidget {
	private static final int OFFSET = 2;
	private static final int TEXT_EXTRA_SPACING = 1;
	private static final int TITLE_BOTTOM_PADDING = 2;
	private final Component title;
	private final Pair<List<Component>, List<Component>> halves;

	public TuningSlotWidget(int width, TuningSlot slot) {
		List<Component> lines = buildLines(slot);
		Pair<List<Component>, List<Component>> halves = CollectionUtils.halve(lines);

		int lineHeight = Minecraft.getInstance().font.lineHeight;
		int titleHeight = lineHeight + TEXT_EXTRA_SPACING + TITLE_BOTTOM_PADDING;
		int linesHeight = (lineHeight + TEXT_EXTRA_SPACING) * Math.max(halves.left().size(), halves.right().size());
		int height = (OFFSET * 2) + titleHeight + linesHeight;

		super(width, height);
		this.title = Component.literal("Tuning Stats").withStyle(ChatFormatting.BOLD);
		this.halves = halves;
	}

	private static List<Component> buildLines(TuningSlot slot) {
		List<Component> lines = new ArrayList<>();

		// Note: The API returns the points allocated to each stat not how much of it you actually get
		// so some stats have to be "scaled" to be accurate.

		if (slot.health > 0) {
			lines.add(AccessoryPowerCalculator.formatHealth(slot.health * 5d));
		}

		if (slot.defence > 0) {
			lines.add(AccessoryPowerCalculator.formatDefence(slot.defence));
		}

		if (slot.strength > 0) {
			lines.add(AccessoryPowerCalculator.formatStrength(slot.strength));
		}

		if (slot.criticalChance > 0) {
			lines.add(AccessoryPowerCalculator.formatCritChance(slot.criticalChance * 0.2d));
		}

		if (slot.criticalDamage > 0) {
			lines.add(AccessoryPowerCalculator.formatCritDamage(slot.criticalDamage));
		}

		if (slot.attackSpeed > 0) {
			lines.add(AccessoryPowerCalculator.formatAttackSpeed(slot.attackSpeed * 0.3d));
		}

		if (slot.intelligence > 0) {
			lines.add(AccessoryPowerCalculator.formatIntelligence(slot.intelligence * 2d));
		}

		if (slot.walkSpeed > 0) {
			lines.add(AccessoryPowerCalculator.formatWalkSpeed(slot.walkSpeed * 1.5d));
		}

		if (lines.isEmpty()) {
			lines.add(Component.literal("Unconfigured...").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
		}

		return List.copyOf(lines);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		super.extractWidgetRenderState(graphics, mouseX, mouseY, a);

		Font font = Minecraft.getInstance().font;
		int x = this.getX() + OFFSET;
		int y = this.getY() + OFFSET;
		final int textYStep = font.lineHeight + TEXT_EXTRA_SPACING;

		// Add title with extra bottom padding
		graphics.text(font, this.title, x, y, CommonColors.WHITE);
		final int titleY = y += textYStep + TITLE_BOTTOM_PADDING;

		// Left half
		for (Component line : this.halves.left()) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}

		// Right half
		x = this.getX() + (this.getWidth() / 2);
		y = titleY;

		for (Component line : this.halves.right()) {
			graphics.text(font, line, x, y, CommonColors.WHITE);
			y += textYStep;
		}
	}
}
