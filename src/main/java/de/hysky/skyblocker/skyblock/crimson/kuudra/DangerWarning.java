package de.hysky.skyblocker.skyblock.crimson.kuudra;

import java.util.function.Supplier;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class DangerWarning {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Supplier<MutableComponent> DANGER_TEXT = () -> Component.translatable("skyblocker.crimson.kuudra.danger");
	private static final Title TITLE = new Title(DANGER_TEXT.get());

	@Init
	public static void init() {
		Scheduler.INSTANCE.scheduleCyclic(DangerWarning::updateIndicator, 5);
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> reset());
	}

	private static void updateIndicator() {
		if (Utils.isInKuudra() && SkyblockerConfigManager.get().crimsonIsle.kuudra.dangerWarning && CLIENT.player != null && CLIENT.level != null) {
			for (int i = 1; i <= 5; i++) {
				BlockPos under = CLIENT.player.blockPosition().below(i);
				Title title = getDangerTitle(under);

				if (title != null) {
					TitleContainer.addTitleAndPlaySound(title);

					return;
				} else if (i == 5) { //Prevent removing the title prematurely
					TitleContainer.removeTitle(TITLE);
				}
			}
		}
	}

	private static Title getDangerTitle(BlockPos pos) {
		BlockState state = CLIENT.level.getBlockState(pos);
		Block block = state.getBlock();

		int argb = switch (block) {
			case Block b when b == Blocks.GREEN_TERRACOTTA -> DyeColor.GREEN.getTextureDiffuseColor();
			case Block b when b == Blocks.LIME_TERRACOTTA -> DyeColor.LIME.getTextureDiffuseColor();
			case Block b when b == Blocks.YELLOW_TERRACOTTA -> DyeColor.YELLOW.getTextureDiffuseColor();
			case Block b when b == Blocks.ORANGE_TERRACOTTA -> DyeColor.ORANGE.getTextureDiffuseColor();
			case Block b when b == Blocks.PINK_TERRACOTTA -> DyeColor.PINK.getTextureDiffuseColor();
			case Block b when b == Blocks.RED_TERRACOTTA -> DyeColor.RED.getTextureDiffuseColor();

			default -> 0;
		};

		return argb != 0 ? TITLE.setText(DANGER_TEXT.get().withColor(argb & 0x00FFFFFF)) : null;
	}

	private static void reset() {
		TitleContainer.removeTitle(TITLE);
	}
}
