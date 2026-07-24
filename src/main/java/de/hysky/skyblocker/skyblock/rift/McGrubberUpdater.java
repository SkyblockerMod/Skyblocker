package de.hysky.skyblocker.skyblock.rift;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.item.tooltip.info.TooltipInfoType;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.RegexUtils;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Updates the McGrubber Burger count for players automatically when the
 * related config option is enabled.
 */
public class McGrubberUpdater {
	private static final String MOTES_GRUBBER = "Motes Grubber";
	private static final String CONSUMABLE_ITEMS = "Miscellaneous ➜ Consumable Items";
	private static final Pattern ORB_PICKUP = Pattern.compile("ORB! Picked up \\+(?<motes>\\d+) Motes, recovered \\+2ф Rift Time!");
	private static final Pattern MOTES_PRICE = Pattern.compile("(?<price>\\d+(\\.\\d)?) Motes");
	private static final Pattern MCGRUBBER_PROGRESS = Pattern.compile("Total Progress: (?<percent>\\d+)%");

	private McGrubberUpdater() {}

	@Init
	public static void init() {
		ScreenEvents.BEFORE_INIT.register((_, screen, _, _) -> {
			if (!Utils.isOnSkyblock() || !(screen instanceof ContainerScreen containerScreen)) {
				return;
			} else if (!SkyblockerConfigManager.get().otherLocations.rift.autoDetectMcGrubber) {
				return;
			}

			if (Utils.isInTheRift() && containerScreen.getTitle().getString().equals(MOTES_GRUBBER)) {
				ScreenEvents.remove(screen).register(_ -> McGrubberUpdater.fromMotesGrubber(containerScreen));
			} else if (!Utils.isInTheRift() && containerScreen.getTitle().getString().equals(CONSUMABLE_ITEMS)) {
				ScreenEvents.remove(screen).register(_ -> McGrubberUpdater.fromConsumableItems(containerScreen));
			}
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register(McGrubberUpdater::fromOrbPickup);
	}

	/**
	 * Detects McGrubber stacks when selling to Motes Grubber NPC
	 */
	private static void fromMotesGrubber(ContainerScreen screen) {
		for (Slot slot : screen.getMenu().slots) {
			if (slot.container != Minecraft.getInstance().player.getInventory()) continue;

			ItemStack item = slot.getItem();
			Matcher matcher = ItemUtils.getLoreLineIfMatch(item, MOTES_PRICE);

			if (matcher == null) continue;

			float price = NumberUtils.toFloat(matcher.group("price"));
			final String internalID = item.getSkyblockId();

			// Compare against base price to determine mcgrubber stacks
			if (TooltipInfoType.MOTES.hasOrNullWarning(internalID)) {
				// x is mcgrubber stacks, y is final price, & z is initial price
				// y = (1 + 0.05x)z -> reorder to solve for x -> x = 20y / z - 20
				int mcGrubberStacks = Math.round(20 * price / TooltipInfoType.MOTES.getData().getInt(internalID) - 20);

				if (SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks != mcGrubberStacks) {
					SkyblockerConfigManager.updateOnly(config -> config.otherLocations.rift.mcGrubberStacks = mcGrubberStacks);
				}

				break;
			}
		}
	}

	/**
	 * Detects McGrubber stacks from the /sblevels GUI
	 */
	private static void fromConsumableItems(ContainerScreen screen) {
		for (Slot slot : screen.getMenu().slots) {
			ItemStack item = slot.getItem();

			if (!item.getSkyblockId().equals("MCGRUBBER_BURGER")) continue;

			Matcher matcher = ItemUtils.getLoreLineIfMatch(item, MCGRUBBER_PROGRESS);

			if (matcher == null) continue;

			// 20% progress per mcgrubber stack
			int mcGrubberStacks = RegexUtils.parseIntFromMatcher(matcher, "percent") / 20;

			if (SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks != mcGrubberStacks) {
				SkyblockerConfigManager.updateOnly(config -> config.otherLocations.rift.mcGrubberStacks = mcGrubberStacks);
			}

			break;
		}
	}

	/**
	 * Detects McGrubber stacks from picking up orbs in the rift
	 */
	private static boolean fromOrbPickup(Component text, boolean overlay) {
		if (!SkyblockerConfigManager.get().otherLocations.rift.autoDetectMcGrubber) {
			return true;
		} else if (!Utils.isOnSkyblock() || !Utils.isInTheRift() || overlay) {
			return true;
		}

		Matcher matcher = ORB_PICKUP.matcher(text.getString());

		if (matcher.matches()) {
			int motes = RegexUtils.parseIntFromMatcher(matcher, "motes");
			// Base of 5 motes, +60 motes per mcGrubber stack.
			int mcGrubberStacks = (motes - 5) / 60;

			if (SkyblockerConfigManager.get().otherLocations.rift.mcGrubberStacks != mcGrubberStacks) {
				SkyblockerConfigManager.updateOnly(config -> config.otherLocations.rift.mcGrubberStacks = mcGrubberStacks);
			}
		}

		return true;
	}
}
