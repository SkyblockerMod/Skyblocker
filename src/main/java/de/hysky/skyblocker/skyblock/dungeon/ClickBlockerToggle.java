package de.hysky.skyblocker.skyblock.dungeon;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;

/**
 * Emergency off switch for the dungeon incorrect click blockers: shift + Z flips every
 * blockIncorrectClicks toggle at once, for when a blocker misfires mid run.
 */
public final class ClickBlockerToggle {
	public static final KeyMapping TOGGLE_CLICK_BLOCKERS = KeyMappingHelper.registerKeyMapping(
			new KeyMapping("key.skyblocker.toggleClickBlockers", InputConstants.KEY_Z, SkyblockerMod.KEYBINDING_CATEGORY));

	private ClickBlockerToggle() {}

	public static void handleInputEvents(LocalPlayer player) {
		while (TOGGLE_CLICK_BLOCKERS.consumeClick()) {
			Minecraft client = Minecraft.getInstance();
			if (!InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_LSHIFT)
					&& !InputConstants.isKeyDown(client.getWindow(), InputConstants.KEY_RSHIFT)) {
				continue;
			}

			SkyblockerConfigManager.update(config -> {
				var dungeons = config.dungeons;
				boolean anyOn = dungeons.puzzleSolvers.blockIncorrectClicks || dungeons.devices.blockIncorrectClicks || dungeons.terminals.blockIncorrectClicks;
				dungeons.puzzleSolvers.blockIncorrectClicks = !anyOn;
				dungeons.devices.blockIncorrectClicks = !anyOn;
				dungeons.terminals.blockIncorrectClicks = !anyOn;
				player.sendSystemMessage(Constants.PREFIX.get().append(Component.translatable(anyOn
						? "skyblocker.dungeons.blockers.disabled"
						: "skyblocker.dungeons.blockers.enabled").withStyle(anyOn ? ChatFormatting.RED : ChatFormatting.GREEN)));
			});
		}
	}
}
