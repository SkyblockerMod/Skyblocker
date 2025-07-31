package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

final class SkillWidget implements ProfileViewerWidget {
	private static final Identifier ICON_DATA_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
	private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
	private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

	private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
	private final PlayerData.Skill skill;
	private final LevelFinder.LevelInfo levelInfo;
	private final OptionalInt softSkillCap;

	SkillWidget(
			PlayerData.Skill skill,
			LevelFinder.LevelInfo levelInfo,
			OptionalInt softSkillCap
	) {
		this.skill = skill;
		this.levelInfo = levelInfo;
		this.softSkillCap = softSkillCap;
	}

	public static final int WIDTH = 109;
	public static final int HEIGHT = 26;

	@Override
	public void render(DrawContext drawContext,
					   int x, int y, int mouseX, int mouseY, float deltaTicks) {
		drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, ICON_DATA_TEXTURE, x, y, 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
		drawContext.drawItem(skill.getIcon(), x + 3, y + 4);
		drawContext.drawText(textRenderer, skill.getName() + " " + levelInfo.level, x + 31, y + 4, -1, false);
		Color fillColor = Color.GREEN;
		var skillCap = NEURepoManager.getConstants().getLeveling().getMaximumLevels().get(skill.name().toLowerCase(Locale.ROOT));
		if (softSkillCap.isPresent() && levelInfo.level > softSkillCap.getAsInt())
			fillColor = Color.YELLOW;
		if (levelInfo.level >= skillCap)
			fillColor = Color.MAGENTA;
		drawContext.drawGuiTexture(RenderPipelines.GUI_TEXTURED, BAR_BACK, x + 30, y + 14, 75, 6);
		HudHelper.renderNineSliceColored(drawContext, BAR_FILL, x + 30, y + 14, (int) (75 * levelInfo.fill), 6, fillColor);

		// TODO: add helper for hover selection
		if (mouseX > x + 30 && mouseX < x + 105 && mouseY > y + 14 && mouseY < y + 21) {
			List<Text> tooltipText = new ArrayList<>();
			tooltipText.add(Text.literal(skill.getName()).formatted(Formatting.GREEN));
			tooltipText.add(Text.literal("XP: " + Formatters.INTEGER_NUMBERS.format(levelInfo.xp)).formatted(Formatting.GOLD));
			if (levelInfo.level < skillCap) {
				tooltipText.add(Text.literal("XP till " + (levelInfo.level + 1) + ": " + Formatters.INTEGER_NUMBERS.format(levelInfo.nextLevelXP - levelInfo.levelXP)).formatted(Formatting.GRAY));
			}
			drawContext.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
		}
	}

	@Override
	public int getHeight() {
		return HEIGHT;
	}

	@Override
	public int getWidth() {
		return WIDTH;
	}
}
