package de.hysky.skyblocker.skyblock.special;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.FlexibleItemStack;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;

public class TrophySpecialEffects {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern DIAMOND_PATTERN = Pattern.compile("^NEW DISCOVERY:\\s+(?<item>.+?)\\s+DIAMOND$");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(TrophySpecialEffects::displayRareDropEffect);
	}

	private static boolean displayRareDropEffect(Component message, boolean overlay) {
		if (!Utils.isOnSkyblock() || overlay || !SkyblockerConfigManager.get().general.specialEffects.trophyDropEffects) {
			return true;
		}

		try {
			String stringForm = message.getString();
			Matcher matcher = DIAMOND_PATTERN.matcher(stringForm);

			if (matcher.matches()) {
				FlexibleItemStack stack = getStackFromName(matcher.group("item"));

				if (stack != null && stack.getStack() != null && !stack.getStackOrThrow().isEmpty()) {
					CLIENT.particleEngine.createTrackingEmitter(CLIENT.player, ParticleTypes.PORTAL, 30);
					CLIENT.gameRenderer.displayItemActivation(stack.getStackOrThrow());
				}
			}
		} catch (Exception e) { // In case there's a regex failure or something else bad happens
			LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered!", e);
		}

		return true;
	}

	private static @Nullable FlexibleItemStack getStackFromName(String itemName) {
		String itemId = switch (itemName) {

			// Fish
			case "Blobfish" -> "BLOBFISH_DIAMOND";
			case "Flyfish" -> "FLYFISH_DIAMOND";
			case "Golden Fish" -> "GOLDEN_FISH_DIAMOND";
			case "Gusher" -> "GUSHER_DIAMOND";
			case "Karate Fish" -> "KARATE_FISH_DIAMOND";
			case "Lava Horse" -> "LAVA_HORSE_DIAMOND";
			case "Mana Ray" -> "MANA_RAY_DIAMOND";
			case "Moldfin" -> "MOLDFIN_DIAMOND";
			case "Skeleton Fish" -> "SKELETON_FISH_DIAMOND";
			case "Slugfish" -> "SLUGFISH_DIAMOND";
			case "Soul Fish" -> "SOUL_FISH_DIAMOND";
			case "Steaming Hot Flounder" -> "STEAMING_HOT_FLOUNDER_DIAMOND";
			case "Sulphur Skitter" -> "SULPHUR_SKITTER_DIAMOND";
			case "Vanille" -> "VANILLE_DIAMOND";
			case "Volcanic Stonefish" -> "VOLCANIC_STONEFISH_DIAMOND";
			case "Obfuscated-1" -> "OBFUSCATED_FISH_1_DIAMOND";
			case "Obfuscated-2" -> "OBFUSCATED_FISH_2_DIAMOND";
			case "Obfuscated-3" -> "OBFUSCATED_FISH_3_DIAMOND";

			//Frogs
			case "Common Frog" -> "COMMON_FROG_DIAMOND";
			case "Exploding Frog" -> "EXPLODING_FROG_DIAMOND";
			case "Leap Frog" -> "LEAP_FROG_DIAMOND";
			case "Wetlands Frog" -> "WETLANDS_FROG_DIAMOND";
			case "Reality Hopper" -> "REALITY_HOPPER_DIAMOND";
			case "Blessed Frog" -> "BLESSED_FROG_DIAMOND";
			case "Bullfrog" -> "BULLFROG_DIAMOND";
			case "Sea Frog" -> "SEA_FROG_DIAMOND";
			case "Cave Frog" -> "CAVE_FROG_DIAMOND";
			case "Highlands Frog" -> "HIGHLANDS_FROG_DIAMOND";
			case "Tree Frog" -> "TREE_FROG_DIAMOND";
			case "Puddle Jumper" -> "PUDDLE_JUMPER_DIAMOND";

			default -> "NONE";
		};

		return ItemRepository.getItemStack(itemId);
	}
}
