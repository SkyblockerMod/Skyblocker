package de.hysky.skyblocker.skyblock.profileviewer.rework;

import de.hysky.skyblocker.skyblock.tabhud.util.Ico;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ErrorPage(ProfileLoadState.Error error) implements ProfileViewerPage {

	@Override
	public int getSortIndex() {
		return 0;
	}

	@Override
	public @NotNull ItemStack getIcon() {
		return Ico.BARRIER;
	}

	@Override
	public @NotNull String getName() {
		return "Error Loading";
	}

	@Override
	public @NotNull List<ProfileViewerWidget.Instance> getWidgets() {
		return List.of(
				ProfileViewerWidget.widget(ProfileViewerScreenRework.PAGE_WIDTH / 2, 8, TextWidget.centered(Text.of("Error!"))),
				ProfileViewerWidget.widget(ProfileViewerScreenRework.PAGE_WIDTH / 2, 19, TextWidget.centered(Text.of(error.message())))
		);
	}
}
