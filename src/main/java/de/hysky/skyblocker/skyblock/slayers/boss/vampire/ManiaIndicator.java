package de.hysky.skyblocker.skyblock.slayers.boss.vampire;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerManager;
import de.hysky.skyblocker.skyblock.slayers.SlayerType;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.block.Blocks;

public class ManiaIndicator {
	private static final Title title = new Title("skyblocker.rift.mania", ChatFormatting.RED);

	public static void updateMania() {
		Minecraft client = Minecraft.getInstance();

		if (!SkyblockerConfigManager.get().slayers.vampireSlayer.enableManiaIndicator || !SlayerManager.isFightingSlayerType(SlayerType.VAMPIRE) || client.player == null || client.level == null) {
			TitleContainer.removeTitle(title);
			return;
		}

		Entity slayerEntity = SlayerManager.getSlayerArmorStand();
		if (slayerEntity == null) return;

		boolean anyMania = false;
		for (ArmorStand armorStandEntity : SlayerManager.getEntityArmorStands(slayerEntity, 2.5f)) {
			if (armorStandEntity.getName().toString().contains("MANIA")) {
				anyMania = true;
				BlockPos pos = client.player.blockPosition().below();
				boolean isGreen = client.level.getBlockState(pos).getBlock() == Blocks.GREEN_TERRACOTTA;
				title.setText(Component.translatable("skyblocker.rift.mania").withStyle(isGreen ? ChatFormatting.GREEN : ChatFormatting.RED));
				TitleContainer.addTitleAndPlaySound(title);
			}
		}
		if (!anyMania) {
			TitleContainer.removeTitle(title);
		}
	}
}
