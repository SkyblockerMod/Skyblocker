package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class HealingMelonIndicator {
    private static Title title = null;
    public static void updateHealth(MinecraftClient client) {
        if(title == null)
            new Title(I18n.translate("skyblocker.rift.healNow"), Formatting.DARK_RED.getColorValue());

        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableHealingMelonIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !Utils.getLocation().contains("Stillgore Château")) {
            title.active = false;
            return;
        }
        title.active = true;
        ClientPlayerEntity player = client.player;
        if (player != null && player.getHealth() <= SkyblockerConfig.get().slayer.vampireSlayer.healingMelonHealthThreshold * 2F) {
            title.active = true;
            if(!TitleContainer.titles.contains(title))
                RenderHelper.displayInTitleContainerAndPlaySound(title);
        }
        else {
            title.active = false;
        }
    }
}