package de.hysky.skyblocker.mixins;

import java.util.Locale;

import com.mojang.authlib.GameProfile;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignTextSlot;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.auction.AuctionViewScreen;
import de.hysky.skyblocker.skyblock.auction.EditBidPopup;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.rift.HealingMelonIndicator;
import de.hysky.skyblocker.skyblock.searchoverlay.OverlayScreen;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
	@Shadow
	@Final
	protected Minecraft minecraft;

	public LocalPlayerMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "hurtTo", at = @At("RETURN"))
	public void skyblocker$updateHealth(CallbackInfo ci) {
		HealingMelonIndicator.updateHealth();
	}

	@Inject(method = "openTextEdit", at = @At("HEAD"), cancellable = true)
	public void skyblocker$redirectEditSignScreen(SignBlockEntity sign, SignTextSlot slot, CallbackInfo ci) {
		boolean front = slot == SignTextSlot.FRONT;
		// Fancy Party Finder
		if (!PartyFinderScreen.isInKuudraPartyFinder && minecraft.gui.screen() instanceof PartyFinderScreen partyFinderScreen && !partyFinderScreen.isAborted() && sign.getText(slot).getMessages(false).get(3).getString().toLowerCase(Locale.ENGLISH).contains("level")) {
			partyFinderScreen.updateSign(sign, front);
			ci.cancel();
			return;
		}

		if (minecraft.gui.screen() instanceof AuctionViewScreen auctionViewScreen) {
			this.minecraft.gui.setScreen(new EditBidPopup(auctionViewScreen, sign, front, auctionViewScreen.minBid));
			ci.cancel();
		}

		// Search Overlay
		if (minecraft.gui.screen() != null) {
			UIAndVisualsConfig.SearchOverlay config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay;
			boolean isInputSign = sign.getText(slot).getMessages(false).get(3).getString().equalsIgnoreCase("enter query");
			if (!isInputSign) return;

			String title = minecraft.gui.screen().getTitle().getString();
			if (config.enableAuctionHouse && title.contains("Auction") ||
					config.enableIronmanAuctionHouse && title.contains("Cosmetics")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.AUCTION);
				minecraft.gui.setScreen(new OverlayScreen());
				ci.cancel();
			} else if (config.enableBazaar && title.contains("Bazaar")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.BAZAAR);
				minecraft.gui.setScreen(new OverlayScreen());
				ci.cancel();
			} else if (config.enableMuseum && title.contains("Museum")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.MUSEUM);
				minecraft.gui.setScreen(new OverlayScreen());
				ci.cancel();
			}
		}
	}
}
