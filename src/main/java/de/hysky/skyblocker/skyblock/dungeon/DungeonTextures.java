package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;

public class DungeonTextures {
	@Init
	public static void init() {
		ResourceManagerHelper.registerBuiltinResourcePack(
				SkyblockerMod.id("recolored_dungeon_items"),
				SkyblockerMod.SKYBLOCKER_MOD,
				ResourcePackActivationType.NORMAL
		);
	}
}
