package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.Constants;
import de.hysky.skyblocker.utils.Location;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.render.WorldRenderExtractionCallback;
import de.hysky.skyblocker.utils.render.primitive.PrimitiveCollector;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import de.hysky.skyblocker.utils.ws.WsMessageHandler;
import de.hysky.skyblocker.utils.ws.Service;
import de.hysky.skyblocker.utils.ws.WsStateManager;
import de.hysky.skyblocker.utils.ws.message.CrystalsWaypointMessage;
import de.hysky.skyblocker.utils.ws.message.CrystalsWaypointSubscribeMessage;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import static net.minecraft.commands.SharedSuggestionProvider.suggest;

/**
 * Manager for Crystal Hollows waypoints that handles {@link #update() location detection},
 * {@link #extractLocationFromMessage(Component, Boolean) waypoints receiving}, {@link #shareWaypoint(String) sharing},
 * {@link #registerWaypointLocationCommands(CommandDispatcher, CommandBuildContext) commands}, and
 * {@link #extractRendering(PrimitiveCollection) render extraction}.
 */
public class CrystalsLocationsManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Minecraft CLIENT = Minecraft.getInstance();

	/**
	 * A look-up table to convert between location names and waypoint in the {@link MiningLocationLabel.CrystalHollowsLocationsCategory} values.
	 */
	private static final Map<String, MiningLocationLabel.CrystalHollowsLocationsCategory> WAYPOINT_LOCATIONS = Arrays.stream(MiningLocationLabel.CrystalHollowsLocationsCategory.values()).collect(Collectors.toMap(MiningLocationLabel.CrystalHollowsLocationsCategory::getName, Function.identity()));
	//Package-private for testing
	static final Pattern TEXT_CWORDS_PATTERN = Pattern.compile("\\Dx?(\\d{3})(?=[, ]),? ?y?(\\d{2,3})(?=[, ]),? ?z?(\\d{3})\\D?(?!\\d)");
	private static final int REMOVE_UNKNOWN_DISTANCE = 50;

	protected static Map<String, MiningLocationLabel> activeWaypoints = new HashMap<>();
	protected static List<String> verifiedWaypoints = new ArrayList<>();
	private static final List<MiningLocationLabel.CrystalHollowsLocationsCategory> waypointsSent2Socket = new ArrayList<>();

	@Init
	public static void init() {
		// Crystal Hollows Waypoints
		Scheduler.INSTANCE.scheduleCyclic(CrystalsLocationsManager::update, 40);
		WorldRenderExtractionCallback.EVENT.register(CrystalsLocationsManager::extractRendering);
		ClientReceiveMessageEvents.ALLOW_GAME.register(CrystalsLocationsManager::extractLocationFromMessage);
		ClientCommandRegistrationCallback.EVENT.register(CrystalsLocationsManager::registerWaypointLocationCommands);
		SkyblockEvents.LOCATION_CHANGE.register(CrystalsLocationsManager::onLocationChange);
		ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());

		// Nucleus Waypoints
		WorldRenderExtractionCallback.EVENT.register(NucleusWaypoints::extractRendering);
	}

	private static boolean extractLocationFromMessage(Component message, Boolean overlay) {
		if (!SkyblockerConfigManager.get().mining.crystalsWaypoints.findInChat || !Utils.isInCrystalHollows() || overlay) {
			return true;
		}
		String text = ChatFormatting.stripFormatting(message.getString());
		try {
			//make sure that it is only reading user messages and not from skyblocker
			if (text.contains(":") && !text.startsWith(Constants.PREFIX.get().getString())) {
				String userMessage = text.split(":", 2)[1];

				//get the message text
				Matcher matcher = TEXT_CWORDS_PATTERN.matcher(userMessage);
				//if there are coordinates in the message try to get them and what they are talking about
				if (matcher.find()) {
					BlockPos blockPos = new BlockPos(Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3)));
					String location = blockPos.getX() + " " + blockPos.getY() + " " + blockPos.getZ();
					//if position is not in the hollows do not add it
					if (!checkInCrystals(blockPos)) {
						return true;
					}

					//see if there is a name of a location to add to this
					for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
						if (Arrays.stream(waypointLocation.toLowerCase(Locale.ENGLISH).split(" ")).anyMatch(word -> userMessage.toLowerCase(Locale.ENGLISH).contains(word))) { //check if contains a word of location
							//all data found to create waypoint
							//make sure the waypoint does not already exist in active waypoints, so waypoints can not get randomly moved
							if (!activeWaypoints.containsKey(waypointLocation)) {
								addCustomWaypoint(waypointLocation, blockPos);
							}
							return true;
						}
					}

					//if the location is not found ask the user for the location (could have been in a previous chat message)
					if (CLIENT.player == null || CLIENT.getConnection() == null) {
						return true;
					}

					CLIENT.player.displayClientMessage(getLocationMenu(location, false), false);
				}
			}

		} catch (Exception e) {
			LOGGER.error("[Skyblocker Crystals Locations Manager] Encountered an exception while extracing a location from a chat message!", e);
		}

		//move waypoint to be more accurate based on locational chat messages if not already verifed
		if (CLIENT.player != null && SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled) {
			for (MiningLocationLabel.CrystalHollowsLocationsCategory waypointLocation : WAYPOINT_LOCATIONS.values()) {
				String waypointLinkedMessage = waypointLocation.getLinkedMessage();
				String waypointName = waypointLocation.getName();
				if (waypointLinkedMessage != null && text.contains(waypointLinkedMessage) && !verifiedWaypoints.contains(waypointName)) {
					addCustomWaypoint(waypointLocation.getName(), CLIENT.player.blockPosition());
					verifiedWaypoints.add(waypointName);
					trySendWaypoint2Socket(waypointLocation);
				}
			}
		}

		return true;
	}

	protected static boolean checkInCrystals(BlockPos pos) {
		//checks if a location is inside crystal hollows bounds
		return pos.getX() >= 202 && pos.getX() <= 823
				&& pos.getZ() >= 202 && pos.getZ() <= 823
				&& pos.getY() >= 31 && pos.getY() <= 188;
	}

	private static void registerWaypointLocationCommands(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandBuildContext registryAccess) {
		dispatcher.register(literal(SkyblockerMod.NAMESPACE)
				.then(literal("crystalWaypoints")
						.then(literal("add")
								.executes(context -> {
									if (CLIENT.player == null) {
										return 0;
									}
									CLIENT.player.displayClientMessage(getLocationMenu((int) CLIENT.player.getX() + " " + (int) CLIENT.player.getY() + " " + (int) CLIENT.player.getZ(), true), false);
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("pos", ClientBlockPosArgumentType.blockPos())
										.then(argument("place", StringArgumentType.greedyString())
												.suggests((context, builder) -> suggest(WAYPOINT_LOCATIONS.keySet(), builder))
												.executes(context -> addWaypointFromCommand(context.getSource(), getString(context, "place"), context.getArgument("pos", ClientPosArgument.class)))
										)
								))
						.then(literal("share")
								.executes(context -> {
									if (CLIENT.player == null) {
										return 0;
									}
									CLIENT.player.displayClientMessage(getPlacesMenu("share"), false);
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("place", StringArgumentType.greedyString())
										.suggests((context, builder) -> suggest(WAYPOINT_LOCATIONS.keySet(), builder))
										.executes(context -> shareWaypoint(getString(context, "place")))
								)
						)
						.then(literal("remove")
								.executes(context -> {
									if (CLIENT.player == null) {
										return 0;
									}
									CLIENT.player.displayClientMessage(getPlacesMenu("remove"), false);
									return Command.SINGLE_SUCCESS;
								})
								.then(argument("place", StringArgumentType.greedyString())
										.suggests((context, builder) -> suggest(WAYPOINT_LOCATIONS.keySet(), builder))
										.executes(context -> removeWaypoint(getString(context, "place")))
								)
						)
				)
		);
	}

	protected static Component getSetLocationMessage(String location, BlockPos blockPos) {
		int locationColor = WAYPOINT_LOCATIONS.get(location).getColor();

		// Minecraft transforms all arguments (`%s`, `%d`, whatever) to `%$1s` DURING LOADING in `Language#load(InputStream, BiConsumer<String,String>)` for some unknown reason.
		// And then `TranslatableTextContent#forEachPart` only accepts `%s` for some other unknown reason.
		// So that's why the arguments are all `%s`. Wtf mojang?????????
		return Constants.PREFIX.get().append(Component.translatableWithFallback("skyblocker.config.mining.crystalsWaypoints.addedWaypoint", "Added waypoint for '%s' at %s %s %s.", Component.literal(location).withColor(locationColor), blockPos.getX(), blockPos.getY(), blockPos.getZ()));
	}

	/**
	 * Creates a formated text with a list of possible places to add a waypoint for
	 *
	 * @param location       the location where the waypoint will be created
	 * @param excludeUnknown if the {@link de.hysky.skyblocker.skyblock.dwarven.MiningLocationLabel.CrystalHollowsLocationsCategory#UNKNOWN Unknown} location should be available to add
	 * @return text for a message to send to the player
	 */
	private static Component getLocationMenu(String location, boolean excludeUnknown) {

		//if the user has all available waypoints active warn them instead of an empty list (excused unknown from check when disabled)
		if (activeWaypoints.size() == WAYPOINT_LOCATIONS.size() || (excludeUnknown && WAYPOINT_LOCATIONS.size() - activeWaypoints.size() == 1 && !activeWaypoints.containsKey(MiningLocationLabel.CrystalHollowsLocationsCategory.UNKNOWN.getName()))) {
			return Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.allActive").withStyle(ChatFormatting.RED));
		}

		//add starting message
		MutableComponent text = Component.empty();

		//add possible locations to the message
		for (String waypointLocation : WAYPOINT_LOCATIONS.keySet()) {
			//do not show option to add waypoints for existing locations or unknown if its disabled
			if (activeWaypoints.containsKey(waypointLocation) || (excludeUnknown && Objects.equals(waypointLocation, MiningLocationLabel.CrystalHollowsLocationsCategory.UNKNOWN.getName()))) {
				continue;
			}
			int locationColor = WAYPOINT_LOCATIONS.get(waypointLocation).getColor();
			text.append(Component.literal("[" + waypointLocation + "]").withColor(locationColor).withStyle(style -> style
					.withClickEvent(new ClickEvent.RunCommand("/skyblocker crystalWaypoints add " + location + " " + waypointLocation))
					.withHoverEvent(new HoverEvent.ShowText(Component.translatable("skyblocker.config.mining.crystalsWaypoints.getLocationHover.add").withColor(locationColor))))
			);
		}

		return Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.markLocation", location, text));
	}

	/**
	 * Creates a formated text with a list of found places to remove / share a waypoint for
	 *
	 * @param action the action the command should perform (remove / share)
	 * @return text for a message to send to the player
	 */
	private static Component getPlacesMenu(String action) {
		MutableComponent text = Constants.PREFIX.get();

		//if the user has no active warn them instead of an empty list
		if (activeWaypoints.isEmpty()) {
			return text.append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.noActive").withStyle(ChatFormatting.RED));
		}

		//depending on the action load the correct prefix and hover message
		MutableComponent hoverMessage;
		if (action.equals("remove")) {
			text.append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.getLocationHover.remove").append(Component.literal(": ")));
			hoverMessage = Component.translatable("skyblocker.config.mining.crystalsWaypoints.getLocationHover.remove");
		} else {
			text.append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.getLocationHover.share").append(Component.literal(": ")));
			hoverMessage = Component.translatable("skyblocker.config.mining.crystalsWaypoints.getLocationHover.share");
		}

		for (String waypointLocation : activeWaypoints.keySet()) {
			int locationColor = WAYPOINT_LOCATIONS.get(waypointLocation).getColor();
			text.append(Component.literal("[" + waypointLocation + "]").withColor(locationColor).withStyle(style -> style
					.withClickEvent(new ClickEvent.RunCommand("/skyblocker crystalWaypoints " + action + " " + waypointLocation))
					.withHoverEvent(new HoverEvent.ShowText(hoverMessage.withColor(locationColor))))
			);
		}

		return text;
	}

	public static int addWaypointFromCommand(FabricClientCommandSource source, String place, ClientPosArgument location) {
		BlockPos blockPos = location.toAbsoluteBlockPos(source);

		if (WAYPOINT_LOCATIONS.containsKey(place)) {
			addCustomWaypoint(place, blockPos);

			//tell the client it has done this
			if (CLIENT.player == null || CLIENT.getConnection() == null) {
				return 0;
			}

			CLIENT.player.displayClientMessage(getSetLocationMessage(place, blockPos), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	public static int shareWaypoint(String place) {
		if (activeWaypoints.containsKey(place)) {
			BlockPos pos = activeWaypoints.get(place).pos;
			MessageScheduler.INSTANCE.sendMessageAfterCooldown(Constants.PREFIX.get().getString() + " " + place + ": " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ(), false);
		} else {
			//send fail message
			if (CLIENT.player == null || CLIENT.getConnection() == null) {
				return 0;
			}
			CLIENT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.shareFail").withStyle(ChatFormatting.RED)), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	public static int removeWaypoint(String place) {
		if (CLIENT.player == null || CLIENT.getConnection() == null) {
			return 0;
		}
		if (activeWaypoints.containsKey(place)) {
			CLIENT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.removeSuccess").withStyle(ChatFormatting.GREEN)).append(Component.literal(place).withColor(WAYPOINT_LOCATIONS.get(place).getColor())), false);
			activeWaypoints.remove(place);
			verifiedWaypoints.remove(place);
		} else {
			//send fail message
			CLIENT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.config.mining.crystalsWaypoints.removeFail").withStyle(ChatFormatting.RED)), false);
		}

		return Command.SINGLE_SUCCESS;
	}

	public static void addCustomWaypointFromSocket(CrystalsWaypointMessage... messages) {
		MutableComponent receivedWaypointNames = Component.empty();
		boolean shouldSend = false; // check if empty
		for (CrystalsWaypointMessage message : messages) {
			var category = message.location();
			BlockPos pos = message.coordinates();
			if (activeWaypoints.containsKey(category.getName())) continue;
			if (category == MiningLocationLabel.CrystalHollowsLocationsCategory.FAIRY_GROTTO && !SkyblockerConfigManager.get().mining.crystalsWaypoints.shareFairyGrotto) continue;
			shouldSend = true;

			removeUnknownNear(pos);
			MiningLocationLabel waypoint = new MiningLocationLabel(category, pos);
			waypointsSent2Socket.add(category);
			activeWaypoints.put(category.getName(), waypoint);

			receivedWaypointNames.append(Component.literal(category.getName()).withColor(category.getColor()));
			if (message != messages[messages.length - 1]) {
				receivedWaypointNames.append(", ");
			}
		}

		if (!shouldSend) return;
		assert CLIENT.player != null;
		CLIENT.player.displayClientMessage(Constants.PREFIX.get().append(Component.translatable("skyblocker.webSocket.receivedCrystalsWaypoint", receivedWaypointNames)), false);
	}

	protected static void addCustomWaypoint(String waypointName, BlockPos pos) {
		removeUnknownNear(pos);
		MiningLocationLabel.CrystalHollowsLocationsCategory category = WAYPOINT_LOCATIONS.get(waypointName);
		MiningLocationLabel waypoint = new MiningLocationLabel(category, pos);
		activeWaypoints.put(waypointName, waypoint);
	}

	/**
	 * Removes unknown waypoint from active waypoints if it's close to a location
	 *
	 * @param location center location
	 */
	private static void removeUnknownNear(BlockPos location) {
		String name = MiningLocationLabel.CrystalHollowsLocationsCategory.UNKNOWN.getName();
		MiningLocationLabel unknownWaypoint = activeWaypoints.getOrDefault(name, null);
		if (unknownWaypoint != null) {
			double distance = unknownWaypoint.centerPos.distanceTo(location.getCenter());
			if (distance < REMOVE_UNKNOWN_DISTANCE) {
				activeWaypoints.remove(name);
			}
		}
	}

	private static void extractRendering(PrimitiveCollector collector) {
		if (SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled) {
			for (MiningLocationLabel crystalsWaypoint : activeWaypoints.values()) {
				crystalsWaypoint.extractRendering(collector);
			}
		}
	}

	private static void onLocationChange(Location newLocation) {
		if (newLocation == Location.CRYSTAL_HOLLOWS) {
			WsStateManager.subscribeServer(Service.CRYSTAL_WAYPOINTS, Optional.of(CrystalsWaypointSubscribeMessage.create(CLIENT.level)));
		}
	}

	private static void reset() {
		activeWaypoints.clear();
		verifiedWaypoints.clear();
		waypointsSent2Socket.clear();
	}

	private static void update() {
		if (CLIENT.player == null || CLIENT.getConnection() == null || !SkyblockerConfigManager.get().mining.crystalsWaypoints.enabled || !Utils.isInCrystalHollows()) {
			return;
		}

		//get if the player is in the crystals
		@SuppressWarnings("deprecation")
		String location = Utils.getIslandArea().substring(2);
		//if new location and needs waypoint add waypoint, and if socket hasn't received waypoint send it
		if (!location.equals("Unknown") && WAYPOINT_LOCATIONS.containsKey(location)) {
			if (!activeWaypoints.containsKey(location)) {
				//add waypoint at player location
				BlockPos playerLocation = CLIENT.player.blockPosition();
				addCustomWaypoint(location, playerLocation);
			}

			trySendWaypoint2Socket(WAYPOINT_LOCATIONS.get(location));
		}
	}

	private static void trySendWaypoint2Socket(MiningLocationLabel.CrystalHollowsLocationsCategory category) {
		if (waypointsSent2Socket.contains(category)) return;
		if (category == MiningLocationLabel.CrystalHollowsLocationsCategory.FAIRY_GROTTO && !SkyblockerConfigManager.get().mining.crystalsWaypoints.shareFairyGrotto) return;

		WsMessageHandler.sendServerMessage(Service.CRYSTAL_WAYPOINTS, new CrystalsWaypointMessage(category, CLIENT.player.blockPosition()));
		waypointsSent2Socket.add(category);
	}
}
