package de.hysky.skyblocker.skyblock.dungeon;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

public class DungeonTextures {
    public static void init() {
        FabricLoader.getInstance()
            .getModContainer("skyblocker")
            .ifPresent(container -> ResourceManagerHelper.registerBuiltinResourcePack(
                new Identifier("skyblocker", "recolored_dungeon_items"),
                container,
                ResourcePackActivationType.NORMAL
            ));
    }
}
