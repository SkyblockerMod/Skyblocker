package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

public class LowerSensitivity {
	private static boolean sensitivityLowered = false;

	@Init
	public static void init() {
		ClientTickEvents.END_WORLD_TICK.register(world -> {
			if (Utils.getLocation() != Location.GARDEN || Minecraft.getInstance().player == null || !SkyblockerConfigManager.get().farming.garden.lockMouseTool) {
				if (sensitivityLowered) lowerSensitivity(false);
				return;
			}
			ItemStack mainHandStack = Minecraft.getInstance().player.getMainHandItem();
			String itemId = mainHandStack.getSkyblockId();
			boolean shouldLockMouse = FarmingHudWidget.FARMING_TOOLS.containsKey(itemId) && (!SkyblockerConfigManager.get().farming.garden.lockMouseGroundOnly || Minecraft.getInstance().player.onGround());
			if (shouldLockMouse && !sensitivityLowered) lowerSensitivity(true);
			else if (!shouldLockMouse && sensitivityLowered) lowerSensitivity(false);
		});
	}

	public static void lowerSensitivity(boolean lowerSensitivity) {
		sensitivityLowered = lowerSensitivity;
	}

	public static boolean isSensitivityLowered() {
		return sensitivityLowered;
	}
}
