package de.hysky.skyblocker.mixins;

import com.mojang.authlib.GameProfile;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.auction.AuctionViewScreen;
import de.hysky.skyblocker.skyblock.auction.EditBidPopup;
import de.hysky.skyblocker.skyblock.dungeon.partyfinder.PartyFinderScreen;
import de.hysky.skyblocker.skyblock.item.HotbarSlotLock;
import de.hysky.skyblocker.skyblock.item.ItemProtection;
import de.hysky.skyblocker.skyblock.rift.HealingMelonIndicator;
import de.hysky.skyblocker.skyblock.searchoverlay.OverlayScreen;
import de.hysky.skyblocker.skyblock.searchoverlay.SearchOverManager;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin extends AbstractClientPlayerEntity {
    @Shadow
    @Final
    protected MinecraftClient client;

    public ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "dropSelectedItem", at = @At("HEAD"), cancellable = true)
    public void skyblocker$dropSelectedItem(CallbackInfoReturnable<Boolean> cir) {
        if (Utils.isOnSkyblock() && (ItemProtection.isItemProtected(this.getInventory().getMainHandStack()) || HotbarSlotLock.isLocked(this.getInventory().selectedSlot))
                && (!SkyblockerConfigManager.get().dungeons.allowDroppingProtectedItems || !Utils.isInDungeons())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "updateHealth", at = @At("RETURN"))
    public void skyblocker$updateHealth(CallbackInfo ci) {
        HealingMelonIndicator.updateHealth();
    }

    @Inject(method = "openEditSignScreen", at = @At("HEAD"), cancellable = true)
    public void skyblocker$redirectEditSignScreen(SignBlockEntity sign, boolean front, CallbackInfo ci) {
        // Fancy Party Finder
        if (!PartyFinderScreen.isInKuudraPartyFinder && client.currentScreen instanceof PartyFinderScreen partyFinderScreen && !partyFinderScreen.isAborted() && sign.getText(front).getMessage(3, false).getString().toLowerCase().contains("level")) {
            partyFinderScreen.updateSign(sign, front);
            ci.cancel();
            return;
        }

        if (client.currentScreen instanceof AuctionViewScreen auctionViewScreen) {
            this.client.setScreen(new EditBidPopup(auctionViewScreen, sign, front, auctionViewScreen.minBid));
            ci.cancel();
        }

        // Search Overlay
        if (client.currentScreen != null) {
            if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableAuctionHouse && client.currentScreen.getTitle().getString().toLowerCase().contains("auction")) {
                if (sign.getText(front).getMessage(3, false).getString().equalsIgnoreCase("enter query")) {
                    SearchOverManager.updateSign(sign, front, true);
                    client.setScreen(new OverlayScreen(Text.of("")));
                    ci.cancel();
                }
            } else if (SkyblockerConfigManager.get().uiAndVisuals.searchOverlay.enableBazaar && client.currentScreen.getTitle().getString().toLowerCase().contains("bazaar")) {
                if (sign.getText(front).getMessage(3, false).getString().equalsIgnoreCase("enter query")) {
                    SearchOverManager.updateSign(sign, front, false);
                    client.setScreen(new OverlayScreen(Text.of("")));
                    ci.cancel();
                }
            }
        }
    }
}