package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

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

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.render.GuiHelper;

public final class LevelBarWidget extends AbstractWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");
	private static final Identifier BAR_BACKGROUND = SkyblockerMod.id("bars/bar_back");
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final int HEIGHT = 22;
	private static final int ICON_AREA_SIZE = 22;
	private static final int TEXT_Y_OFFSET = 3;
	private static final int BAR_OFFSET = TEXT_Y_OFFSET + Minecraft.getInstance().font.lineHeight + 1;
	private static final int BAR_WIDTH = 75;
	private static final int BAR_HEIGHT = 6;
	private final FlexibleItemStack icon;
	private final double barFillPercentage;
	private final Color barFillColour;
	private final List<Component> tooltip;

	public LevelBarWidget(int width) {
		this(width, Ico.BARRIER, Component.literal("Placeholder"), 0.75d, Color.CYAN, List.of());
	}

	private LevelBarWidget(int width, FlexibleItemStack icon, Component label, double barFillPercentage, Color barFillColour, List<Component> tooltip) {
		super(0, 0, width, HEIGHT, label);
		this.icon = icon;
		this.barFillPercentage = barFillPercentage;
		this.barFillColour = barFillColour;
		this.tooltip = tooltip;

		// Make the widget ignore clicks
		this.active = false;
	}

	public static LevelBarWidget forSkill(int width, Skill skill, ProfileMember member) {
		LevelInfo levelInfo = member.playerData.getSkillLevel(skill, member);
		Component label = Component.literal(skill.getFriendlyName() + " " + levelInfo.level());
		double barFillPercentage = levelInfo.progress().isPresent() ? levelInfo.progress().get().percentageToNextLevel() : 1f;
		Color barFillColour = Color.GREEN;

		if (levelInfo.isLevelCapped()) {
			barFillColour = Color.YELLOW;
		} else if (levelInfo.isLevelAbsolutelyMaxed()) {
			barFillColour = Color.MAGENTA;
		}

		List<Component> tooltip = new ArrayList<>();
		tooltip.add(label.plainCopy().withStyle(ChatFormatting.GREEN));
		tooltip.add(Component.literal("XP: " + Formatters.INTEGER_NUMBERS.format(levelInfo.xp())).withStyle(ChatFormatting.GOLD));

		if (levelInfo.isLevelNotAtAnyMaximum() && levelInfo.progress().isPresent()) {
			LevelInfo.Progress progress = levelInfo.progress().get();

			tooltip.add(Component.literal("Progress: " + Formatters.INTEGER_NUMBERS.format(progress.xpProgress()) + "/" + Formatters.INTEGER_NUMBERS.format(progress.xpNeeded())).withStyle(ChatFormatting.GOLD));
		}

		return new LevelBarWidget(width, skill.getIcon(), label, barFillPercentage, barFillColour, List.copyOf(tooltip));
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// Background
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY(), this.getWidth(), this.getHeight());

		// Icon
		graphics.fakeItem(this.icon.getStackOrThrow(), this.getX() + (ICON_AREA_SIZE - GuiRenderer.DEFAULT_ITEM_SIZE) / 2, this.getY() + (ICON_AREA_SIZE - GuiRenderer.DEFAULT_ITEM_SIZE) / 2);

		// Label
		graphics.text(Minecraft.getInstance().font, this.getMessage(), this.getX() + ICON_AREA_SIZE, this.getY() + TEXT_Y_OFFSET, CommonColors.WHITE);

		// Bars
		int barX = this.getX() + ICON_AREA_SIZE;
		int barY = this.getY() + BAR_OFFSET;
		int barFillWidth = (int) (this.barFillPercentage * BAR_WIDTH);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, barX, barY, BAR_WIDTH, BAR_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_FILL, barX, barY, barFillWidth, BAR_HEIGHT, this.barFillColour.getRGB());

		// Tooltip
		if (GuiHelper.pointIsInArea(mouseX, mouseY, barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT)) {
			graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
		}
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {}

	@Override
	public boolean shouldTakeFocusAfterInteraction() {
		return false;
	}
}
