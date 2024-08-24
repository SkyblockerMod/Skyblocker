package de.hysky.skyblocker.skyblock.dwarven;

import com.mojang.brigadier.Command;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.debug.Debug;
import de.hysky.skyblocker.events.SkyblockEvents;
import de.hysky.skyblocker.utils.*;
import de.hysky.skyblocker.utils.command.argumenttypes.CorpseTypeArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientBlockPosArgumentType;
import de.hysky.skyblocker.utils.command.argumenttypes.blockpos.ClientPosArgument;
import de.hysky.skyblocker.utils.scheduler.MessageScheduler;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

// 9 forward (+X), 3 right (+Z), 12 down (-Y)
// Y is pulled out of my ass idk
// portal frame can be used to locate

public class CorpseHelper {
    private static final Location LOCATION = Location.GLACITE_MINESHAFT;
    private static boolean isLocationCorrect = false;
    private static final Pattern corpseFoundPattern = Pattern.compile("([A-Z]+) CORPSE LOOT!");
    private static final int SEARCH_RADIUS = 6;
    private static final String PREFIX = "[Skyblocker Corpse Helper] ";
    private static final Logger LOGGER = LoggerFactory.getLogger(CorpseHelper.class);
    private static final Map<String, List<Corpse>> corpsesByType = new HashMap<>();

