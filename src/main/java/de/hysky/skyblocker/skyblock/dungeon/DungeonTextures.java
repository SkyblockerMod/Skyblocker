package de.hysky.skyblocker.skyblock.dungeon;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;

public class DungeonTextures {
	@Init
	public static void init() {
		ResourceLoader.registerBuiltinPack(
				SkyblockerMod.id("recolored_dungeon_items"),
				SkyblockerMod.SKYBLOCKER_MOD,
				PackActivationType.NORMAL
		);
	}
}
