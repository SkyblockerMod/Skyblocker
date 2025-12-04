package de.hysky.skyblocker.skyblock.profileviewer;

import de.hysky.skyblocker.skyblock.profileviewer.utils.SubPageSelectButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.List;

public interface ProfileViewerPage {
	void render(DrawContext context, int mouseX, int mouseY, float delta, int rootX, int rootY);
	default List<ClickableWidget> getButtons() {
		return null;
	}
	default void onNavButtonClick(SubPageSelectButton selectButton) {}
	default void markWidgetsAsVisible() {}
	default void markWidgetsAsInvisible() {}
	default void nextPage() {}
	default void previousPage() {}
}
