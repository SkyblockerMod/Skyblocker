package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.command.CommandSource.suggestMatching;

public class CrystalsLocationsManager {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();

    /**
     * A look-up table to convert between location names and waypoint in the {@link CrystalsWaypoint.Category} values.
     */
    private static final Map<String, CrystalsWaypoint.Category> WAYPOINT_LOCATIONS = Arrays.stream(CrystalsWaypoint.Category.values()).collect(Collectors.toMap(CrystalsWaypoint.Category::toString, Function.identity()));
    private static final Pattern TEXT_CWORDS_PATTERN = Pattern.compile("([0-9][0-9][0-9]) ([0-9][0-9][0-9]?) ([0-9][0-9][0-9])");

    protected static Map<String, CrystalsWaypoint> activeWaypoints = new HashMap<>();

    public static void init() {
        Scheduler.INSTANCE.scheduleCyclic(CrystalsLocationsManager::update, 40);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrystalsLocationsManager::render);
        ClientReceiveMessageEvents.GAME.register(CrystalsLocationsManager::extractLocationFromMessage);
        ClientCommandRegistrationCallback.EVENT.register(CrystalsLocationsManager::registerWaypointLocationCommands);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

    private static void extractLocationFromMessage(Text message, Boolean overlay) {
        if (!SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.findInChat || !Utils.isInCrystalHollows()) {
            return;
        }

        try {
            //get the message text
            String value = message.getString();
            Matcher matcher = TEXT_CWORDS_PATTERN.matcher(value);
            //if there are coordinates in the message try to get them and what they are talking about
            if (matcher.find()) {
                String location = matcher.group();
                int[] coordinates = Arrays.stream(location.split(" ", 3)).mapToInt(Integer::parseInt).toArray();
                BlockPos blockPos = new BlockPos(coordinates[0], coordinates[1], coordinates[2]);

                //if position is not in the hollows do not add it
                if (!checkInCrystals(blockPos)) {
                    return;
                }

                //see if there is a name of a location to add to this
                for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
                    if (value.toLowerCase().contains(waypointLocation.toLowerCase())) { //todo be more lenient
                        //all data found to create waypoint
                        addCustomWaypoint(waypointLocation, blockPos);
                        return;
                    }
                }

                //if the location is not found ask the user for the location (could have been in a previous chat message)
                if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                    return;
                }

                CLIENT.player.sendMessage(getLocationInputText(location), false);
            }
        } catch (Exception e) {
            LOGGER.error("[Skyblocker Crystals Locations Manager] Encountered an exception while extracing a location from a chat message!", e);
        }
    }

    protected static Boolean checkInCrystals(BlockPos pos) {
        //checks if a location is inside crystal hollows bounds
        return pos.getX() >= 202 && pos.getX() <= 823
                && pos.getZ() >= 202 && pos.getZ() <= 823
                && pos.getY() >= 31 && pos.getY() <= 188;
    }

    private static void registerWaypointLocationCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
                .then(literal("crystalWaypoints")
                        .then(argument("pos", BlockPosArgumentType.blockPos())
                                .then(argument("place", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestMatching(WAYPOINT_LOCATIONS.keySet(), builder))
                                        .executes(context -> addWaypointFromCommand(context.getSource(), getString(context, "place"), context.getArgument("pos", PosArgument.class)))
                                )
                        )
                        .then(literal("share")
                                .then(argument("place", StringArgumentType.greedyString())
                                        .suggests((context, builder) -> suggestMatching(WAYPOINT_LOCATIONS.keySet(), builder))
                                        .executes(context -> shareWaypoint(getString(context, "place")))
                                )
                        )
                )
        );
    }

    protected static Text getSetLocationMessage(String location, BlockPos blockPos) {
        MutableText text = Constants.PREFIX.get();
        text.append(Text.literal("Added waypoint for "));
        Color locationColor = WAYPOINT_LOCATIONS.get(location).color;
        text.append(Text.literal(location).withColor(locationColor.getRGB()));
        text.append(Text.literal(" at : " + blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ() + "."));

        return text;
    }

    private static Text getLocationInputText(String location) {
        MutableText text = Constants.PREFIX.get();

        for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
            Color locationColor = WAYPOINT_LOCATIONS.get(waypointLocation).color;
            text.append(Text.literal("[" + waypointLocation + "]").withColor(locationColor.getRGB()).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker crystalWaypoints " + location + " " + waypointLocation))));
        }

        return text;
    }

    public static int addWaypointFromCommand(FabricClientCommandSource source, String place, PosArgument location) {
        // TODO Less hacky way with custom ClientBlockPosArgumentType
        BlockPos blockPos = location.toAbsoluteBlockPos(new ServerCommandSource(null, source.getPosition(), source.getRotation(), null, 0, null, null, null, null));

        if (WAYPOINT_LOCATIONS.containsKey(place)) {
            addCustomWaypoint(place, blockPos);

            //tell the client it has done this
            if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                return 0;
            }

            CLIENT.player.sendMessage(getSetLocationMessage(place, blockPos), false);
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int shareWaypoint(String place) {
        if (activeWaypoints.containsKey(place)) {
            BlockPos pos = activeWaypoints.get(place).pos;
            MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + " " + place + ": " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        } else {
            //send fail message
            if (CLIENT.player == null || CLIENT.getNetworkHandler() == null) {
                return 0;
            }
            CLIENT.player.sendMessage(Constants.PREFIX.get().append(Text.translatable("text.autoconfig.skyblocker.option.locations.dwarvenMines.crystalsWaypoints.shareFail").formatted(Formatting.RED)), false);
        }

        return Command.SINGLE_SUCCESS;
    }


    private static void addCustomWaypoint(String waypointName, BlockPos pos) {
        CrystalsWaypoint.Category category = WAYPOINT_LOCATIONS.get(waypointName);
        CrystalsWaypoint waypoint = new CrystalsWaypoint(category, Text.literal(waypointName), pos);
        activeWaypoints.put(waypointName, waypoint);
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.enabled) {
            for (CrystalsWaypoint crystalsWaypoint : activeWaypoints.values()) {
                if (crystalsWaypoint.shouldRender()) {
                    crystalsWaypoint.render(context);
                }
            }
        }
    }

    private static void reset() {
        activeWaypoints.clear();
    }

    public static void update() {
        if (CLIENT.player == null || CLIENT.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.enabled || !Utils.isInCrystalHollows()) {
            return;
        }

        //get if the player is in the crystals
        String location = Utils.getIslandArea().substring(2);
        //if new location and needs waypoint add waypoint
        if (!location.equals("Unknown") && WAYPOINT_LOCATIONS.containsKey(location) && !activeWaypoints.containsKey(location)) {
            //add waypoint at player location
            BlockPos playerLocation = CLIENT.player.getBlockPos();
            addCustomWaypoint(location, playerLocation);
        }
    }
}
