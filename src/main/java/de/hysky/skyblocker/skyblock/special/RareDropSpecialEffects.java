package de.hysky.skyblocker.skyblock.special;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RareDropSpecialEffects {
	private static final Logger LOGGER = LoggerFactory.getLogger(RareDropSpecialEffects.class);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern DUNGEON_CHEST_PATTERN = Pattern.compile("^\\s{3,}(?!.*:)(?:RARE REWARD!\\s+)?(?<item>.+)$");
	private static final Pattern MAGIC_FIND_PATTERN = Pattern.compile("^(?!.*:)(?:RARE|CRAZY RARE|INSANE RARE) DROP!\\s+(?<item>.+?)\\s+\\(\\+\\d+ ✯ Magic Find\\)$");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(RareDropSpecialEffects::displayRareDropEffect);
	}

	private static boolean displayRareDropEffect(Text message, boolean overlay) {
		if (Utils.isOnSkyblock()
		&& SkyblockerConfigManager.get().general.specialEffects.rareDropEffects
		&& !overlay) {

		try {
			String stringForm = message.getString();
			Matcher dungeonMatcher = DUNGEON_CHEST_PATTERN.matcher(stringForm);
			Matcher magicFindMatcher = MAGIC_FIND_PATTERN.matcher(stringForm);

			if (dungeonMatcher.matches()) {
				triggerDropEffect(dungeonMatcher.group("item"));
			}

			else if (magicFindMatcher.matches()) {
				triggerDropEffect(magicFindMatcher.group("item"));
				}
			} catch (Exception e) { //In case there's a regex failure or something else bad happens
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered: ", e);
			}
		}

		return true;
	}

	private static void triggerDropEffect(String itemName) {
		ItemStack stack = getStackFromName(itemName);
			if (stack != null && !stack.isEmpty()) {
				CLIENT.particleManager.addEmitter(CLIENT.player, ParticleTypes.PORTAL, 30);
				CLIENT.gameRenderer.showFloatingItem(stack);
		}
	}

	private static ItemStack getStackFromName(String itemName) {
		String itemId = switch (itemName) {
			//Dungeon
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
