package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class HotbarSlotLock {
    public static KeyBinding hotbarSlotLock;

    @Init
    public static void init() {
        hotbarSlotLock = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.hotbarSlotLock",
                GLFW.GLFW_KEY_H,
                "key.categories.skyblocker"
        ));
    }

    public static boolean isLocked(int slot) {
        return SkyblockerConfigManager.get().general.lockedSlots.contains(slot);
    }

    public static void handleInputEvents(ClientPlayerEntity player) {
        while (hotbarSlotLock.wasPressed()) {
            List<Integer> lockedSlots = SkyblockerConfigManager.get().general.lockedSlots;
            int selected = player.getInventory().selectedSlot;
            if (!isLocked(player.getInventory().selectedSlot)) lockedSlots.add(selected);
            else lockedSlots.remove(Integer.valueOf(selected));
            SkyblockerConfigManager.save();
        }
    }
}