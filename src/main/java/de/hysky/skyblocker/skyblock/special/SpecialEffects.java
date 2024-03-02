package de.hysky.skyblocker.skyblock.special;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.itemlist.ItemRepository;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;

public class SpecialEffects {
	private static final Logger LOGGER = LoggerFactory.getLogger(SpecialEffects.class);
	private static final Pattern DROP_PATTERN = Pattern.compile("(?:\\[[A-Z+]+] )?(?<player>[A-Za-z0-9_]+) unlocked (?<item>.+)!");

	public static void init() {
		ClientReceiveMessageEvents.GAME.register(SpecialEffects::displayRareDropEffect);
	}

	private static void displayRareDropEffect(Text message, boolean overlay) {
		//We don't check if we're in dungeons because that check doesn't work in m7 which defeats the point of this
		//It might also allow it to work with Croesus
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().general.specialEffects.rareDungeonDropEffects && !overlay) {
			try {
				String stringForm = message.getString();
				Matcher matcher = DROP_PATTERN.matcher(stringForm);

				if (matcher.matches()) {
					MinecraftClient client = MinecraftClient.getInstance();
					String player = matcher.group("player");

					if (player.equals(client.getSession().getUsername())) {
						ItemStack stack = getStackFromName(matcher.group("item"));

						if (stack != null && !stack.isEmpty()) {
							RenderHelper.runOnRenderThread(() -> {
								client.particleManager.addEmitter(client.player, ParticleTypes.PORTAL, 30);
								client.gameRenderer.showFloatingItem(stack);
							});
						}
					}
				}
			} catch (Exception e) { //In case there's a regex failure or something else bad happens
				LOGGER.error("[Skyblocker Special Effects] An unexpected exception was encountered: ", e);
			}
		}
	}

	private static ItemStack getStackFromName(String itemName) {
		String itemId = switch (itemName) {
			//M7
			case "Necron Dye" -> "NECRON_DYE";
			case "Dark Claymore" -> "DARK_CLAYMORE";
			case "Necron's Handle", "Shiny Necron's Handle" -> "NECRON_HANDLE";
			case "Enchanted Book (Thunderlord VII)" -> "ENCHANTED_BOOK";
			case "Master Skull - Tier 5" -> "MASTER_SKULL_TIER_5";
			case "Shadow Warp", "Wither Shield", "Implosion" -> "IMPLOSION_SCROLL";
			case "Fifth Master Star" -> "FIFTH_MASTER_STAR";

			//M6
			case "Giant's Sword" -> "GIANTS_SWORD";

			default -> "NONE";
		};
		
		return ItemRepository.getItemStack(itemId);
	}
}
