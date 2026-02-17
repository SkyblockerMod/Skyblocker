package de.hysky.skyblocker.skyblock.item;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class HotbarSlotLock {
	public static KeyMapping hotbarSlotLock;

	@Init
	public static void init() {
		hotbarSlotLock = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.skyblocker.hotbarSlotLock",
				GLFW.GLFW_KEY_H,
				SkyblockerMod.KEYBINDING_CATEGORY
		));
	}

	public static boolean isLocked(int slot) {
		return SkyblockerConfigManager.get().general.lockedSlots.contains(slot);
	}

	public static void handleInputEvents(LocalPlayer player) {
		while (hotbarSlotLock.consumeClick()) {
			SkyblockerConfigManager.update(config -> {
				List<Integer> lockedSlots = config.general.lockedSlots;
				int selected = player.getInventory().getSelectedSlot();
				if (!isLocked(player.getInventory().getSelectedSlot())) lockedSlots.add(selected);
				else lockedSlots.remove(Integer.valueOf(selected));
			});
		}
	}
}
