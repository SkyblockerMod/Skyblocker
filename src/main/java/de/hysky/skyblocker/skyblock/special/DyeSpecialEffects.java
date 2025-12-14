package de.hysky.skyblocker.skyblock.special;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DyeSpecialEffects {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft CLIENT = Minecraft.getInstance();
	@VisibleForTesting
	protected static final Pattern DROP_PATTERN = Pattern.compile("WOW! (?:\\[[A-Z+]+\\] )?(?<player>[A-Za-z0-9_]+) found (?<dye>[A-Za-z ]+ Dye)(?: #[\\d,]+)?!");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(DyeSpecialEffects::displayDyeDropEffect);
	}

	private static boolean displayDyeDropEffect(Component message, boolean overlay) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.specialEffects.rareDyeDropEffects && !overlay) {
			try {
				String stringForm = message.getString();
				Matcher matcher = DROP_PATTERN.matcher(stringForm);

				if (matcher.matches() && matcher.group("player").equals(CLIENT.getUser().getName())) {
					ItemStack stack = findDyeStack(matcher.group("dye"));

					if (!stack.isEmpty()) {
						CLIENT.particleEngine.createTrackingEmitter(CLIENT.player, ParticleTypes.TRIAL_SPAWNER_DETECTED_PLAYER, 30);
						CLIENT.gameRenderer.displayItemActivation(stack);
					}
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered!", e);
			}
		}

		return true;
	}

	private static ItemStack findDyeStack(String dyeName) {
		Optional<ItemStack> dye = ItemRepository.getItemsStream()
				.filter(stack -> stack.getHoverName().getString().equals(dyeName))
				.findFirst();

		return dye.orElse(ItemStack.EMPTY);
	}
}
