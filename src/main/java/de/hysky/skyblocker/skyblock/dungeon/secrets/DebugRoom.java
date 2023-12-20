package de.hysky.skyblocker.skyblock.dungeon.secrets;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2ic;

public class DebugRoom extends Room {
    public DebugRoom(@NotNull Type type, @NotNull Vector2ic... physicalPositions) {
        super(type, physicalPositions);
    }
}
