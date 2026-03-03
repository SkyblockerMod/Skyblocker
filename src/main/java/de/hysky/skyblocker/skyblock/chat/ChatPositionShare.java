package de.hysky.skyblocker.skyblock.chat;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatPositionShare {
	private static final Logger LOGGER = LoggerFactory.getLogger(ChatPositionShare.class);
	private static final Pattern SIMPLE_COORDS_PATTERN = Pattern.compile("(?<x>-?[0-9]+) (?<y>[0-9]+) (?<z>-?[0-9]+)");
	private static final Pattern SIMPLE_COMMA_COORDS_PATTERN = Pattern.compile("(?<x>-?[0-9]+), (?<y>[0-9]+), (?<z>-?[0-9]+)");
	private static final Pattern GENERIC_COORDS_PATTERN = Pattern.compile("x: (?<x>-?[0-9]+), y: (?<y>[0-9]+), z: (?<z>-?[0-9]+)");
	private static final Pattern SKYBLOCKER_COORDS_PATTERN = Pattern.compile("x: (?<x>-?[0-9]+), y: (?<y>[0-9]+), z: (?<z>-?[0-9]+)(?: \\| (?<area>[^|]+))");
	private static final Pattern SKYHANNI_DIANA_PATTERN = Pattern.compile("A MINOS INQUISITOR has spawned near \\[(?<area>[^]]*)] at Coords (?<x>-?[0-9]+) (?<y>[0-9]+) (?<z>-?[0-9]+)");
	private static final List<Pattern> PATTERNS = List.of(SKYBLOCKER_COORDS_PATTERN, SKYHANNI_DIANA_PATTERN, GENERIC_COORDS_PATTERN, SIMPLE_COMMA_COORDS_PATTERN, SIMPLE_COORDS_PATTERN);

	@Init
	public static void init() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("sharePosition").executes(context -> sharePlayerPosition(context.getSource())))
					);
			dispatcher.register(
					ClientCommandManager.literal("skyblocker").then(ClientCommandManager.literal("shareCoords").executes(context -> sharePlayerPosition(context.getSource())))
					);
		});
		ClientReceiveMessageEvents.ALLOW_GAME.register(ChatPositionShare::onMessage);
	}

	@SuppressWarnings("deprecation")
	private static int sharePlayerPosition(FabricClientCommandSource source) {
		Vec3 pos = source.getPosition();
		MessageScheduler.INSTANCE.sendMessageAfterCooldown("x: " + (int) pos.x() + ", y: " + (int) pos.y() + ", z: " + (int) pos.z() + " | " + Utils.getIslandArea(), true);
		return Command.SINGLE_SUCCESS;
	}

	private static boolean onMessage(Component text, boolean overlay) {
		if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.waypoints.enableChatWaypoints) {
			String message = text.getString();

			// prevents parsing skyblocker's own messages. Also prevents TH solver from parsing as it already has own waypoint
			if (message.startsWith("[Skyblocker]") || message.startsWith("§e[NPC] Treasure Hunter§f:")) {
				return true;
			}

			for (Pattern pattern : PATTERNS) {
				Matcher matcher = pattern.matcher(message);
				if (matcher.find()) {
					try {
						String x = matcher.group("x");
						String y = matcher.group("y");
						String z = matcher.group("z");
						String area = matcher.namedGroups().containsKey("area") ? matcher.group("area") : "";
						requestWaypoint(x, y, z, area);
					} catch (Exception e) {
						LOGGER.error("[Skyblocker Chat Waypoints] Error creating chat waypoint: ", e);
					}
					break;
				}
			}
		}

		return true;
	}

	private static void requestWaypoint(String x, String y, String z, String area) {
		String command = "/skyblocker waypoints individual " + x + " " + y + " " + z + " " + area;
		MutableComponent requestMessage = Constants.PREFIX.get().append(Component.translatable("skyblocker.waypoints.chat.display", x, y, z).withStyle(ChatFormatting.AQUA)
				.withStyle(style -> style
						.withClickEvent(new ClickEvent.RunCommand(command.trim()))
						)
				);
		if (!area.isEmpty()) {
			requestMessage = requestMessage.append(" at ").append(Component.literal(area).withStyle(ChatFormatting.AQUA));
		}
		Minecraft.getInstance().player.displayClientMessage(requestMessage, false);
	}
}
