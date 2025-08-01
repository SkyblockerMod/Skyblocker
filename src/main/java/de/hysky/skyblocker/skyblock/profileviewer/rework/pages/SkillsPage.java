package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileLoadState;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer.rework.widgets.BarWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class SkillsPage implements ProfileViewerPage {

	List<ProfileViewerWidget.Instance> widgets = new ArrayList<>();

	public SkillsPage(ProfileLoadState.SuccessfulLoad load) {
		var playerData = load.member().playerData;
		List<ProfileViewerWidget> skills = new ArrayList<>();
		skills.add(new BarWidget(PlayerData.Skill.COMBAT.getName(), PlayerData.Skill.COMBAT.getIcon(), playerData.getSkillLevel(PlayerData.Skill.COMBAT), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.MINING.getName(), PlayerData.Skill.MINING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.MINING), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.FARMING.getName(), PlayerData.Skill.FARMING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.FARMING), OptionalInt.empty(), OptionalInt.of(50)));
		skills.add(new BarWidget(PlayerData.Skill.FORAGING.getName(), PlayerData.Skill.FORAGING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.FORAGING), OptionalInt.empty(), OptionalInt.of(50)));
		skills.add(new BarWidget(PlayerData.Skill.FISHING.getName(), PlayerData.Skill.FISHING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.FISHING), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.ENCHANTING.getName(), PlayerData.Skill.ENCHANTING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.ENCHANTING), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.ALCHEMY.getName(), PlayerData.Skill.ALCHEMY.getIcon(), playerData.getSkillLevel(PlayerData.Skill.ALCHEMY), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.TAMING.getName(), PlayerData.Skill.TAMING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.TAMING), OptionalInt.empty(), OptionalInt.of(50)));
		skills.add(new BarWidget(PlayerData.Skill.CARPENTRY.getName(), PlayerData.Skill.CARPENTRY.getIcon(), playerData.getSkillLevel(PlayerData.Skill.CARPENTRY), OptionalInt.empty(), OptionalInt.empty()));

		skills.add(new BarWidget(PlayerData.Skill.CATACOMBS.getName(), PlayerData.Skill.CATACOMBS.getIcon(), PlayerData.Skill.CATACOMBS.getLevelInfo(load.member().dungeons.dungeonInfo.catacombs.experience), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(PlayerData.Skill.RUNECRAFTING.getName(), PlayerData.Skill.RUNECRAFTING.getIcon(), playerData.getSkillLevel(PlayerData.Skill.RUNECRAFTING), OptionalInt.empty(), OptionalInt.empty()));
		skills.add(new BarWidget(
				PlayerData.Skill.SOCIAL.getName(),
				PlayerData.Skill.SOCIAL.getIcon(),
				PlayerData.Skill.SOCIAL.getLevelInfo(
						load.profile().members.values()
								.stream().mapToDouble(it -> it.playerData.getSkillExperience(PlayerData.Skill.SOCIAL))
								.sum()),
				OptionalInt.empty(),
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
		widgets.add(widget(0, 112, new PlayerMetaWidget(load)));
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
