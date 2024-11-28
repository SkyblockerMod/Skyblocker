package de.hysky.skyblocker.skyblock.special;

import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DyeSpecialEffects {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	@VisibleForTesting
	protected static final Pattern DROP_PATTERN = Pattern.compile("WOW! (?:\\[[A-Z+]+\\] )?(?<player>[A-Za-z0-9_]+) found (?<dye>[A-Za-z ]+ Dye)(?: #[\\d,]+)?!");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.GAME.register(DyeSpecialEffects::displayDyeDropEffect);
	}

	private static void displayDyeDropEffect(Text message, boolean overlay) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.specialEffects.rareDyeDropEffects && !overlay) {
			try {
				String stringForm = message.getString();
				Matcher matcher = DROP_PATTERN.matcher(stringForm);

				if (matcher.matches() && matcher.group("player").equals(CLIENT.getSession().getUsername())) {
					ItemStack stack = findDyeStack(matcher.group("dye"));

					if (!stack.isEmpty()) {
						CLIENT.particleManager.addEmitter(CLIENT.player, ParticleTypes.TRIAL_SPAWNER_DETECTION, 30);
						CLIENT.gameRenderer.showFloatingItem(stack);
					}
				}
			} catch (Exception e) {
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered!", e);
			}
		}
	}

	private static ItemStack findDyeStack(String dyeName) {
		Optional<ItemStack> dye = ItemRepository.getItemsStream()
				.filter(stack -> stack.getName().getString().equals(dyeName))
				.findFirst();

		return dye.orElse(ItemStack.EMPTY);
	}
}
