package de.hysky.skyblocker;

import java.util.concurrent.TimeUnit;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.CountdownComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PopupScreen;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.CommonColors;

public final class DisableAllPopup {
	private static final long DELAY_S = 5;
	private static final long DELAY_MS = DELAY_S * 1000;
	private static final SystemToast.SystemToastId TOAST_ID = new SystemToast.SystemToastId();
	private final long end;

	public DisableAllPopup() {
		this.end = System.currentTimeMillis() + DELAY_MS;
	}

	public void open(Screen parent) {
		Component confirmPrefix = Component.translatable("text.skyblocker.confirm").withColor(CommonColors.RED).withStyle(style -> style.withBold(true));
		Component confirmComponent = new CountdownComponent(DELAY_S, TimeUnit.SECONDS, confirmPrefix, Style.EMPTY);
		PopupScreen.Builder builder = new PopupScreen.Builder(parent, Component.translatable("skyblocker.disableAll.popup.title"))
				.setMessage(Component.translatable("skyblocker.disableAll.popup.warning", Component.translatable("skyblocker.disableAll.popup.seriousWarning").withColor(CommonColors.SOFT_RED)))
				.addButton(confirmComponent, this::tryReset)
				.addButton(CommonComponents.GUI_CANCEL, PopupScreen::onClose);

		Minecraft.getInstance().setScreen(builder.build());
	}

	private void tryReset(PopupScreen popupScreen) {
		if (System.currentTimeMillis() >= this.end) {
			Minecraft minecraft = Minecraft.getInstance();

			try {
				SkyblockerConfigManager.update(config -> {
					try {
						DisableAll.disableBooleans(config);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				});

				popupScreen.onClose();
				minecraft.setScreen(SkyblockerConfigManager.createGUI(null));
				SystemToast.add(minecraft.getToastManager(), TOAST_ID, Component.translatable("skyblocker.disableAll.toast.title"), Component.translatable("skyblocker.disableAll.success").withStyle(ChatFormatting.RED));
			} catch (Exception e) {
				DisableAll.LOGGER.error("[Skyblocker DisableAll] Failed to disable all features", e);
				SystemToast.add(minecraft.getToastManager(), TOAST_ID, Component.translatable("skyblocker.disableAll.toast.title"), Component.translatable("skyblocker.disableAll.failed").withStyle(ChatFormatting.RED));
			}
		}
	}
}
