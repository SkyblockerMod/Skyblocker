package me.xmrvizzy.skyblocker.skyblock.rift;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.SlayerUtils;
import me.xmrvizzy.skyblocker.utils.Utils;
import me.xmrvizzy.skyblocker.utils.title.Title;
import me.xmrvizzy.skyblocker.utils.title.TitleContainer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.Formatting;

public class ManiaIndicator {
    private static Title title = null;
    public static void updateMania() {
        if(title == null)
            title = new Title("b", Formatting.RED.getColorValue());

        if (!SkyblockerConfig.get().slayer.vampireSlayer.enableManiaIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getLocation().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
            title.active = false;
            return;
        }

        Entity slayerEntity = SlayerUtils.getSlayerEntity();
        if (slayerEntity == null) return;

        boolean anyMania = false;
        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity)) {
            if (entity.getDisplayName().toString().contains("MANIA")) {
                anyMania = true;
                title.active = true;
                var pos = MinecraftClient.getInstance().player.getBlockPos().down();
                var isGreen = MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == Blocks.GREEN_TERRACOTTA;
                title.color = isGreen ? Formatting.GREEN.getColorValue() : Formatting.RED.getColorValue();
                if(!TitleContainer.titles.contains(title)) {
                    title.text = I18n.translate("skyblocker.rift.mania");
                    RenderHelper.displayInTitleContainerAndPlaySound(title);
                }
            }
        }
        if(!anyMania)
            title.active = false;
    }
}