package de.hysky.skyblocker.skyblock.profileviewer2.widgets;

import java.awt.Color;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer2.model.ProfileMember;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.LevelInfo;
import de.hysky.skyblocker.skyblock.profileviewer2.utils.Skill;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;

public final class LevelBarWidget extends ProfileViewerWidget {
	private static final Identifier BACKGROUND = SkyblockerMod.id("profile_viewer2/basic_background");
	private static final Identifier BAR_BACKGROUND = SkyblockerMod.id("bars/bar_back");
	private static final Identifier BAR_FILL = SkyblockerMod.id("bars/bar_fill");
	private static final int HEIGHT = 26;
	private static final int ICON_BOX_SIZE = 22;
	private static final int ICON_BOX_Y_OFFSET = 2;
	// The gap of 4 is for space between icon & content box
	private static final int CONTENT_BOX_OFFSET = ICON_BOX_SIZE + 4;
	// Extra padding of 3 is so the text & bar aren't against the left edge of the content box
	private static final int CONTENT_OFFSET = CONTENT_BOX_OFFSET + 3;
	private static final int TEXT_Y_OFFSET = 5;
	private static final int BAR_OFFSET = TEXT_Y_OFFSET + getFont().lineHeight + 1;
	private static final int BAR_WIDTH = 75;
	private static final int BAR_HEIGHT = 6;
	private final FlexibleItemStack icon;
	private final double barFillPercentage;
	private final Color barFillColour;

	public LevelBarWidget(int width) {
		this(width, Ico.BARRIER, Component.literal("Placeholder"), 0.75d, Color.CYAN);
	}

	public LevelBarWidget(int width, FlexibleItemStack icon, Component label, double barFillPercentage, Color barFillColour) {
		super(0, 0, width, HEIGHT, label);
		this.icon = icon;
		this.barFillPercentage = barFillPercentage;
		this.barFillColour = barFillColour;
	}

	public static LevelBarWidget forSkill(int width, Skill skill, ProfileMember member) {
		LevelInfo levelInfo = member.playerData.getSkillLevel(skill, member);
		Component label = Component.literal(skill.getFriendlyName() + " " + levelInfo.level());
		double barFillPercentage = levelInfo.percentageToNextLevel();
		Color barFillColour = switch ((Integer) levelInfo.level()) {
			case Integer i when i >= skill.getBaseCap() && i < skill.getAbsoluteCap() -> Color.YELLOW;
			case Integer i when i >= skill.getBaseCap() -> Color.MAGENTA;
			default -> Color.GREEN;
		};

		return new LevelBarWidget(width, skill.getIcon(), label, barFillPercentage, barFillColour);
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
		// Background
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX(), this.getY() + ICON_BOX_Y_OFFSET, ICON_BOX_SIZE, ICON_BOX_SIZE);

		// Icon
		graphics.fakeItem(this.icon.getStackOrThrow(), this.getX() + (ICON_BOX_SIZE - ITEM_SIZE) / 2, this.getY() + ICON_BOX_Y_OFFSET + (ICON_BOX_SIZE - ITEM_SIZE) / 2);

		// Content Area background
		int contentAreaWidth = this.getWidth() - CONTENT_BOX_OFFSET;
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BACKGROUND, this.getX() + CONTENT_BOX_OFFSET, this.getY(), contentAreaWidth, HEIGHT);

		// Label
		graphics.text(getFont(), this.getMessage(), this.getX() + CONTENT_OFFSET, this.getY() + TEXT_Y_OFFSET, CommonColors.WHITE);

		// Bars
		int barFillWidth = (int) (this.barFillPercentage * BAR_WIDTH);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_BACKGROUND, this.getX() + CONTENT_OFFSET, this.getY() + BAR_OFFSET, BAR_WIDTH, BAR_HEIGHT);
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BAR_FILL, this.getX() + CONTENT_OFFSET, this.getY() + BAR_OFFSET, barFillWidth, BAR_HEIGHT, this.barFillColour.getRGB());
	}
}
