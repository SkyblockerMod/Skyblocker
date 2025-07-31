package de.hysky.skyblocker.skyblock.profileviewer.rework;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;

@NotNullByDefault
public interface ProfileViewerPage extends Comparable<ProfileViewerPage> {
	int getSortIndex();

	@Override
	default int compareTo(@NotNull ProfileViewerPage o) {
		return Integer.compare(this.getSortIndex(), o.getSortIndex());
	}

	default ProfileViewerWidget.Instance widget(int x, int y, ProfileViewerWidget widget) {
		return ProfileViewerWidget.widget(x, y, widget);
	}

	ItemStack getIcon();

	String getName();

	List<ProfileViewerWidget.Instance> getWidgets();
}
