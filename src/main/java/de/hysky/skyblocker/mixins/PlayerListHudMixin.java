package de.hysky.skyblocker.mixins;

import de.hysky.skyblocker.skyblock.tabhud.config.WidgetsConfigurationScreen;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.tabhud.TabHud;
import de.hysky.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Inject(at = @At("HEAD"), method = "render", cancellable = true)
    public void skyblocker$renderTabHud(CallbackInfo info, @Local(argsOnly = true) DrawContext context, @Local(argsOnly = true) int w) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfigManager.get().uiAndVisuals.tabHud.tabHudEnabled || TabHud.defaultTgl.isPressed() || MinecraftClient.getInstance().currentScreen instanceof WidgetsConfigurationScreen) {
            return;
        }

        info.cancel();
    }

    @Inject(at = @At("HEAD"), method = "setFooter")
    public void skblocker$updateFooter(@Nullable Text footer, CallbackInfo info) {
        PlayerListMgr.updateFooter(footer);
    }

}
