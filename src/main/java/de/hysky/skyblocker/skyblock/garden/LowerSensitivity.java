package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class LowerSensitivity {
	private static final Minecraft CLIENT = Minecraft.getInstance();
	private static boolean sensitivityLowered = false;

	@Init
	public static void init() {
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			if (Utils.getLocation() != Location.GARDEN || CLIENT.player == null || isInBarn(CLIENT.player) || !SkyblockerConfigManager.get().farming.mouseLock.lockMouseTool) {
				if (sensitivityLowered) lowerSensitivity(false);
				return;
			}
			ItemStack mainHandStack = CLIENT.player.getMainHandItem();
			String itemId = mainHandStack.getSkyblockId();
			boolean shouldLockMouse = FarmingHudWidget.FARMING_TOOLS.containsKey(itemId) && (!SkyblockerConfigManager.get().farming.mouseLock.lockMouseGroundOnly || CLIENT.player.onGround());
			if (shouldLockMouse && !sensitivityLowered) lowerSensitivity(true);
			else if (!shouldLockMouse && sensitivityLowered) lowerSensitivity(false);
		});
	}

	// pause mouse locking while in the garden's barn
	private static boolean isInBarn(Player player) {
		return player.getX() <= 35.5d && player.getX() >= -32.5d && player.getZ() <= -4.5d && player.getZ() >= -46.5d;
	}

	public static void lowerSensitivity(boolean lowerSensitivity) {
		sensitivityLowered = lowerSensitivity;
	}

	public static boolean isSensitivityLowered() {
		return sensitivityLowered;
	}
}
