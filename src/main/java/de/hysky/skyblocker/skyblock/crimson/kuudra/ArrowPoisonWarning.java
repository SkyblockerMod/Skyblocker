package de.hysky.skyblocker.skyblock.crimson.kuudra;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.config.configs.CrimsonIsleConfig;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.title.Title;
import de.hysky.skyblocker.utils.render.title.TitleContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Supplier;

public class ArrowPoisonWarning {
	private static final Supplier<CrimsonIsleConfig.Kuudra> CONFIG = () -> SkyblockerConfigManager.get().crimsonIsle.kuudra;
	private static final int THREE_SECONDS = 20 * 3;
	private static final Title NONE_TITLE = new Title(Text.translatable("skyblocker.crimson.kuudra.noArrowPoison").formatted(Formatting.GREEN));
	private static final Title LOW_TITLE = new Title(Text.translatable("skyblocker.crimson.kuudra.lowArrowPoison").formatted(Formatting.GREEN));

	public static void tryWarn(int newSlot) {
		//Exclude skyblock menu
		if (Utils.isInKuudra() && CONFIG.get().noArrowPoisonWarning && Kuudra.phase == KuudraPhase.DPS && newSlot != 8) {
			MinecraftClient client = MinecraftClient.getInstance();
			PlayerInventory inventory = client.player.getInventory();
			ItemStack heldItem = inventory.getStack(newSlot);

			if (heldItem.getItem() instanceof BowItem) {
				boolean hasToxicArrowPoison = false;
				int arrowPoisonAmount = 0;

				for (int i = 0; i < inventory.size(); ++i) {
					ItemStack stack = inventory.getStack(i);
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
