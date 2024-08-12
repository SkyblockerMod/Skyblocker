package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.util.Identifier;

public class StyledTitleScreen {
	public static void init() {
		ResourceManagerHelper.registerBuiltinResourcePack(
				Identifier.of(SkyblockerMod.NAMESPACE, "styled_title_screen"),
				SkyblockerMod.SKYBLOCKER_MOD,
				ResourcePackActivationType.NORMAL
		);
	}
}
