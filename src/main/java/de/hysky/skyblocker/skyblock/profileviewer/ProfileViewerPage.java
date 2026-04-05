package de.hysky.skyblocker.skyblock.profileviewer;

import de.hysky.skyblocker.skyblock.profileviewer.utils.SubPageSelectButton;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import org.jspecify.annotations.Nullable;

public interface ProfileViewerPage {
	void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, int rootX, int rootY);
	default @Nullable List<AbstractWidget> getButtons() {
		return null;
	}
	default void onNavButtonClick(SubPageSelectButton selectButton) {}
	default void markWidgetsAsVisible() {}
	default void markWidgetsAsInvisible() {}
	default void nextPage() {}
	default void previousPage() {}
}
