package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;

// right: 5, 63, 8
// left: 25, 63, 8
// jungle, birch
public class Boulder extends DungeonPuzzle {
    public Boulder() {
        super("Boulder", "boxes-room");
    }

    @Override
    public void tick(MinecraftClient client) {
    }

    @Override
    public void render(WorldRenderContext context) {
    }
}
