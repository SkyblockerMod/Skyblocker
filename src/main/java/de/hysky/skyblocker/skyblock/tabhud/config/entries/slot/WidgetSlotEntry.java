package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;

import java.util.List;
import java.util.Locale;

public class WidgetSlotEntry extends WidgetsListSlotEntry {
	private final ButtonWidget editButton;
	private final State state;
	private final ButtonWidget enableButton;
	private final boolean alwaysEnabled;

	public WidgetSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		editButton = ButtonWidget.builder(Text.literal("EDIT"), button -> this.parent.clickAndWaitForServer(this.slotId, 1))
				.size(32, 12)
				.build();

		String string = icon.getName().getString().trim();
		if (string.startsWith("✔")) {
			state = State.ENABLED;
		} else if (string.startsWith("✖")) {
			state = State.DISABLED;
		} else state = State.LOCKED;
		enableButton = ButtonWidget.builder(state.equals(State.ENABLED) ? ENABLED_TEXT : DISABLED_TEXT, button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(64, 12)
				.build();
		alwaysEnabled = ItemUtils.getLoreLineIf(icon, s -> s.toLowerCase(Locale.ENGLISH).contains("always enable")) != null;
	}

	@Override
	public void renderTooltip(DrawContext context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 110 && mouseY >= y && mouseY <= y + entryHeight) {
			List<Text> lore = ItemUtils.getLore(icon);
			if (alwaysEnabled) {
				lore = lore.subList(0, Math.max(lore.size() - 2, 0));
			} else if (state != State.LOCKED) {
				lore = lore.subList(0, Math.max(lore.size() - 3, 0));
			}
			context.drawTooltip(MinecraftClient.getInstance().textRenderer, lore, mouseX, mouseY);
		}
	}

	@Override
	public List<? extends Element> children() {
		return alwaysEnabled ? List.of(editButton) : List.of(editButton, enableButton);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int textY = this.getY() + (this.getHeight() - 9) / 2;
		TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		if (state != State.LOCKED) {

			editButton.setPosition(this.getX() + this.getWidth() - 40, this.getY() + (this.getHeight() - 12) / 2);
			editButton.render(context, mouseX, mouseY, deltaTicks);

			if (!alwaysEnabled) {
				enableButton.setPosition(this.getX() + this.getWidth() - 110, this.getY() + (this.getHeight() - 12) / 2);
				enableButton.render(context, mouseX, mouseY, deltaTicks);
			}
		} else {
			context.drawText(textRenderer, "LOCKED", this.getX() + this.getWidth() - 50, textY, Colors.RED, true);
		}
	}

	enum State {
		ENABLED,
		DISABLED,
		LOCKED
	}
}
