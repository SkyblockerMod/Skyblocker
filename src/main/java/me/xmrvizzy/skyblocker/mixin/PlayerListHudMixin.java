package me.xmrvizzy.skyblocker.mixin;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.tabhud.TabHud;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screenbuilder.ScreenMaster;
import me.xmrvizzy.skyblocker.skyblock.tabhud.util.PlayerListMgr;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.cbyrne.betterinject.annotations.Arg;
import dev.cbyrne.betterinject.annotations.Inject;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {
    @Shadow
    private Text footer;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", cancellable = true)
    public void skyblocker$renderTabHud(@Arg DrawContext context, @Arg int w, CallbackInfo info) {
        if (!Utils.isOnSkyblock() || !SkyblockerConfig.get().general.tabHud.tabHudEnabled || TabHud.defaultTgl.isPressed()) {
            return;
        }

        ClientPlayNetworkHandler nwH = MinecraftClient.getInstance().getNetworkHandler();
        if (nwH == null) {
            return;
        }

        int h = MinecraftClient.getInstance().getWindow().getScaledHeight();
        float scale = SkyblockerConfig.get().general.tabHud.tabHudScale / 100f;
        w = (int) (w / scale);
        h = (int) (h / scale);

        PlayerListMgr.updateFooter(footer);

        try {
            ScreenMaster.render(context, w,h);
            // Screen screen = Screen.getCorrect(w, h, footer);
            // screen.render(context);
            info.cancel();
        } catch (Exception e) {
            TabHud.LOGGER.error("[Skyblocker] Encountered unknown exception while drawing default hud", e);
        }
    }

}