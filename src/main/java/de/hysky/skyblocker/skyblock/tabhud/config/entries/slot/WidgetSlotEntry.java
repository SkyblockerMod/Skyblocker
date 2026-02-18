package de.hysky.skyblocker.skyblock.tabhud.config.entries.slot;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsElementList;
import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsListTab;
import de.hysky.skyblocker.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class WidgetSlotEntry extends WidgetsListSlotEntry {
	private final Button editButton;
	private final State state;
	private final Button enableButton;
	private final boolean alwaysEnabled;

	public WidgetSlotEntry(WidgetsListTab parent, int slotId, ItemStack icon) {
		super(parent, slotId, icon);
		editButton = Button.builder(Component.literal("EDIT"), button -> {
					this.parent.clickAndWaitForServer(this.slotId, 1);
					this.parent.resetScrollOnLoad();
				})
				.size(32, 12)
				.build();

		String string = icon.getHoverName().getString().trim();
		if (string.startsWith("✔")) {
			state = State.ENABLED;
		} else if (string.startsWith("✖")) {
			state = State.DISABLED;
		} else state = State.LOCKED;
		enableButton = Button.builder(state.equals(State.ENABLED) ? ENABLED_TEXT : DISABLED_TEXT, button -> this.parent.clickAndWaitForServer(this.slotId, 0))
				.size(64, 12)
				.build();
		alwaysEnabled = ItemUtils.getLoreLineIf(icon, s -> s.toLowerCase(Locale.ENGLISH).contains("always enable")) != null;
	}

	@Override
	public void renderTooltip(GuiGraphics context, int x, int y, int entryWidth, int entryHeight, int mouseX, int mouseY) {
		if (mouseX >= x && mouseX <= x + entryWidth - 110 && mouseY >= y && mouseY <= y + entryHeight) {
			@SuppressWarnings("deprecation")
			List<Component> lore = ItemUtils.getLore(icon);
			if (alwaysEnabled) {
				lore = lore.subList(0, Math.max(lore.size() - 2, 0));
			} else if (state != State.LOCKED) {
				lore = lore.subList(0, Math.max(lore.size() - 3, 0));
			}
			context.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lore, mouseX, mouseY);
		}
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return alwaysEnabled ? List.of(editButton) : List.of(editButton, enableButton);
	}

	@Override
	public void renderContent(GuiGraphics context, int mouseX, int mouseY, boolean hovered, float deltaTicks) {
		int textY = this.getY() + (this.getHeight() - 9) / 2;
		Font textRenderer = Minecraft.getInstance().font;
		renderIconAndText(context, this.getY(), this.getX(), this.getHeight());
		if (state != State.LOCKED) {

			editButton.setPosition(this.getX() + this.getWidth() - 40, this.getY() + (this.getHeight() - 12) / 2);
			editButton.render(context, mouseX, mouseY, deltaTicks);

			if (!alwaysEnabled) {
				enableButton.setPosition(this.getX() + this.getWidth() - 110, this.getY() + (this.getHeight() - 12) / 2);
				enableButton.render(context, mouseX, mouseY, deltaTicks);
			}
		} else {
			context.drawString(textRenderer, "LOCKED", this.getX() + this.getWidth() - 50, textY, CommonColors.RED, true);
		}
	}

	private static void addToast(Component message) {
		Minecraft.getInstance().getToastManager().addToast(new SystemToast(WidgetsListTab.SYSTEM_TOAST_ID, message, null));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
		if (super.mouseClicked(event, doubled) || parent.isWaitingForServer()) return true;
		boolean isSelect = event.button() == 0;
		if (isSelect && state != State.ENABLED) {
			addToast(Component.translatable("skyblocker.uiAndVisuals.tabHud.widgetsScreen.toast.mustEnableWidget"));
			return false;
		}

		int relativePosition = slotId - 18 - 1;
		relativePosition -= 2 * (relativePosition / 9);

		if (WidgetsElementList.editingPosition == relativePosition) return false;
		if (isSelect && relativePosition == 0 && !WidgetsElementList.isOnSecondPage) {
			addToast(Component.translatable("skyblocker.uiAndVisuals.tabHud.widgetsScreen.toast.unselectableWidget"));
			return false;
		} else if (!isSelect && relativePosition > WidgetsElementList.maxPosition) {
			addToast(Component.translatable("skyblocker.uiAndVisuals.tabHud.widgetsScreen.toast.cannotMoveHere"));
			return true;
		}

		boolean isGreater = WidgetsElementList.editingPosition > relativePosition;
		if (isSelect) {
			parent.clickAndWaitForServer(13, isGreater ? 1 : 0);
		} else {
			parent.shiftClickAndWaitForServer(13, isGreater ? 1 : 0);
		}

		final int remainingClicks = Math.abs(WidgetsElementList.editingPosition - relativePosition) - 1;
		if (remainingClicks == 0) return true;

		if (isSelect) {
			addToast(Component.translatable("skyblocker.uiAndVisuals.tabHud.widgetsScreen.toast.remainingClicksToSelect", remainingClicks));
		} else {
			addToast(Component.translatable("skyblocker.uiAndVisuals.tabHud.widgetsScreen.toast.remainingClicksToMove", remainingClicks));
		}
		return true;
	}

	public State getState() {
		return this.state;
	}

	public enum State {
		ENABLED,
		DISABLED,
		LOCKED
	}
}
