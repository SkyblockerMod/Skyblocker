package de.hysky.skyblocker.mixins;

import java.util.List;

import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.museum.MuseumManager;
import de.hysky.skyblocker.skyblock.quicknav.QuickNav;
import de.hysky.skyblocker.skyblock.quicknav.QuickNavButton;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

@Mixin(ContainerScreen.class)
public abstract class ContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {
	@Shadow
	@Final
	private static Identifier CONTAINER_BACKGROUND;
	@Unique
	private @Nullable List<QuickNavButton> quickNavButtons;

	public ContainerScreenMixin(ChestMenu handler, Inventory inventory, Component title) {
		super(handler, inventory, title);
	}

	@SuppressWarnings("unused")
	@Override
	protected void init() {
		super.init();

		// Init Quick Nav
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().quickNav.enableQuickNav && this.minecraft.player != null && !this.minecraft.player.isCreative()) {
			for (QuickNavButton quickNavButton : this.quickNavButtons = QuickNav.init(this.getTitle().getString().trim())) {
				this.addWidget(quickNavButton);
			}
		}

		// Museum Overlay
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.museumOverlay && this.minecraft.player != null && this.getTitle().getString().contains("Museum")) {
			int overlayWidth = MuseumManager.BACKGROUND_WIDTH; // width of the overlay
			int spacing = MuseumManager.SPACING; // space between inventory and overlay

			// Default: center inventory
			int inventoryX = (this.width - this.imageWidth) / 2;

			// If overlay would go off the right edge, shift inventory left
			if (inventoryX + this.imageWidth + spacing + overlayWidth > this.width) {
				inventoryX = this.width - (this.imageWidth + overlayWidth + spacing);
				if (inventoryX < 0) inventoryX = 0;
			}
			this.leftPos = inventoryX;

			new MuseumManager(this, this.leftPos, this.topPos, this.imageWidth);
		}
	}

	// The following two mixins are needed for the Accessory Helper & Museum Overlay

	@ModifyVariable(method = "extractBackground", at = @At("STORE"), name = "xo")
	public int x(int ignored) {
		return this.leftPos;
	}

	@ModifyVariable(method = "extractBackground", at = @At("STORE"), name = "yo")
	public int y(int ignored) {
		return this.topPos;
	}

	/**
	 * Draws the unselected tabs in front of the background, but behind the main inventory, similar to creative inventory tabs.
	 */
	@Inject(method = "extractBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V", ordinal = 0))
	private void skyblocker$extractUnselectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
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
	@Inject(method = "extractBackground", at = @At("RETURN"))
	private void skyblocker$extractSelectedQuickNavButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo ci) {
		if (this.quickNavButtons != null) for (QuickNavButton quickNavButton : this.quickNavButtons) {
			if (quickNavButton.toggled()) {
				quickNavButton.setRenderInFront(true);
				quickNavButton.extractRenderState(graphics, mouseX, mouseY, a);
			}
		}
	}
}
