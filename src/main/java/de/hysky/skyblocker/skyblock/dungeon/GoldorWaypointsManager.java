package de.hysky.skyblocker.skyblock.dungeon;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.dungeon.secrets.DungeonManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.NamedWaypoint;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GoldorWaypointsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GoldorWaypointsManager.class);

    private static final ObjectArrayList<GoldorWaypoint> TERMINALS = new ObjectArrayList<>();
    private static final ObjectArrayList<GoldorWaypoint> DEVICES = new ObjectArrayList<>();
    private static final ObjectArrayList<GoldorWaypoint> LEVERS = new ObjectArrayList<>();

    private static final String TERMINALS_START = "[BOSS] Storm: I should have known that I stood no chance.";
    private static final Pattern TERMINAL_ACTIVATED = Pattern.compile("^(?<name>\\w+) activated a terminal! \\(\\d/\\d\\)$");
    private static final Pattern DEVICE_ACTIVATED = Pattern.compile("^(?<name>\\w+) completed a device! \\(\\d/\\d\\)$");
    private static final Pattern LEVER_ACTIVATED = Pattern.compile("^(?<name>\\w+) activated a lever! \\(\\d/\\d\\)$");
    private static final Pattern PHASE_COMPLETE = Pattern.compile("^(?<name>\\w+) (?:activated a (?:terminal|lever)|completed a device)! (?:\\(7/7\\)|\\(8/8\\))$");
    private static final String CORE_ENTRANCE = "The Core entrance is opening!";
    private static final Codec<List<GoldorWaypoint>> CODEC = GoldorWaypoint.CODEC.listOf();

    // If the waypoints are loaded
    private static boolean loaded = false;
    // If this should be processed
    private static boolean active = false;
    // The current set of terminals, each phase is delimited by a gate
    private static short currentPhase = 0;

	@Init
	public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(GoldorWaypointsManager::render);
        ClientLifecycleEvents.CLIENT_STARTED.register(GoldorWaypointsManager::load);
        ClientReceiveMessageEvents.ALLOW_GAME.register(GoldorWaypointsManager::onChatMessage);
        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> reset()));
    }

    private static void load(MinecraftClient client) {
        CompletableFuture<Void> terminals = loadWaypoints(client, Identifier.of(SkyblockerMod.NAMESPACE, "dungeons/goldorwaypoints.json"));

        terminals.whenComplete((_result, _throwable) -> loaded = true);
    }

    private static CompletableFuture<Void> loadWaypoints(MinecraftClient client, Identifier file) {
        return CompletableFuture.supplyAsync(() -> {
            try (BufferedReader reader = client.getResourceManager().openAsReader(file)) {
                JsonArray arr = JsonParser.parseReader(reader).getAsJsonArray();

                return CODEC.parse(JsonOps.INSTANCE, arr).getOrThrow();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker Goldor Waypoints] Failed to load waypoints from: {}", file, e);

                return List.<GoldorWaypoint>of();
            }
        }).thenAccept(list -> list.forEach(waypoint -> {
            switch (waypoint.kind) {
                case TERMINAL -> TERMINALS.add(waypoint);
                case DEVICE -> DEVICES.add(waypoint);
                case LEVER -> LEVERS.add(waypoint);
            }
        }));
    }

    /**
     * Checks if we should process messages
     *
     * @return true if we should process messages
     */
    private static boolean shouldProcessMsgs() {
        return (loaded && SkyblockerConfigManager.get().dungeons.goldor.enableGoldorWaypoints && Utils.isInDungeons() && DungeonManager.isInBoss() && DungeonManager.getBoss().isFloor(7));
    }

    /**
     * Given a list of waypoints to operate on and a player name, hides the visible waypoint that is closest to the player
     *
     * @param waypoints  The list of waypoints to operate on
     * @param playerName The name of the player to check against
     */
    private static void removeNearestWaypoint(List<GoldorWaypoint> waypoints, String playerName) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

		// Get the position of the player with the given name
        Optional<Vec3d> posOptional = client.world.getPlayers().stream().filter(player -> player.getGameProfile().getName().equals(playerName)).findAny().map(Entity::getPos);

		// Find the nearest waypoint to the player and hide it
		posOptional.flatMap(pos -> waypoints.stream().filter(GoldorWaypoint::shouldRender).min(Comparator.comparingDouble(waypoint -> waypoint.centerPos.squaredDistanceTo(pos)))).ifPresent(Waypoint::setFound);
    }

    /**
     * Resets state, disabling waypoint rendering (but setting all waypoints to be ready for the next run)
     */
    private static void reset() {
        active = false;
        currentPhase = 0;
        enableAll(TERMINALS);
        enableAll(DEVICES);
        enableAll(LEVERS);
    }

    /**
     * Enables rendering for all waypoints in a set
     *
     * @param waypoints The set of waypoints to enable rendering for
     */
    private static void enableAll(ObjectArrayList<GoldorWaypoint> waypoints) {
        waypoints.forEach(Waypoint::setMissing);
    }

    /**
     * Convenience method to extract the player name from a message
     *
     * @param matcher The matcher to extract the name from
     * @return The player name, or null if the matcher didn't match
     */
    @Nullable
    private static String getPlayerName(Matcher matcher) {
        return matcher.matches() ? matcher.group("name") : null;
    }

    private static boolean onChatMessage(Text text, boolean overlay) {
        if (overlay || !shouldProcessMsgs()) return true;
        String message = text.getString();

        if (active) {
            if (PHASE_COMPLETE.matcher(message).matches()) {
                currentPhase++;
            } else {
                String playerName;

                if ((playerName = getPlayerName(TERMINAL_ACTIVATED.matcher(message))) != null) {
                    removeNearestWaypoint(TERMINALS, playerName);
                } else if ((playerName = getPlayerName(DEVICE_ACTIVATED.matcher(message))) != null) {
                    removeNearestWaypoint(DEVICES, playerName);
                } else if ((playerName = getPlayerName(LEVER_ACTIVATED.matcher(message))) != null) {
                    removeNearestWaypoint(LEVERS, playerName);
                } else if (message.equals(CORE_ENTRANCE)) {
                    active = false;
                }
            }
        } else {
            if (message.equals(TERMINALS_START)) {
                enableAll(TERMINALS);
                enableAll(DEVICES);
                enableAll(LEVERS);
                active = true;
            }
        }

        return true;
    }

    private static void renderWaypoints(WorldRenderContext context, ObjectArrayList<GoldorWaypoint> waypoints) {
        for (GoldorWaypoint waypoint : waypoints) {
            if (waypoint.phase == currentPhase && waypoint.shouldRender()) {
                waypoint.render(context);
            }
        }
    }

    private static void render(WorldRenderContext context) {
        if (active) {
            renderWaypoints(context, TERMINALS);
            renderWaypoints(context, DEVICES);
            renderWaypoints(context, LEVERS);
        }
    }

    private static class GoldorWaypoint extends NamedWaypoint {
        public static final Codec<GoldorWaypoint> CODEC = RecordCodecBuilder.create(i -> i.group(
                WaypointTargetKind.CODEC.fieldOf("kind").forGetter(w -> w.kind),
                Codec.INT.fieldOf("phase").forGetter(customWaypoint -> customWaypoint.phase),
                TextCodecs.CODEC.fieldOf("name").forGetter(NamedWaypoint::getName),
                BlockPos.CODEC.fieldOf("pos").forGetter(customWaypoint -> customWaypoint.pos)
        ).apply(i, GoldorWaypoint::new));

        private static final Supplier<Type> TYPE_SUPPLIER = () -> SkyblockerConfigManager.get().dungeons.goldor.waypointType;

        final WaypointTargetKind kind;
        final int phase;

        GoldorWaypoint(WaypointTargetKind kind, int phase, Text name, BlockPos pos) {
            super(pos, name, TYPE_SUPPLIER, kind.colorComponents, 0.25F, true);
            this.kind = kind;
            this.phase = phase;
        }

        /**
         * The different classes of waypoints
         */
        enum WaypointTargetKind implements StringIdentifiable {
            TERMINAL(0, 255, 0),
            DEVICE(0, 0, 255),
            LEVER(255, 255, 0);

            private static final Codec<WaypointTargetKind> CODEC = StringIdentifiable.createBasicCodec(WaypointTargetKind::values);
            private final float[] colorComponents;

            WaypointTargetKind(int r, int g, int b) {
                this.colorComponents = new float[]{r / 255F, g / 255F, b / 255F};
            }

            @Override
            public String asString() {
                return name().toLowerCase();
            }
        }
    }
}
