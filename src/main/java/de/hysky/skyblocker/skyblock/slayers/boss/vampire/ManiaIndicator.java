package de.hysky.skyblocker.skyblock.slayers.boss.vampire;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public class ManiaIndicator {
	private static final Title title = new Title("skyblocker.rift.mania", Formatting.RED);

	public static void updateMania() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableManiaIndicator || !SlayerManager.isFightingSlayerType(SlayerType.VAMPIRE) || client.player == null || client.world == null) {
			TitleContainer.removeTitle(title);
			return;
		}

		Entity slayerEntity = SlayerManager.getSlayerArmorStand();
		if (slayerEntity == null) return;

		boolean anyMania = false;
		for (ArmorStandEntity armorStandEntity : SlayerManager.getEntityArmorStands(slayerEntity, 2.5f)) {
			if (armorStandEntity.getName().toString().contains("MANIA")) {
				anyMania = true;
				BlockPos pos = client.player.getBlockPos().down();
				boolean isGreen = client.world.getBlockState(pos).getBlock() == Blocks.GREEN_TERRACOTTA;
				title.setText(Text.translatable("skyblocker.rift.mania").formatted(isGreen ? Formatting.GREEN : Formatting.RED));
				TitleContainer.addTitleAndPlaySound(title);
			}
		}
		if (!anyMania) {
			TitleContainer.removeTitle(title);
		}
	}
}
