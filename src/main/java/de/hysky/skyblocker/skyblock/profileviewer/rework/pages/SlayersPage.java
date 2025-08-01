package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.model.SlayerData;
import de.hysky.skyblocker.skyblock.profileviewer.rework.*;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

public class SlayersPage implements ProfileViewerPage {

	List<ProfileViewerWidget.Instance> widgets = new ArrayList<>();

	public SlayersPage(ProfileLoadState.SuccessfulLoad load) {
		var slayerData = load.member().slayer;
		// TODO: Maybe make it's own SlayerWidget
		List<ProfileViewerWidget> slayers = new ArrayList<>();
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.REVENANT_HORROR, slayerData.getSkillLevel(SlayerData.Slayer.REVENANT_HORROR)));
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.TARANTULA_BROODFATHER, slayerData.getSkillLevel(SlayerData.Slayer.TARANTULA_BROODFATHER)));
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.SVEN_PACKMASTER, slayerData.getSkillLevel(SlayerData.Slayer.SVEN_PACKMASTER)));
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.VOIDGLOOM_SERAPH, slayerData.getSkillLevel(SlayerData.Slayer.VOIDGLOOM_SERAPH)));
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.RIFTSTALKER_BLOODFIEND, slayerData.getSkillLevel(SlayerData.Slayer.RIFTSTALKER_BLOODFIEND)));
		slayers.add(new SlayerLevelWidget(SlayerData.Slayer.INFERNO_DEMONLORD, slayerData.getSkillLevel(SlayerData.Slayer.INFERNO_DEMONLORD)));

		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.REVENANT_HORROR, slayerData.getSlayerData(SlayerData.Slayer.REVENANT_HORROR)));
		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.TARANTULA_BROODFATHER, slayerData.getSlayerData(SlayerData.Slayer.TARANTULA_BROODFATHER)));
		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.SVEN_PACKMASTER, slayerData.getSlayerData(SlayerData.Slayer.SVEN_PACKMASTER)));
		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.VOIDGLOOM_SERAPH, slayerData.getSlayerData(SlayerData.Slayer.VOIDGLOOM_SERAPH)));
		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.RIFTSTALKER_BLOODFIEND, slayerData.getSlayerData(SlayerData.Slayer.RIFTSTALKER_BLOODFIEND)));
		slayers.add(new SlayerKillsWidget(SlayerData.Slayer.INFERNO_DEMONLORD, slayerData.getSlayerData(SlayerData.Slayer.INFERNO_DEMONLORD)));

		int i = 0;
		for (var slayer : slayers) {
			int x = i < 6 ? 88 : 88 + 113;
			int y = (i % 6) * (2 + 26);
			i++;
			widgets.add(widget(
					x, y, slayer
			));
		}
		widgets.add(widget(0, 0, new EntityViewerWidget(load.mainMemberId())));
		widgets.add(widget(0, 112, new PlayerMetaWidget(load)));
	}

	@Init
	public static void init() {
		ProfileViewerScreenRework.PAGE_CONSTRUCTORS.add(SlayersPage::new);
	}

	@Override
	public int getSortIndex() {
		return 1;
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
