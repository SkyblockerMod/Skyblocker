package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.Renderable;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public abstract class DungeonPuzzle implements Tickable, Renderable, Resettable {
    protected final String puzzleName;
    @NotNull
    private final Set<String> roomNames;
    private boolean shouldSolve;

    public DungeonPuzzle(String puzzleName, String... roomName) {
        this(puzzleName, Set.of(roomName));
    }

    public DungeonPuzzle(String puzzleName, @NotNull Set<String> roomNames) {
        this.puzzleName = puzzleName;
        this.roomNames = roomNames;
        DungeonEvents.PUZZLE_MATCHED.register(room -> {
            if (roomNames.contains(room.getName())) {
                room.addSubProcess(this);
                shouldSolve = true;
            }
        });
        ClientPlayConnectionEvents.JOIN.register(this);
    }

    public boolean shouldSolve() {
        return shouldSolve;
    }

    @Override
    public void reset() {
        shouldSolve = false;
    }
}
