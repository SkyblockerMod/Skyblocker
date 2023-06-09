package me.xmrvizzy.skyblocker.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.skyblock.tabhud.TabHud;
import me.xmrvizzy.skyblocker.skyblock.tabhud.screens.Screen;
import me.xmrvizzy.skyblocker.utils.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;

@Environment(EnvType.CLIENT)
@Mixin(PlayerListHud.class)
public class PlayerListHudMixin {

    @Shadow
    private Text footer;

    @Inject(at = @At("HEAD"), method = "render(Lnet/minecraft/client/util/math/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreboardObjective;)V", cancellable = true)
    public void skyblocker$renderTabHud(MatrixStack ms, int scaledW, Scoreboard sb, ScoreboardObjective sbo,
            CallbackInfo info) {

        if (!Utils.isOnSkyblock()
                || !SkyblockerConfig.get().general.tabHud.tabHudEnabled
                || TabHud.defaultTgl.isPressed()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayNetworkHandler nwH = client.getNetworkHandler();
        if (nwH == null) {
            return;
        }

        int w = scaledW;
        int h = MinecraftClient.getInstance().getWindow().getScaledHeight();
        try {
            Screen screen = Screen.getCorrect(w, h, footer);
            screen.render(ms);
            info.cancel();
        } catch (Exception e) {
            TabHud.LOGGER.error("Drawing default hud. Reason: Screen exception {}", e);
        }
    }

}