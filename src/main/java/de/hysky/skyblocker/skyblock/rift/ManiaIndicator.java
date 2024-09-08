package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ManiaIndicator {
    private static final Title title = new Title("skyblocker.rift.mania", Formatting.RED);

    protected static void updateMania() {
        if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableManiaIndicator || !Utils.isOnSkyblock() || !Utils.isInTheRift() || !(Utils.getIslandArea().contains("Stillgore Château")) || !SlayerUtils.isInSlayer()) {
            TitleContainer.removeTitle(title);
            return;
        }

        Entity slayerEntity = SlayerUtils.getSlayerArmorStandEntity();
        if (slayerEntity == null) return;

        boolean anyMania = false;
        for (Entity entity : SlayerUtils.getEntityArmorStands(slayerEntity, 2.5f)) {
            if (entity.getDisplayName().toString().contains("MANIA")) {
                anyMania = true;
                BlockPos pos = MinecraftClient.getInstance().player.getBlockPos().down();
                boolean isGreen = MinecraftClient.getInstance().world.getBlockState(pos).getBlock() == Blocks.GREEN_TERRACOTTA;
                title.setText(Text.translatable("skyblocker.rift.mania").formatted(isGreen ? Formatting.GREEN : Formatting.RED));
                RenderHelper.displayInTitleContainerAndPlaySound(title);
            }
        }
        if (!anyMania) {
            TitleContainer.removeTitle(title);
        }
    }
}
