package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.*;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.List;

public class SkillsPage implements ProfileViewerPage {

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

		skills.add(new SkillWidget(PlayerData.Skill.CATACOMBS, PlayerData.Skill.CATACOMBS.getLevelInfo(load.member().dungeons.dungeonInfo.catacombs.experience), OptionalInt.empty()));
		skills.add(new SkillWidget(PlayerData.Skill.RUNECRAFTING, playerData.getSkillLevel(PlayerData.Skill.RUNECRAFTING), OptionalInt.empty()));
		skills.add(new SkillWidget(
				PlayerData.Skill.SOCIAL,
				PlayerData.Skill.SOCIAL.getLevelInfo(
						load.profile().members.values()
								.stream().mapToDouble(it -> it.playerData.getSkillExperience(PlayerData.Skill.SOCIAL))
								.sum()),
				OptionalInt.empty()));


		int i = 0;
		for (var skill : skills) {
			int x = i < 6 ? 88 : 88 + 113;
			int y = (i % 6) * (2 + 26);
			i++;
			widgets.add(widget(
					x, y, skill
			));
		}
		widgets.add(widget(0, 0, new EntityViewerWidget(load.mainMemberId())));
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
