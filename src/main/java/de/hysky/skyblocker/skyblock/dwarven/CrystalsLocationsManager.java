package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CrystalsLocationsManager {
    public static final MinecraftClient client = MinecraftClient.getInstance();


    public static final Map<String, CrystalsWaypoint.Category> WAYPOINTLOCATIONS = Map.of(
            "Jungle Temple", CrystalsWaypoint.Category.JUNGLETEMPLE,
            "Mines Of Divan", CrystalsWaypoint.Category.MINESOFDIVAN,
            "Goblin Queen's Den", CrystalsWaypoint.Category.GOBLINQUEENSDEN,
            "Lost Precursor City", CrystalsWaypoint.Category.LOSTPRECURSORCITY,
            "Khazad-dûm", CrystalsWaypoint.Category.KHAZADUM,
            "Fairy Grotto", CrystalsWaypoint.Category.FAIRYGROTTO,
            "Dragon's Lair", CrystalsWaypoint.Category.DRAGONSLAIR,
            "Corleone", CrystalsWaypoint.Category.DRAGONSLAIR
    );

    private static final Pattern TEXT_CWORDS_PATTERN = Pattern.compile("([0-9][0-9][0-9]) ([0-9][0-9][0-9]?) ([0-9][0-9][0-9])");


    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrystalsLocationsManager::render);
        ClientReceiveMessageEvents.CHAT.register(CrystalsLocationsManager::extractLocationFromMessage);
        ClientCommandRegistrationCallback.EVENT.register(CrystalsLocationsManager::registerWaypointLocationCommands);
    }
    private static void extractLocationFromMessage(Text message, SignedMessage signedMessage, GameProfile sender, MessageType.Parameters params, Instant receptionTimestamp){
        if (!SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.findInChat || !Utils.isInCrystals()) {
            return;
        }
        //get the message text
        String value = signedMessage.getContent().getString();
        Matcher matcher = TEXT_CWORDS_PATTERN.matcher(value);
        //if there are cwords in the message try to get them and what they are talking about
        if (matcher.find()){
            String location = matcher.group();
            Integer[] cowordinates = Arrays.stream(location.split(" ",3)).map(Integer::parseInt).toArray(Integer[]::new);
            BlockPos blockPos = new BlockPos(cowordinates[0],cowordinates[1],cowordinates[2]);
            //if position is not in the hollows do not add it
            if (!checkInCrystals(blockPos)){
                return;
            }
            //see if there is a name of a location to add to this
            for (String waypointLocation : WAYPOINTLOCATIONS.keySet()){
                if (value.toLowerCase().contains(waypointLocation.toLowerCase())){ //todo be more lenient
                    //all data found to create waypoint
                    addCustomWaypoint(Text.of(waypointLocation),blockPos);
                    return;
                }
            }
            //if the location is not found ask the user for the location (could have been in a previous chat message)
            if (client.player == null || client.getNetworkHandler() == null ) {
                return;
            }
            client.player.sendMessage(getLocationInputText(location), false);
        }


    }
    private static Boolean checkInCrystals(BlockPos pos){
        //checks if a location is inside crystal hollows bounds
        return     pos.getX() >= 202 && pos.getX() <= 823
                && pos.getZ() >= 202 && pos.getZ() <= 823
                && pos.getY() >= 31  && pos.getY() <= 188;
    }
    private static void registerWaypointLocationCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register(literal(SkyblockerMod.NAMESPACE)
            .then(literal("crystalWaypoints")
                .then(argument("pos", BlockPosArgumentType.blockPos())
                        .then(argument("place", StringArgumentType.greedyString())
                                .executes(context -> addWaypointFromCommand(context.getSource(), getString(context, "place"),context.getArgument("pos", PosArgument.class)))
                        )
                )
            )
        );
    }
    private static Text getSetLocationMessage(String location,BlockPos blockPos) {
        MutableText text = Text.empty();
        text.append(Text.literal("Added waypoint for "));
        Color locationColor = WAYPOINTLOCATIONS.get(location).color;
        text.append(Text.literal(location).withColor(locationColor.getRGB()));
        text.append(Text.literal(" at : "+blockPos.getX()+" "+blockPos.getY()+" "+blockPos.getZ()+"."));
        return text;
    }
    private static Text getLocationInputText(String location) {
        MutableText text = Text.empty();
        for (String waypointLocation : WAYPOINTLOCATIONS.keySet()){
            Color locationColor = WAYPOINTLOCATIONS.get(waypointLocation).color;
            text.append(Text.literal("["+waypointLocation+"]").withColor(locationColor.getRGB()).styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker crystalWaypoints "+location+" "+waypointLocation))));
        }

        return text;
    }
    public static int addWaypointFromCommand(FabricClientCommandSource source, String place, PosArgument location) {
        // TODO Less hacky way with custom ClientBlockPosArgumentType
        BlockPos blockPos = location.toAbsoluteBlockPos(new ServerCommandSource(null, source.getPosition(), source.getRotation(), null, 0, null, null, null, null));
        if (WAYPOINTLOCATIONS.containsKey(place)){
            addCustomWaypoint(Text.of(place), blockPos);
            //tell the client it has done this
            if (client.player == null || client.getNetworkHandler() == null ) {
                return 0;
            }
            client.player.sendMessage(getSetLocationMessage(place, blockPos), false);
        }
        return Command.SINGLE_SUCCESS;
    }


    private static void addCustomWaypoint( Text waypointName, BlockPos pos) {
        CrystalsWaypoint.Category category = WAYPOINTLOCATIONS.get(waypointName.getString());
        CrystalsWaypoint waypoint = new CrystalsWaypoint(category, waypointName, pos);
        Map<String,CrystalsWaypoint> ActiveWaypoints=  SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.ActiveWaypoints;
        ActiveWaypoints.put(waypointName.getString(),waypoint);
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.enabled ) {
            Map<String,CrystalsWaypoint> ActiveWaypoints=  SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.ActiveWaypoints;
            for (CrystalsWaypoint crystalsWaypoint : ActiveWaypoints.values()) {
                if (crystalsWaypoint.shouldRender()) {
                    crystalsWaypoint.render(context);
                }
            }
        }
    }

    public static void update() {
        if (client.player == null || client.getNetworkHandler() == null || !SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.enabled) {
            SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.ActiveWaypoints= new HashMap<>();
            return;
        }
        //get if the player is in the crystals
        String location = Utils.getIslandArea().replace("⏣ ","");
        //if new location and needs waypoint add waypoint
        Map<String,CrystalsWaypoint> ActiveWaypoints=  SkyblockerConfigManager.get().locations.dwarvenMines.crystalsWaypoints.ActiveWaypoints;
        if (!location.equals("Unknown") && WAYPOINTLOCATIONS.containsKey(location) && !ActiveWaypoints.containsKey(location)){
            //add waypoint at player location
            BlockPos playerLocation = client.player.getBlockPos();
            addCustomWaypoint(Text.of(location),playerLocation);
        }


    }
}
