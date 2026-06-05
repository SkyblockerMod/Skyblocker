package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class QuickNavMixin extends QuickNavScreenMixin {
	@Unique
	private @Nullable List<QuickNavButton> quickNavButtons;

	@Inject(method = "init()V", at = @At(value = "TAIL"))
	protected void skyblocker$initQuickNav(CallbackInfo ci) {
		Screen instance = (Screen) (Object) this;
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && Minecraft.getInstance().player != null && !Minecraft.getInstance().player.isCreative()) {
			for (QuickNavButton quickNavButton : this.quickNavButtons = QuickNav.init(instance.getTitle().getString().trim())) {
				instance.addWidget(quickNavButton);
			}
		}
	}

	/**
	 * Draws the unselected tabs in front of the background, but behind the main inventory, similar to creative inventory tabs.
	 */
	@Override
	protected void skyblocker$extractUnselectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.quickNavButtons != null) for (QuickNavButton quickNavButton : this.quickNavButtons) {
			// Render the button behind the main inventory background if it's not toggled or if it's still fading in
			if (!quickNavButton.toggled() || quickNavButton.getAlpha() < 255) {
				quickNavButton.setRenderInFront(false);
				quickNavButton.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}

	/**
	 * Draws the selected tab in front of the background and the main inventory, similar to creative inventory tabs.
	 */
	@Override
	protected void skyblocker$extractSelectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.quickNavButtons != null) for (QuickNavButton quickNavButton : this.quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}
}
