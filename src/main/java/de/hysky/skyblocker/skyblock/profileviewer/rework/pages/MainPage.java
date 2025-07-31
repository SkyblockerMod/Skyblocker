package de.hysky.skyblocker.skyblock.profileviewer.rework.pages;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.skyblock.profileviewer.rework.*;
import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class MainPage implements ProfileViewerPage {
	public MainPage(ProfileLoadState.SuccessfulLoad load) {}

	@Init
	public static void init() {
		ProfileViewerScreenRework.PAGE_CONSTRUCTORS.add(MainPage::new);
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
		return List.of(
				ProfileViewerWidget.widget(ProfileViewerScreenRework.GUI_WIDTH / 2, 8, TextWidget.centered(Text.of("Skills")))
		);
	}
}
