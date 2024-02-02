package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;

// 1: 15, 69, 7
// 2: 15, 70, 12
// 3: 15, 71, 19
// ice -> packed_ice
// polished andesite
public class IceFill extends DungeonPuzzle {
    public static final IceFill INSTANCE = new IceFill();

    public IceFill() {
        super("ice-fill", "ice-path");
    }

    public static void init() {}

    @Override
    public void tick(MinecraftClient client) {
        if (!SkyblockerConfigManager.get().locations.dungeons.solveIceFill) {
            return;
        }
    }

    @Override
    public void render(WorldRenderContext context) {

    }
}
