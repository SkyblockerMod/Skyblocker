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
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RareDropSpecialEffects {
	private static final Logger LOGGER = LoggerFactory.getLogger(RareDropSpecialEffects.class);
	private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
	private static final Pattern MAGIC_FIND_PATTERN = Pattern.compile("^(?!.*:)(?:RARE|VERY RARE|CRAZY RARE|INSANE) DROP!\\s+(?<item>.+?)(?:\\s+\\(\\+\\d+%? ✯ Magic Find\\))?$");

	@Init
	public static void init() {
		ClientReceiveMessageEvents.ALLOW_GAME.register(RareDropSpecialEffects::displayRareDropEffect);
	}

	private static boolean displayRareDropEffect(Text message, boolean overlay) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.specialEffects.rareDropEffects && !overlay) {

			try {
				String stringForm = message.getString();
				Matcher magicFindMatcher = MAGIC_FIND_PATTERN.matcher(stringForm);

				if (magicFindMatcher.matches()) {
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
			CLIENT.particleManager.addEmitter(CLIENT.player, ParticleTypes.SCRAPE, 30);
			CLIENT.gameRenderer.showFloatingItem(stack);
		}
	}

	private static @Nullable ItemStack getStackFromName(String itemName) {
		String itemId = switch (itemName) {
			//Slayer
			//Zombie
			case "Scythe Blade" -> "SCYTHE_BLADE";
			case "Shard Of The Shredded" -> "SHARD_OF_THE_SHREDDED";
			case "Severed Hand" -> "SEVERED_HAND";
			case "Warden Heart" -> "WARDEN_HEART";
			//Spider
			case "Shriveled Wasp" -> "SHRIVELED_WASP";
			case "Digested Mosquito" -> "DIGESTED_MOSQUITO";
			case "Ensnared Snail" -> "ENSNARED_SNAIL";
			case "Primordial Eye" -> "PRIMORDIAL_EYE";
			//Wolf
			case "Overflux Capacitor" -> "OVERFLUX_CAPACITOR";
			//Enderman
			case "End Stone Idol" -> "END_STONE_IDOL";
			case "Judgement Core" -> "JUDGEMENT_CORE";
			//Blaze
			case "High Class Archfiend Dice" -> "HIGH_CLASS_ARCHFIEND_DICE";

			//Fishing
			case "Radioactive Vial" -> "RADIOACTIVE_VIAL";
			case "Tiki Mask" -> "TIKI_MASK";
			case "Titanoboa Shed" -> "TITANOBOA_SHED";

			default -> "NONE";
		};

		return ItemRepository.getItemStack(itemId);
	}
}
