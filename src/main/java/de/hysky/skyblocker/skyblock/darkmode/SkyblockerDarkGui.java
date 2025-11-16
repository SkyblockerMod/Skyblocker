package de.hysky.skyblocker.skyblock.darkmode;

import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;

public class SkyblockerDarkGui {
    @Init
    public static void init() {
        ResourceManagerHelper.registerBuiltinResourcePack(
                SkyblockerMod.id("skyblocker_dark_gui"),
                SkyblockerMod.SKYBLOCKER_MOD,
                ResourcePackActivationType.NORMAL
        );
    }
}
