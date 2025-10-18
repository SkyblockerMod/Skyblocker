package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

public class QuiverWarning {
    @Nullable
    private static Type warning = null;

    @Init
    public static void init() {
        ClientReceiveMessageEvents.ALLOW_GAME.register(QuiverWarning::onChatMessage);
        Scheduler.INSTANCE.scheduleCyclic(QuiverWarning::update, 10);
    }

    public static boolean onChatMessage(Text text, boolean overlay) {
        String message = text.getString();
        if (SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarning && message.endsWith("left in your Quiver!")) {
            MinecraftClient.getInstance().inGameHud.setDefaultTitleFade();
            if (message.startsWith("You only have 50")) {
                onChatMessage(Type.FIFTY_LEFT);
            } else if (message.startsWith("You only have 10")) {
                onChatMessage(Type.TEN_LEFT);
            } else if (message.startsWith("You don't have any more")) {
                onChatMessage(Type.EMPTY);
            }
        }
        return true;
    }

    private static void onChatMessage(Type warning) {
        if (!Utils.isInDungeons()) {
            MinecraftClient.getInstance().inGameHud.setTitle(Text.translatable(warning.key).formatted(Formatting.RED));
        } else if (SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarningInDungeons) {
            MinecraftClient.getInstance().inGameHud.setTitle(Text.translatable(warning.key).formatted(Formatting.RED));
            QuiverWarning.warning = warning;
        }
    }

    public static void update() {
        if (warning != null && SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarning && SkyblockerConfigManager.get().general.quiverWarning.enableQuiverWarningAfterDungeon && !Utils.isInDungeons()) {
            InGameHud inGameHud = MinecraftClient.getInstance().inGameHud;
            inGameHud.setDefaultTitleFade();
            inGameHud.setTitle(Text.translatable(warning.key).formatted(Formatting.RED));
            warning = null;
        }
    }

    private enum Type {
        NONE(""),
        FIFTY_LEFT("50Left"),
        TEN_LEFT("10Left"),
        EMPTY("empty");
        private final String key;

        Type(String key) {
            this.key = "skyblocker.quiverWarning." + key;
        }
    }
}
