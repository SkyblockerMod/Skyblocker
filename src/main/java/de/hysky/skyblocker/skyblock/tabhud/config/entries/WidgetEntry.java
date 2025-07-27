package de.hysky.skyblocker.skyblock.tabhud.config.entries;

import de.hysky.skyblocker.skyblock.tabhud.widget.HudWidget;
import de.hysky.skyblocker.utils.Location;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.util.Colors;

import java.util.List;

public class WidgetEntry extends WidgetsListEntry {
	private final HudWidget widget;
	private final Location currentLocation;
	private final ButtonWidget enableButton;

	public WidgetEntry(HudWidget widget, Location currentLocation) {
		this.widget = widget;
		this.currentLocation = currentLocation;

		boolean enabled = widget.isEnabledIn(currentLocation);
		enableButton = ButtonWidget.builder(enabled ? ENABLED_TEXT : DISABLED_TEXT, button -> {
					boolean enabledIn = this.widget.isEnabledIn(this.currentLocation);
					this.widget.setEnabledIn(currentLocation, !enabledIn);
					button.setMessage(!enabledIn ? ENABLED_TEXT : DISABLED_TEXT);
				})
				.size(64, 12)
				.build();
	}

	@Override
	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {

	}

	@Override
	public List<? extends Element> children() {
		return List.of(enableButton);
	}

	@Override
	public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
		int textY = y + (entryHeight - 9) / 2;
		enableButton.setPosition(x + entryWidth - 110, y + (entryHeight - 12) / 2);
		enableButton.render(context, mouseX, mouseY, tickDelta);
		context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, widget.getDisplayName(), x + 2, textY, Colors.WHITE);
	}
}
