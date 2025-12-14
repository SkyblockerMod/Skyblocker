package de.hysky.skyblocker.skyblock.special;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DungeonsSpecialEffects {
	private static final Logger LOGGER = LoggerFactory.getLogger(DungeonsSpecialEffects.class);
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static final Pattern CROESUS_PATTERN = Pattern.compile("^\\s{3,}(?!.*:)(?:RARE REWARD!\\s+)?(?<item>.+)");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(DungeonsSpecialEffects::displayRareDropEffect);
	}

	private static boolean displayRareDropEffect(Component message, boolean overlay) {
		//We don't check if we're in dungeons because that check doesn't work in m7 which defeats the point of this
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.specialEffects.rareDungeonDropEffects && !overlay) {
			try {
				String stringForm = message.getString();
				Matcher matcher = CROESUS_PATTERN.matcher(stringForm);

				if (matcher.matches()) {
					ItemStack stack = getStackFromName(matcher.group("item"));

					if (stack != null && !stack.isEmpty()) {
						CLIENT.particleEngine.createTrackingEmitter(CLIENT.player, ParticleTypes.PORTAL, 30);
						CLIENT.gameRenderer.displayItemActivation(stack);
					}
				}
			} catch (Exception e) { //In case there's a regex failure or something else bad happens
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered: ", e);
			}
		}

		return true;
	}

	private static ItemStack getStackFromName(String itemName) {
		String itemId = switch (itemName) {
			//M7
			case "Necron Dye" -> "NECRON_DYE";
			case "Dark Claymore" -> "DARK_CLAYMORE";
			case "Necron's Handle", "Shiny Necron's Handle" -> "NECRON_HANDLE";
			case "Enchanted Book (Thunderlord VII)" -> "ENCHANTED_BOOK";
			case "Master Skull - Tier 5" -> "MASTER_SKULL_TIER_5";
			case "Shadow Warp" -> "SHADOW_WARP_SCROLL";
			case "Wither Shield" -> "WITHER_SHIELD_SCROLL";
			case "Implosion" -> "IMPLOSION_SCROLL";
			case "Fifth Master Star" -> "FIFTH_MASTER_STAR";

			//M6
			case "Giant's Sword" -> "GIANTS_SWORD";
			case "Fourth Master Star" -> "FOURTH_MASTER_STAR";

			//M5
			case "Third Master Star" -> "THIRD_MASTER_STAR";
			case "Shadow Fury" -> "SHADOW_FURY";

			//M4
			case "Second Master Star" -> "SECOND_MASTER_STAR";

			//M3
			case "First Master Star" -> "FIRST_MASTER_STAR";

			//I like money
			case "Recombobulator 3000" -> "RECOMBOBULATOR_3000";

			default -> "NONE";
		};

		return ItemRepository.getItemStack(itemId);
	}
}
