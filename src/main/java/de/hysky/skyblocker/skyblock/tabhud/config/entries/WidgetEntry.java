package de.hysky.skyblocker.skyblock.tabhud.config.entries;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.util.CommonColors;

public class WidgetEntry extends WidgetsListEntry {
	private final HudWidget widget;
	private final Location currentLocation;
	private final Button enableButton;

	public WidgetEntry(HudWidget widget, Location currentLocation) {
		this.widget = widget;
		this.currentLocation = currentLocation;

		boolean enabled = widget.isEnabledIn(currentLocation);
		enableButton = Button.builder(enabled ? ENABLED_TEXT : DISABLED_TEXT, button -> {
					boolean enabledIn = this.widget.isEnabledIn(this.currentLocation);
					this.widget.setEnabledIn(currentLocation, !enabledIn);
					button.setMessage(!enabledIn ? ENABLED_TEXT : DISABLED_TEXT);
				})
				.size(64, 12)
				.build();
	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {

	}

	@Override
	public List<? extends GuiEventListener> children() {
		return List.of(enableButton);
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int textY = this.getY() + (this.getHeight() - 9) / 2;
		enableButton.setPosition(this.getX() + this.getWidth() - 110, this.getY() + (this.getHeight() - 12) / 2);
		enableButton.render(context, mouseX, mouseY, deltaTicks);
		context.drawString(Minecraft.getInstance().font, widget.getDisplayName(), this.getX() + 2, textY, CommonColors.WHITE);
	}
}
