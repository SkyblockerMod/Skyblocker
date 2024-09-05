package de.hysky.skyblocker.skyblock.chat.chatcoords;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatWaypointLocation {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChatWaypointLocation.class);

    private static final Pattern GENERIC_COORDS_PATTERN = Pattern.compile("x: (?<x>-?[0-9]+), y: (?<y>[0-9]+), z: (?<z>-?[0-9]+)");
    private static final Pattern SKYBLOCKER_COORDS_PATTERN = Pattern.compile("x: (?<x>-?[0-9]+), y: (?<y>[0-9]+), z: (?<z>-?[0-9]+)(?: \\| (?<area>[^|]+))");
    private static final Pattern SKYHANNI_DIANA_PATTERN = Pattern.compile("A MINOS INQUISITOR has spawned near \\[(?<area>[^]]*)] at Coords (?<x>-?[0-9]+) (?<y>[0-9]+) (?<z>-?[0-9]+)");
    private static final List<Pattern> PATTERNS = List.of(SKYBLOCKER_COORDS_PATTERN, SKYHANNI_DIANA_PATTERN, GENERIC_COORDS_PATTERN);

    @Init
    public static void init() {
        ClientReceiveMessageEvents.GAME.register(ChatWaypointLocation::onMessage);
    }

    private static void onMessage(Text text, boolean overlay) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.waypoints.enableWaypoints) {

            String message = text.getString();

            for (Pattern pattern : PATTERNS) {
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()) {
                    try {
                        String x = matcher.group("x");
                        String y = matcher.group("y");
                        String z = matcher.group("z");
                        String area = matcher.group("area");
                        requestWaypoint(x, y, z, area);
                    } catch (Exception e) {
                        LOGGER.error("[SKYBLOCKER CHAT WAYPOINTS] Error creating chat waypoint: ", e);
                    }
                    break;
                }
            }
        }
    }

    private static void requestWaypoint(String x, String y, String z, String area) {
        String command = "/skyblocker waypoints individual " + x + " " + y + " " + z + " " + area;

        Text text = Constants.PREFIX.get()
                .append(Text.translatable("skyblocker.config.chat.waypoints.display").formatted(Formatting.AQUA)
                        .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))))
                .append(Text.of(area != null ? " at " + area : ""));

        MinecraftClient.getInstance().player.sendMessage(text, false);
    }
}
