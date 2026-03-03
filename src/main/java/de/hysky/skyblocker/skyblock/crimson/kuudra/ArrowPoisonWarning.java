package de.hysky.skyblocker.skyblock.crimson.kuudra;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.CrimsonIsleConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;

public class ArrowPoisonWarning {
	private static final Supplier<CrimsonIsleConfig.Kuudra> CONFIG = () -> SkyblockerConfigManager.get().crimsonIsle.kuudra;
	private static final int THREE_SECONDS = 20 * 3;
	private static final Title NONE_TITLE = new Title(Component.translatable("skyblocker.crimson.kuudra.noArrowPoison").withStyle(ChatFormatting.GREEN));
	private static final Title LOW_TITLE = new Title(Component.translatable("skyblocker.crimson.kuudra.lowArrowPoison").withStyle(ChatFormatting.GREEN));

	public static void tryWarn(int newSlot) {
		//Exclude skyblock menu
		if (Utils.isInKuudra() && CONFIG.get().noArrowPoisonWarning && Kuudra.phase == KuudraPhase.DPS && newSlot != 8) {
			Minecraft client = Minecraft.getInstance();
			Inventory inventory = client.player.getInventory();
			ItemStack heldItem = inventory.getItem(newSlot);

			if (heldItem.getItem() instanceof BowItem) {
				boolean hasToxicArrowPoison = false;
				int arrowPoisonAmount = 0;

				for (int i = 0; i < inventory.getContainerSize(); ++i) {
					ItemStack stack = inventory.getItem(i);
					String itemId = stack.getSkyblockId();

					if (itemId.equals("TOXIC_ARROW_POISON")) {
						hasToxicArrowPoison = true;
						arrowPoisonAmount += stack.getCount();
					}
				}

				if (!hasToxicArrowPoison) {
					TitleContainer.addTitleAndPlaySound(NONE_TITLE, THREE_SECONDS);
				} else if (arrowPoisonAmount < CONFIG.get().arrowPoisonThreshold) {
					TitleContainer.addTitleAndPlaySound(LOW_TITLE, THREE_SECONDS);
				}
			}
		}
	}
}
