package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.item.SkyblockItemRarity;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.EliteLeaderboards;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;

public sealed class LevelBarWidget extends AbstractWidget permits DungeonLevelBarWidget, SkillLevelBarWidget, SkyBlockLevelBarWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");
	private static final Identifier BAR_BACKGROUND = SkyblockerMod.id("bars/bar_back");
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final int HEIGHT = 22;
	private static final int ICON_AREA_SIZE = 22;
	private static final int TEXT_Y_OFFSET = 3;
	private static final int BAR_OFFSET = TEXT_Y_OFFSET + Minecraft.getInstance().font.lineHeight + 1;
	private static final int BAR_WIDTH = 75;
	private static final int BAR_HEIGHT = 6;
	private final ItemStack icon;
	private final double barFillPercentage;
	private final int barFillColour;
	private final List<Component> tooltip;
	private final @Nullable Identifier tooltipStyle;

	protected LevelBarWidget(int width, ItemStack icon, Component label, double barFillPercentage, int barFillColour, List<Component> tooltip, @Nullable Identifier tooltipStyle) {
		super(0, 0, width, HEIGHT, label);
		this.icon = icon;
		this.barFillPercentage = barFillPercentage;
		this.barFillColour = barFillColour;
		this.tooltip = tooltip;
		this.tooltipStyle = tooltipStyle;

		// Make the widget ignore clicks
		this.active = false;
	}

	public static LevelBarWidget placeholder(int width) {
		return new LevelBarWidget(width, Ico.BARRIER.getStackOrThrow(), Component.literal("Placeholder"), 0.75d, Color.CYAN.getRGB(), List.of(), null);
	}

	protected static double getBarFillPercentage(LevelInfo levelInfo) {
		return levelInfo.progress().isPresent() ? levelInfo.progress().get().percentageToNextLevel() : 1f;
	}

	protected static int getBarFillColour(LevelInfo levelInfo) {
		Color barFillColour = Color.GREEN;

		// If the level is capped set the bar colour to yellow or if its fully maxed set the colour to magenta
		if (levelInfo.isLevelCapped()) {
			barFillColour = Color.YELLOW;
		} else if (levelInfo.isLevelAbsolutelyMaxed()) {
			barFillColour = Color.MAGENTA;
		}

		return barFillColour.getRGB();
	}

	protected static List<Component> buildTooltip(Component label, LevelInfo levelInfo, int leaderboardPosition) {
		List<Component> tooltip = new ArrayList<>();

		tooltip.add(label.copy().withStyle(ChatFormatting.GREEN));
		tooltip.add(Component.literal("XP: " + Formatters.INTEGER_NUMBERS.format(levelInfo.xp())).withStyle(ChatFormatting.GOLD));

		// Add progress text if not fully maxed
		if (levelInfo.isLevelNotAtAnyMaximum() && levelInfo.progress().isPresent()) {
			LevelInfo.Progress progress = levelInfo.progress().get();

			tooltip.add(Component.literal("Progress: " + Formatters.INTEGER_NUMBERS.format(progress.xpProgress()) + "/" + Formatters.INTEGER_NUMBERS.format(progress.xpNeeded())).withStyle(ChatFormatting.GOLD));
		}

		if (leaderboardPosition != EliteLeaderboards.NO_POSITION) {
			tooltip.add(Component.literal("Leaderboard: #" + Formatters.INTEGER_NUMBERS.format(leaderboardPosition)).withStyle(ChatFormatting.GOLD));
		}

		return List.copyOf(tooltip);
	}

	/// @param divisor how often the tooltip style should "level up"
	protected static @Nullable Identifier getTooltipStyle(LevelInfo levelInfo, int divisor) {
		int realDivisor = Math.max(1, divisor);
		SkyblockItemRarity tooltipRarity = SkyblockItemRarity.values()[Math.min(levelInfo.level() / realDivisor, 6)];

		return tooltipRarity.toTooltipStyle();
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// Background
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// Icon
		graphics.fakeItem(this.icon, this.getX() + (ICON_AREA_SIZE - GuiRenderer.DEFAULT_ITEM_SIZE) / 2, this.getY() + (ICON_AREA_SIZE - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);

		// Label
		graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + ICON_AREA_SIZE, this.getY() + TEXT_Y_OFFSET, CommonColors.WHITE);

		// Bars
		int barX = this.getX() + ICON_AREA_SIZE;
		int barY = this.getY() + BAR_OFFSET;
		int barFillWidth = (int) (this.barFillPercentage * BAR_WIDTH);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, barX, barY, BAR_WIDTH, BAR_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_FILL, barX, barY, barFillWidth, BAR_HEIGHT, this.barFillColour);

		// Tooltip
		if (GuiHelper.pointIsInArea(mouseX, mouseY, barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT)) {
			graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY, this.tooltipStyle);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
