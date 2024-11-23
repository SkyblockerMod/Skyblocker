package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.CorpseTypeArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CorpseFinder {
    private static final Location LOCATION = Location.GLACITE_MINESHAFT;
    private static boolean isLocationCorrect = false;
    private static final Pattern CORPSE_FOUND_PATTERN = Pattern.compile("([A-Z]+) CORPSE LOOT!");
	private static final Pattern COORDS_PATTERN = Pattern.compile("x: (?<x>-?\\d+), y: (?<y>-?\\d+), z: (?<z>-?\\d+)(?:.+)?");
    private static final String PREFIX = "[Skyblocker Corpse Finder] ";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorpseFinder.class);
    private static final Map<String, List<Corpse>> corpsesByType = new HashMap<>();
	private static final Set<String> ITEM_IDS = Set.of(
			"LAPIS_ARMOR_HELMET",
			"ARMOR_OF_YOG_HELMET",
			"MINERAL_HELMET",
			"VANGUARD_HELMET"
	);

    @Init
    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> {
			isLocationCorrect = false;
			corpsesByType.clear();
        });
        SkyblockEvents.LOCATION_CHANGE.register(CorpseFinder::handleLocationChange);
        ClientReceiveMessageEvents.GAME.register(CorpseFinder::onChatMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CorpseFinder::renderWaypoints);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || client.player == null) return;
            if (!isLocationCorrect) return;
            for (List<Corpse> corpses : corpsesByType.values()) {
                for (Corpse corpse : corpses) {
                    if (!corpse.seen && client.player.canSee(corpse.entity)) {
                        setSeen(corpse);
                    }
				}
			}
		});
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
            .then(literal("corpseHelper")
                .then(literal("shareLocation")
                    .then(argument("blockPos", ClientBlockPosArgumentType.blockPos())
                        .then(argument("corpseType", CorpseTypeArgumentType.corpseType())
                            .executes(context -> {
                                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + "[Skyblocker] " + "Corpse " + StringArgumentType.getString(context, "corpseType") + " found at " + ClientBlockPosArgumentType.getBlockPos(context, "blockPos").toShortString() + "!");
                                return Command.SINGLE_SUCCESS;
                            })
                        )
                    )
                )
            )
        ));
    }

    private static boolean seenDebugWarning = false;

    private static void handleLocationChange(Location location) {
        isLocationCorrect = location == LOCATION;   // true if mineshafts else false
    }

    public static void checkIfCorpse(Entity entity) {
        if (entity instanceof ArmorStandEntity armorStand) checkIfCorpse(armorStand);
    }

    public static void checkIfCorpse(ArmorStandEntity armorStand) {
        if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder) return;
        if (armorStand.hasCustomName() || armorStand.isInvisible() || !armorStand.shouldHideBasePlate()) return;
        if (isLocationCorrect) handleArmorStand(armorStand);
    }

    private static void handleArmorStand(ArmorStandEntity armorStand) {
        for (ItemStack stack : armorStand.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            if (!ITEM_IDS.contains(itemId)) continue;
            LOGGER.info(PREFIX + "Triggered code for handleArmorStand and matched with ITEM_IDS");
            List<Corpse> corpses = corpsesByType.computeIfAbsent(itemId, k -> new ArrayList<>());
            if (corpses.stream().noneMatch(c -> c.entity.getBlockPos().equals(armorStand.getBlockPos()))) {
                Waypoint corpseWaypoint;
                float[] color = getColors(getColor(armorStand));
                corpseWaypoint = new Waypoint(armorStand.getBlockPos(), Waypoint.Type.OUTLINED_WAYPOINT, color);
                if (Debug.debugEnabled() && !seenDebugWarning && (seenDebugWarning = true)) LOGGER.warn(PREFIX + "Debug mode is active! Please use it only for sake of debugging corpse detection!");
                Corpse newCorpse = new Corpse(armorStand, corpseWaypoint);
                corpses.add(newCorpse);
            }
		}
	}

    private static void renderWaypoints(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || !isLocationCorrect) return;
        for (List<Corpse> corpses : corpsesByType.values()) {
            for (Corpse corpse : corpses) {
                if (corpse.waypoint.shouldRender() && (corpse.seen || Debug.debugEnabled())) {
                    corpse.waypoint.render(context);
                }}}}

    private static void onChatMessage(Text text, boolean overlay) {
        if (!isLocationCorrect || !SkyblockerConfigManager.get().mining.glacite.enableCorpseFinder || overlay) return;
        if (SkyblockerConfigManager.get().mining.glacite.enableParsingChatCorpseFinder) parseCords(text);  // parsing cords from chat

        Matcher matcherCorpse = CORPSE_FOUND_PATTERN.matcher(text.getString());
        if (matcherCorpse.find()) {
            LOGGER.info(PREFIX + "Triggered code for onChatMessage");
            LOGGER.warn(PREFIX + "State of corpsesByType: {}", corpsesByType);
            String corpseType = matcherCorpse.group(1).toUpperCase();
            String key = switch (corpseType) {   // there is probably less stupid way to do this
                case "LAPIS" -> "LAPIS_ARMOR_HELMET";
                case "UMBER" -> "ARMOR_OF_YOG_HELMET";
                case "TUNGSTEN" -> "MINERAL_HELMET";
                case "VANGUARD" -> "VANGUARD_HELMET";
                default -> "";
            };
            if (MinecraftClient.getInstance().player == null) return;
            List<Corpse> corpses = corpsesByType.get(key);
            if (corpses == null) {
                LOGGER.warn(PREFIX + "Couldn't get corpses! corpseType: {}, key: {}", corpseType, key);
                return;
            }
            Corpse closestCorpse = null;
            float closestDistance = 100;
            for (Corpse corpse : corpses) {
                float distance = corpse.entity.distanceTo(MinecraftClient.getInstance().player);
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closestCorpse = corpse;
                }
            }
            if (closestCorpse != null) {
                LOGGER.info(PREFIX + "Found corpse, marking as found! {}", closestCorpse.entity);
                closestCorpse.waypoint.setFound();
            } else LOGGER.warn(PREFIX + "Couldn't find the closest corpse despite triggering onChatMessage!");
        }
    }

    private static void setSeen(Corpse corpse) {
        corpse.seen = true;
        if (System.currentTimeMillis() - corpse.messageLastSent < 300)  return;

        corpse.messageLastSent = System.currentTimeMillis();
        MinecraftClient.getInstance().player.sendMessage(
            Constants.PREFIX.get()
                .append("Found a ")
                .append(Text.literal(corpse.name + " Corpse")
                    .withColor(corpse.color.getColorValue()))
                .append(" at " + corpse.entity.getBlockPos().up(0).toShortString() + "!")
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/skyblocker corpseHelper shareLocation " + PosUtils.toSpaceSeparatedString(corpse.waypoint.pos) + " " + corpse.name))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to share the location in chat!").formatted(Formatting.GREEN)))));
    }

    static String getType(ArmorStandEntity entity) {
        for (ItemStack stack : entity.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            switch (itemId) {
                case ("LAPIS_ARMOR_HELMET"):
                    return "LAPIS";
                case ("ARMOR_OF_YOG_HELMET"):
                    return "UMBER";
                case ("MINERAL_HELMET"):
                    return "TUNGSTEN";
                case ("VANGUARD_HELMET"):
                    return "VANGUARD";
            }
        }
        return "UNKNOWN";
    }

    private static Formatting getColor(ArmorStandEntity entity) {
        String itemId = ItemUtils.getItemId(entity.getEquippedStack(EquipmentSlot.HEAD));
        if (ITEM_IDS.contains(itemId)) {
            switch (itemId) {
	            case "LAPIS_ARMOR_HELMET", "VANGUARD_HELMET": return Formatting.BLUE; // dark blue looks bad and those two never exist in same shaft
	            case "ARMOR_OF_YOG_HELMET": return Formatting.RED;
	            case "MINERAL_HELMET": return Formatting.GRAY;
            }
		}

        LOGGER.warn(PREFIX + "Couldn't match a color! Something probably went very wrong!");
        return Formatting.YELLOW;
    }

    @SuppressWarnings("DataFlowIssue")
    private static float[] getColors(Formatting color) {
        return ColorUtils.getFloatComponents(color.getColorValue());
    }

    private static void parseCords(Text text) {
        String message = text.getString();
        Matcher matcher = COORDS_PATTERN.matcher(message);
        if (matcher.find()) {
            int x = Integer.parseInt(matcher.group("x"));
            int y = Integer.parseInt(matcher.group("y"));
            int z = Integer.parseInt(matcher.group("z"));
            LOGGER.info(PREFIX + "Parsed message! X:{}, Y:{}, Z:{}", x, y, z);
            boolean foundCorpse = false;
            BlockPos parsedPos = new BlockPos(x-1, y-1, z-1);   // skyhanni cords format difference is -1, -1, -1
            for (List<Corpse> corpses : corpsesByType.values()) {
                for (Corpse corpse : corpses) {
                    if (corpse.waypoint.pos.equals(parsedPos)) {
                        corpse.seen = true;
                        foundCorpse = true;
                        LOGGER.info(PREFIX + "Setting corpse {} as seen!", corpse.entity);
                        MinecraftClient.getInstance().player.sendMessage(
                            Constants.PREFIX.get()
                                .append("Parsed message from chat, adding corpse at ")
                                .append(corpse.entity.getBlockPos().up(0).toShortString()));
                    }
				}
			}
            if (!foundCorpse) {
                LOGGER.warn(PREFIX + "Did NOT find any match for corpses! corpsesByType.values(): {}", corpsesByType.values());
                LOGGER.info(PREFIX + "Proceeding to iterate over all corpses!");
                for (List<Corpse> corpses : corpsesByType.values()) {
                    for (Corpse corpse : corpses) {
                        LOGGER.info(PREFIX + "Corpse: {}, BlockPos: {}", corpse.entity, corpse.entity.getBlockPos());
                    }
                }
            }
        }
    }

	static class Corpse {
		private final ArmorStandEntity entity;
		private final Waypoint waypoint;
		private boolean seen;
		private long messageLastSent = 0;
		private final Formatting color;
		private final String name;

		Corpse(ArmorStandEntity entity, Waypoint waypoint) {
			this.entity = entity;
			this.waypoint = waypoint;
			this.seen = false;
			this.color = getColor(entity);
			this.name = getType(entity);
		}
	}
}
