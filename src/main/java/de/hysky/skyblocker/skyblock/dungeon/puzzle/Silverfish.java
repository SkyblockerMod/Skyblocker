package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;

//right: 7, 66, 8
//left: 23, 66, 8
//right back: 7, 66, 24
//polished andesite
public class Silverfish extends DungeonPuzzle {
    public Silverfish() {
        super("Silverfish", "ice-silverfish-room");
    }

    @Override
    public void tick(MinecraftClient client) {
    }

    @Override
    public void render(WorldRenderContext context) {
    }
}
