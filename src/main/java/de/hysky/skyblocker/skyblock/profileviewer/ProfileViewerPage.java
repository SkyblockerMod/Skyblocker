package de.hysky.skyblocker.skyblock.profileviewer;

import de.hysky.skyblocker.skyblock.profileviewer.utils.SubPageSelectButton;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

public interface ProfileViewerPage {
	void render(GuiGraphics context, int mouseX, int mouseY, float delta, int rootX, int rootY);
	default @Nullable List<AbstractWidget> getButtons() {
		return null;
	}
	default void onNavButtonClick(SubPageSelectButton selectButton) {}
	default void markWidgetsAsVisible() {}
	default void markWidgetsAsInvisible() {}
	default void nextPage() {}
	default void previousPage() {}
}
