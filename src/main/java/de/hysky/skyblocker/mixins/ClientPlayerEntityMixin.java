package de.hysky.skyblocker.mixins;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.UIAndVisualsConfig;
import de.hysky.skyblocker.skyblock.auction.AuctionViewScreen;
import de.hysky.skyblocker.skyblock.auction.EditBidPopup;
import de.hysky.skyblocker.skyblock.dungeon.DungeonScore;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.rift.HealingMelonIndicator;
import de.hysky.skyblocker.skyblock.searchoverlay.OverlayScreen;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;
import de.hysky.skyblocker.utils.Utils;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayer {
	@Shadow
	@Final
	protected Minecraft minecraft;

	public ClientPlayerEntityMixin(ClientLevel world, GameProfile profile) {
		super(world, profile);
	}

	@Inject(method = "drop(Z)Z", at = @At("HEAD"), cancellable = true)
	public void skyblocker$dropSelectedItem(CallbackInfoReturnable<Boolean> cir) {
		if (Utils.isOnSkyblock() && (ItemProtection.isItemProtected(this.getMainHandItem()) || HotbarSlotLock.isLocked(this.getInventory().getSelectedSlot()))
				&& (!SkyblockerConfigManager.get().dungeons.allowDroppingProtectedItems || !DungeonScore.isDungeonStarted())) {
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "hurtTo", at = @At("RETURN"))
	public void skyblocker$updateHealth(CallbackInfo ci) {
		HealingMelonIndicator.updateHealth();
	}

	@Inject(method = "openTextEdit", at = @At("HEAD"), cancellable = true)
	public void skyblocker$redirectEditSignScreen(SignBlockEntity sign, boolean front, CallbackInfo ci) {
		// Fancy Party Finder
		if (!PartyFinderScreen.isInKuudraPartyFinder && minecraft.screen instanceof PartyFinderScreen partyFinderScreen && !partyFinderScreen.isAborted() && sign.getText(front).getMessage(3, false).getString().toLowerCase(Locale.ENGLISH).contains("level")) {
			partyFinderScreen.updateSign(sign, front);
			ci.cancel();
			return;
		}

		if (minecraft.screen instanceof AuctionViewScreen auctionViewScreen) {
			this.minecraft.setScreen(new EditBidPopup(auctionViewScreen, sign, front, auctionViewScreen.minBid));
			ci.cancel();
		}

		// Search Overlay
		if (minecraft.screen != null) {
			UIAndVisualsConfig.SearchOverlay config = SkyblockerConfigManager.get().uiAndVisuals.searchOverlay;
			boolean isInputSign = sign.getText(front).getMessage(3, false).getString().equalsIgnoreCase("enter query");
			if (!isInputSign) return;

			if (config.enableAuctionHouse && minecraft.screen.getTitle().getString().toLowerCase(Locale.ENGLISH).contains("auction")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.AUCTION);
				minecraft.setScreen(new OverlayScreen());
				ci.cancel();
			} else if (config.enableBazaar && minecraft.screen.getTitle().getString().toLowerCase(Locale.ENGLISH).contains("bazaar")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.BAZAAR);
				minecraft.setScreen(new OverlayScreen());
				ci.cancel();
			} else if (config.enableMuseum && minecraft.screen.getTitle().getString().contains("Museum")) {
				SearchOverManager.updateSign(sign, front, SearchOverManager.SearchLocation.MUSEUM);
				minecraft.setScreen(new OverlayScreen());
				ci.cancel();
			}
		}
	}
}
