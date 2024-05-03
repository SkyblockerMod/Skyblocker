package de.hysky.skyblocker.skyblock.crimson.blazeslayer;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.SlayerUtils;
import de.hysky.skyblocker.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FirePillarAnnouncer {
    public static void Announce(BlockPos pos, BlockState state) {
        if (SkyblockerConfigManager.get().slayer.blazeSlayer.enableFirePillarAnnouncer && Utils.isInCrimsonIsle() && SlayerUtils.isInSlayer()) {
            Entity SlayerEntity = SlayerUtils.getSlayerEntity();
            if (SlayerEntity != null) {
                LivingEntity slayer = (LivingEntity) SlayerUtils.getSlayerEntity();

                if (state.isOf(Blocks.BROWN_TERRACOTTA) && slayer.getBlockPos().isWithinDistance(pos, 20)) {
                    MinecraftClient client = MinecraftClient.getInstance();
                    client.inGameHud.setTitleTicks(5, 20, 10);
                    client.inGameHud.setTitle(Text.literal("Fire Pillar Spawned!").formatted(Formatting.GOLD));
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(), 100f, 0.1f);
                }
            }
        }
    }
}
