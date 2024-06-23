package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.SkyblockerMod;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.minecraft.util.Identifier;

public class DarkTextures {
    public static void init() {
        ResourceManagerHelper.registerBuiltinResourcePack(
                new Identifier(SkyblockerMod.NAMESPACE, "dark_skyblocker_ui"),
                SkyblockerMod.SKYBLOCKER_MOD,
                ResourcePackActivationType.NORMAL
        );
    }
}
