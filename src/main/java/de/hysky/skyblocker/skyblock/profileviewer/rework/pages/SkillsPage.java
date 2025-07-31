package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.*;
import de.hysky.skyblocker.skyblock.profileviewer.utils.LevelFinder;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import de.hysky.skyblocker.utils.Formatters;
import de.hysky.skyblocker.utils.NEURepoManager;
import de.hysky.skyblocker.utils.render.HudHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalInt;

public class SkillsPage implements ProfileViewerPage {
	private static final Identifier ICON_DATA_TEXTURE = Identifier.of(SkyblockerMod.NAMESPACE, "textures/gui/profile_viewer/icon_data_widget.png");
	private static final Identifier BAR_FILL = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_fill");
	private static final Identifier BAR_BACK = Identifier.of(SkyblockerMod.NAMESPACE, "bars/bar_back");

	record SkillWidget(
			PlayerData.Skill skill,
			LevelFinder.LevelInfo levelInfo,
			OptionalInt softSkillCap
	) implements ProfileViewerWidget {
		static TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

		@Override
		public void render(DrawContext drawContext,
						   int x, int y, int mouseX, int mouseY, float deltaTicks) {
			drawContext.drawTexture(RenderPipelines.GUI_TEXTURED, ICON_DATA_TEXTURE, x, y, 0, 0, 109, 26, 109, 26);
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
	}

	List<ProfileViewerWidget.Instance> widgets = new ArrayList<>();

	public SkillsPage(ProfileLoadState.SuccessfulLoad load) {
		var playerData = load.member().playerData;
		List<SkillWidget> skills = new ArrayList<>();
		skills.add(new SkillWidget(PlayerData.Skill.COMBAT, playerData.getSkillLevel(PlayerData.Skill.COMBAT), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.MINING, playerData.getSkillLevel(PlayerData.Skill.MINING), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.FARMING, playerData.getSkillLevel(PlayerData.Skill.FARMING), OptionalInt.of(50)));
		skills.add(new SkillWidget(PlayerData.Skill.FORAGING, playerData.getSkillLevel(PlayerData.Skill.FORAGING), OptionalInt.of(50)));
		skills.add(new SkillWidget(PlayerData.Skill.FISHING, playerData.getSkillLevel(PlayerData.Skill.FISHING), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.ENCHANTING, playerData.getSkillLevel(PlayerData.Skill.ENCHANTING), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.ALCHEMY, playerData.getSkillLevel(PlayerData.Skill.ALCHEMY), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.TAMING, playerData.getSkillLevel(PlayerData.Skill.TAMING), OptionalInt.of(50)));
		skills.add(new SkillWidget(PlayerData.Skill.CARPENTRY, playerData.getSkillLevel(PlayerData.Skill.CARPENTRY), OptionalInt.empty()));
//		skills.add(new SkillWidget(PlayerData.Skill., playerData.getSkillLevel(PlayerData.Skill.COMBAT), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.RUNECRAFTING, playerData.getSkillLevel(PlayerData.Skill.RUNECRAFTING), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.SOCIAL, playerData.getSkillLevel(PlayerData.Skill.SOCIAL), OptionalInt.empty())); // TODO: cross player member


		int i = 0;
		for (var skill : skills) {
			int x = i < 6 ? 88 : 88 + 113;
			int y = (i % 6) * (2 + 26);
			i++;
			widgets.add(widget(
					x, y, skill
			));
		}
	}

	@Init
	public static void init() {
		ProfileViewerScreenRework.PAGE_CONSTRUCTORS.add(SkillsPage::new);
	}

	@Override
	public int getSortIndex() {
		return 0;
	}

	@Override
	public ItemStack getIcon() {
		return Ico.IRON_SWORD;
	}

	@Override
	public String getName() {
		return "Skills";
	}

	@Override
	public List<ProfileViewerWidget.Instance> getWidgets() {
		// TODO: add player widget to the left only on this page.
		return widgets;
	}
}