    public static void init() {
        ClientPlayConnectionEvents.JOIN.register((ignored, ignored2, ignored3) -> isLocationCorrect = false);
        SkyblockEvents.LOCATION_CHANGE.register(CorpseHelper::handleLocationChange);
        ClientReceiveMessageEvents.GAME.register(CorpseHelper::onChatMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CorpseHelper::renderWaypoints);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!true || client.player == null) return; // config
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
                                MessageScheduler.INSTANCE.sendMessageAfterCooldown("/pc " + "[Skyblocker] " + "Corpse " + context.getArgument("corpseType", String.class) + " found at " + context.getArgument("blockPos", ClientPosArgument.class).toAbsoluteBlockPos(context.getSource()).toShortString());
                                return Command.SINGLE_SUCCESS;
                            })))))));
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(literal(SkyblockerMod.NAMESPACE)
            .then(literal("corpseHelper")
                .then(literal("markStart")
                    .executes(context -> {
                        markStart(true);
                        return Command.SINGLE_SUCCESS;
                            })))));

    }

    private static void handleLocationChange(Location location) {
        isLocationCorrect = location == LOCATION;   // true if mineshafts else false
        if (isLocationCorrect) markStart(false);
    }

    public static void checkIfCorpse(Entity entity) {
        if (entity instanceof ArmorStandEntity armorStand) checkIfCorpse(armorStand);
    }

    public static void checkIfCorpse(ArmorStandEntity armorStand) {
        if (!true) return;  // config
        if (armorStand.hasCustomName() || armorStand.isInvisible() || !armorStand.shouldHideBasePlate()) return;
        if (isLocationCorrect) handleArmorStand(armorStand);
    }

    private static boolean seenDebugWarning = false;
    private static void handleArmorStand(ArmorStandEntity armorStand) {
        for (ItemStack stack : armorStand.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            if (ITEM_IDS.contains(itemId)) {
                LOGGER.info(PREFIX + "Triggered code for handleArmorStand and matched with ITEM_IDS");
                List<Corpse> corpses = corpsesByType.computeIfAbsent(itemId, k -> new ArrayList<>());
                if (corpses.stream().noneMatch(c -> c.entity.getBlockPos().equals(armorStand.getBlockPos()))) {
                    Waypoint corpseWaypoint;
                    float[] color = getColors(getColor(armorStand));
                    corpseWaypoint = new Waypoint(armorStand.getBlockPos(), Waypoint.Type.OUTLINED_WAYPOINT, color);
                    if (Debug.debugEnabled()) if (!seenDebugWarning && (seenDebugWarning = true)) LOGGER.warn(PREFIX + "Debug mode is active! Please use it only for sake of debugging corpse detection!");
                    Corpse newCorpse = new Corpse(armorStand, corpseWaypoint, false);
                    corpses.add(newCorpse);
                }
            }
        }
    }

    private static ArrayList<Waypoint> WAYPOINTS = new ArrayList<>(List.of());
    private static void renderWaypoints(WorldRenderContext context) {
        if (!true) return;  // config
        if (!isLocationCorrect) return;
        for (Waypoint waypoint : WAYPOINTS) {
            if (waypoint != null && waypoint.shouldRender()) waypoint.render(context);
        }
        for (List<Corpse> corpses : corpsesByType.values()) {
            for (Corpse corpse : corpses) {
                if (corpse.waypoint.shouldRender() && (corpse.seen || Debug.debugEnabled())) {
                    corpse.waypoint.render(context);
                }
            }
        }
    }

    private static void onChatMessage(Text text, boolean overlay) {
        if (!isLocationCorrect || overlay) return;
        if (true) onMessage(text);  // parsing cords from chat | config

        Matcher matcher = corpseFoundPattern.matcher(text.getString());
        if (matcher.find()) {
            LOGGER.info(PREFIX + "Triggered code for onChatMessage");
            LOGGER.warn(PREFIX + "State of corpsesByType: {}", corpsesByType);
            String corpseType = matcher.group(1).toUpperCase();
            String key = switch (corpseType) {   // there is probably less stupid way to do this
                case ("LAPIS") -> "LAPIS_ARMOR_HELMET";
                case ("UMBER") -> "ARMOR_OF_YOG_HELMET";
                case ("TUNGSTEN") -> "MINERAL_HELMET";
                case ("VANGUARD") -> "VANGUARD_HELMET";
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
                closestCorpse.opened = true;
            } else LOGGER.warn(PREFIX + "Couldn't find closest corpse despite triggering onChatMessage!");
        }
    }

    private static void setSeen(Corpse corpse) {
        corpse.seen = true;
        if (!true || System.currentTimeMillis() - corpse.messageLastSent < 300)   // config
            return;

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

    static class Corpse {
        private final ArmorStandEntity entity;
        private final Waypoint waypoint;
        private boolean opened;
        private boolean seen;
        private long messageLastSent = 0;
        private Formatting color = Formatting.YELLOW;
        private float[] colors;
        private String name;

        Corpse(ArmorStandEntity entity, Waypoint waypoint, boolean opened) {
            this.entity = entity;
            this.waypoint = waypoint;
            this.opened = opened;
            this.seen = false;
            this.color = getColor(entity);
            this.colors = getColors(color);
            this.name = getType(entity);
        }

        Corpse(ArmorStandEntity entity, Waypoint waypoint, boolean opened, Formatting color) {
            this.entity = entity;
            this.waypoint = waypoint;
            this.opened = opened;
            this.seen = false;
            this.color = color;
            this.colors = getColors(color);
            this.name = getType(entity);
        }

    }

    private static final Set<String> ITEM_IDS = Set.of(
            "LAPIS_ARMOR_HELMET",
            "ARMOR_OF_YOG_HELMET",
            "MINERAL_HELMET",
            "VANGUARD_HELMET"
    );

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
        for (ItemStack stack : entity.getArmorItems()) {
            String itemId = ItemUtils.getItemId(stack);
            if (ITEM_IDS.contains(itemId)) {
                switch (itemId) {
                    case ("LAPIS_ARMOR_HELMET"), ("VANGUARD_HELMET"): return Formatting.BLUE; // dark blue looks bad and those two never exist in same shaft
                    case ("ARMOR_OF_YOG_HELMET"): return Formatting.YELLOW;
                    case ("MINERAL_HELMET"): return Formatting.GRAY;
                }
            }
        }
        LOGGER.warn(PREFIX + "Couldn't match a color! Something probably went very wrong!");
        return Formatting.YELLOW;
    }

    private static void markStart(boolean bypassLocationCheck) {
        if (!isLocationCorrect && !bypassLocationCheck || !true) return;    // config
        LOGGER.info(PREFIX + "markStart triggered!");
        try {
            BlockPos portal_loc = null;
            MinecraftClient client = MinecraftClient.getInstance();

            if (!((client.player == null) || (client.world == null))) {
                World world = client.world;
                BlockPos playerPos = client.player.getBlockPos();
                LOGGER.info(PREFIX + "Iteration started!");
                for (int x = -SEARCH_RADIUS; x <= SEARCH_RADIUS; x++) {
                    for (int y = -SEARCH_RADIUS; y <= SEARCH_RADIUS - 3; y++) {
                        for (int z = -SEARCH_RADIUS; z <= SEARCH_RADIUS; z++) {
                            LOGGER.info(PREFIX + "(ITERATOR)" + "Iteration! x={}, y={}, z={}", x, y, z);
                            BlockPos currentPos = playerPos.add(x, y, z);
                            Block currentBlock = world.getBlockState(currentPos).getBlock();
                            LOGGER.info(PREFIX + "(ITERATOR)" + "currentPos: {}, currentBlock: {}", currentPos, currentBlock);
                            if (currentBlock == Blocks.END_PORTAL_FRAME) {
                                portal_loc = currentPos;
                                LOGGER.info(PREFIX + "Found End Portal Frame (start of shaft) at {}", portal_loc.toString());
                            }
                        }
                    }
                }
            }
            if (portal_loc != null) {
                BlockPos ladder_loc = portal_loc.add(9, 3, -12);
                WAYPOINTS.add(new Waypoint(ladder_loc, Waypoint.Type.OUTLINED_WAYPOINT, ColorUtils.getFloatComponents(Formatting.YELLOW.getColorValue())));
            } else {
                LOGGER.warn(PREFIX + "Couldn't find END_PORTAL_FRAME to mark starting point of Glacite Mineshaft!");
                if (client.player == null) LOGGER.warn(PREFIX + "client.player == null!");
                if (client.world == null) LOGGER.warn(PREFIX + "client.world == null!");
            }
        } catch (Exception e) {
            LOGGER.error(PREFIX + "Something went very wrong!");
            LOGGER.error(PREFIX + e);
        }
    }

    private static float[] getColors(Formatting color) {
        return ColorUtils.getFloatComponents(color.getColorValue());
    }

    private static void onMessage(Text text) {
        if (Utils.isOnSkyblock() && isLocationCorrect && true) {    // config
            Pattern cordsPattern = Pattern.compile("x: (?<x>-?\\d+), y: (?<y>-?\\d+), z: (?<z>-?\\d+)(?:.+)?");
            String message = text.getString();
            Matcher matcher = cordsPattern.matcher(message);
            if (matcher.find()) {
                try {
                    int x = Integer.parseInt(matcher.group("x"));
                    int y = Integer.parseInt(matcher.group("y"));
                    int z = Integer.parseInt(matcher.group("z"));
                    LOGGER.info(PREFIX + "Parsed message! X:{}, Y:{}, Z:{}", x, y, z);
                    boolean foundCorpse = false;
                    for (List<Corpse> corpses : corpsesByType.values()) {
                        for (Corpse corpse : corpses) {
                            if (corpse.waypoint.pos == new BlockPos(x,y,z)) {
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
                        LOGGER.warn(PREFIX + "Did NOT found any match for corpses! corpsesByType.values(): {}", corpsesByType.values());
                        LOGGER.info(PREFIX + "Proceeding to iterate over all corpses!");
                        for (List<Corpse> corpses : corpsesByType.values()) {
                            for (Corpse corpse : corpses) {
                                LOGGER.info(PREFIX + "Corpse: {}, BlockPos: {}", corpse.entity, corpse.entity.getBlockPos());
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(PREFIX + "Error while handling chat message:" + e);
                }
            }
        }
    }
}
