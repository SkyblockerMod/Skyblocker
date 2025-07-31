package de.hysky.skyblocker.skyblock.profileviewer.rework;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

import java.util.List;

public class LoadingPage implements ProfileViewerPage {
	@Override
	public int getSortIndex() {
		return 0;
	}

	@Override
	public ItemStack getIcon() {
		return Ico.CLOCK;
	}

	@Override
	public String getName() {
		return "Loading...";
	}

	@Override
	public List<ProfileViewerWidget.Instance> getWidgets() {
		return List.of(
				ProfileViewerWidget.widget(ProfileViewerScreenRework.PAGE_WIDTH / 2, 8, TextWidget.centered(Text.of("Loading profile...")))
		);
	}
}
