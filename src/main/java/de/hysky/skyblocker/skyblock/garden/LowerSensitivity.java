package de.hysky.skyblocker.skyblock.garden;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

public class LowerSensitivity {

    private static boolean sensitivityLowered = false;

    public static void init() {
        ClientTickEvents.END_WORLD_TICK.register(world -> {
            if (!Utils.isOnSkyblock() || Utils.getLocation() != Location.GARDEN || MinecraftClient.getInstance().player == null) {
                if (sensitivityLowered) lowerSensitivity(false);
                return;
            }
            if (SkyblockerConfigManager.get().locations.garden.lockMouseTool) {
                ItemStack mainHandStack = MinecraftClient.getInstance().player.getMainHandStack();
                String itemId = ItemUtils.getItemId(mainHandStack);
                boolean shouldLockMouse = FarmingHudWidget.FARMING_TOOLS.containsKey(itemId) && (!SkyblockerConfigManager.get().locations.garden.lockMouseGroundOnly || MinecraftClient.getInstance().player.isOnGround());
                if (shouldLockMouse && !sensitivityLowered) lowerSensitivity(true);
                else if (!shouldLockMouse && sensitivityLowered) lowerSensitivity(false);

            }
        });
    }

    public static void lowerSensitivity(boolean lowerSensitivity) {
        if (sensitivityLowered == lowerSensitivity) return;
        sensitivityLowered = lowerSensitivity;
    }

    public static boolean isSensitivityLowered() {
        return sensitivityLowered;
    }
}
