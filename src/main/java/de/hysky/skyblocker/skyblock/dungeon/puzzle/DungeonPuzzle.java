package de.hysky.skyblocker.skyblock.dungeon.puzzle;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.events.DungeonEvents;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.Room;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Resettable;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.Renderable;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

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
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE).then(literal("dungeons").then(literal("puzzle").then(literal(puzzleName).then(literal("solve").executes(context -> {
            Room currentRoom = DungeonManager.getCurrentRoom();
            if (currentRoom != null) {
                reset();
                currentRoom.addSubProcess(this);
                context.getSource().sendFeedback(Constants.PREFIX.get().append("§aSolving " + puzzleName + " puzzle in the current room."));
            } else {
                context.getSource().sendError(Constants.PREFIX.get().append("§cCurrent room is null."));
            }
            return Command.SINGLE_SUCCESS;
        })))))));
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
