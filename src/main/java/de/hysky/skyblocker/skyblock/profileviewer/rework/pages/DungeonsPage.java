package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.Dungeons;
import de.hysky.skyblocker.skyblock.profileviewer.model.PlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileLoadState;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerPage;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerScreenRework;
import de.hysky.skyblocker.skyblock.profileviewer.rework.ProfileViewerWidget;
import de.hysky.skyblocker.skyblock.profileviewer.rework.widgets.BarWidget;
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
		dungeons.add(new BarWidget(PlayerData.Skill.CATACOMBS.getName(), PlayerData.Skill.CATACOMBS.getIcon(), PlayerData.Skill.CATACOMBS.getLevelInfo(dungeonsData.dungeonInfo.catacombs.experience), OptionalInt.empty(), OptionalInt.empty()));
		dungeons.add(new BarWidget(Dungeons.Class.HEALER.getName(), Dungeons.Class.HEALER.getIcon(), dungeonsData.getClassData(Dungeons.Class.HEALER).getLevelInfo(), OptionalInt.empty(), OptionalInt.empty()));
		dungeons.add(new BarWidget(Dungeons.Class.MAGE.getName(), Dungeons.Class.MAGE.getIcon(), dungeonsData.getClassData(Dungeons.Class.MAGE).getLevelInfo(), OptionalInt.empty(), OptionalInt.empty()));
		dungeons.add(new BarWidget(Dungeons.Class.BERSERK.getName(), Dungeons.Class.BERSERK.getIcon(), dungeonsData.getClassData(Dungeons.Class.BERSERK).getLevelInfo(), OptionalInt.empty(), OptionalInt.empty()));
		dungeons.add(new BarWidget(Dungeons.Class.ARCHER.getName(), Dungeons.Class.ARCHER.getIcon(), dungeonsData.getClassData(Dungeons.Class.ARCHER).getLevelInfo(), OptionalInt.empty(), OptionalInt.empty()));
		dungeons.add(new BarWidget(Dungeons.Class.TANK.getName(), Dungeons.Class.TANK.getIcon(), dungeonsData.getClassData(Dungeons.Class.TANK).getLevelInfo(), OptionalInt.empty(), OptionalInt.empty()));

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
