package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.Dungeons;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.model.SlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileLoadState;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerScreenRework;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class DungeonsPage implements ProfileViewerPage {

	List<ProfileViewerWidget.Instance> widgets = new ArrayList<>();

	public DungeonsPage(ProfileLoadState.SuccessfulLoad load) {
		var dungeonsData = load.member().dungeons;
		List<ProfileViewerWidget> dungeons = new ArrayList<>();
		dungeons.add(new SkillWidget(PlayerData.Skill.CATACOMBS, PlayerData.Skill.CATACOMBS.getLevelInfo(dungeonsData.dungeonInfo.catacombs.experience), OptionalInt.empty()));
		dungeons.add(new DungeonsLevelWidget(Dungeons.Class.HEALER, dungeonsData.getClassData(Dungeons.Class.HEALER).getLevelInfo()));
		dungeons.add(new DungeonsLevelWidget(Dungeons.Class.MAGE, dungeonsData.getClassData(Dungeons.Class.MAGE).getLevelInfo()));
		dungeons.add(new DungeonsLevelWidget(Dungeons.Class.BERSERK, dungeonsData.getClassData(Dungeons.Class.BERSERK).getLevelInfo()));
		dungeons.add(new DungeonsLevelWidget(Dungeons.Class.ARCHER, dungeonsData.getClassData(Dungeons.Class.ARCHER).getLevelInfo()));
		dungeons.add(new DungeonsLevelWidget(Dungeons.Class.TANK, dungeonsData.getClassData(Dungeons.Class.TANK).getLevelInfo()));

		int i = 0;
		for (var dungeon : dungeons) {
			int x = i < 6 ? 88 : 88 + 113;
			int y = (i % 6) * (2 + 26);
			i++;
			widgets.add(widget(
					x, y, dungeon
			));
		}
		widgets.add(widget(0, 0, new EntityViewerWidget(load.mainMemberId())));
		widgets.add(widget(0, 112, new PlayerMetaWidget(load)));
	}

	@Init
	public static void init() {
		ProfileViewerScreenRework.PAGE_CONSTRUCTORS.add(DungeonsPage::new);
	}

	@Override
	public int getSortIndex() {
		return 2;
	}

	@Override
	public ItemStack getIcon() {
		return Ico.AATROX_BATPHONE_SKULL;
	}

	@Override
	public String getName() {
		return "Slayers";
	}

	@Override
	public List<ProfileViewerWidget.Instance> getWidgets() {
		// TODO: add player widget to the left only on this page.
		return widgets;
	}
}
