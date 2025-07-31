package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
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

import static de.hysky.skyblocker.utils.Formatters.INTEGER_NUMBERS;
import static de.hysky.skyblocker.utils.Formatters.SHORT_INTEGER_NUMBERS;

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
		drawContext.drawItem(skill.getIcon(), x + 3, y + 5);
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
			tooltipText.add(Text.literal(skill.getName() + " " + levelInfo.level).formatted(Formatting.GREEN));
			if (levelInfo.level < skillCap) {
				tooltipText.add(Text.literal("Progress to Level " + (levelInfo.level + 1) + ":").formatted(Formatting.GRAY));
				tooltipText.add(Text.literal("")
						.append(formatBar(levelInfo.fill, 15, Formatting.DARK_GREEN, Formatting.GRAY))
						.append(" ")
						.append(INTEGER_NUMBERS.format(levelInfo.levelXP))
						.append("/")
						.append(SHORT_INTEGER_NUMBERS.format(levelInfo.nextLevelXP))
						.formatted(Formatting.YELLOW));
				tooltipText.add(
						Text.literal("XP till " + (levelInfo.level + 1) + ": ")
								.append(Text.literal(INTEGER_NUMBERS.format(levelInfo.nextLevelXP - levelInfo.levelXP)).formatted(Formatting.YELLOW))
								.formatted(Formatting.GRAY));
			} else {
				tooltipText.add(Text.literal("Progress: §6MAXED").formatted(Formatting.GRAY));
			}
			tooltipText.add(Text.literal("§7Total XP: §r" + INTEGER_NUMBERS.format(levelInfo.xp)).formatted(Formatting.YELLOW));
			drawContext.drawTooltip(textRenderer, tooltipText, mouseX, mouseY);
		}
	}

	private static Text formatBar(double percentage, int total, Formatting filledStyle, Formatting emptyStyle) {
		return formatBar((int) Math.round(percentage * total), total, filledStyle, emptyStyle);
	}

	private static Text formatBar(int filled, int total, Formatting filledStyle, Formatting emptyStyle) {
		assert filled <= total;
		return Text.literal("")
				.append(Text.literal(" ".repeat(filled)).formatted(filledStyle, Formatting.STRIKETHROUGH, Formatting.BOLD))
				.append(Text.literal(" ".repeat(total - filled)).formatted(emptyStyle, Formatting.STRIKETHROUGH, Formatting.BOLD));
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
