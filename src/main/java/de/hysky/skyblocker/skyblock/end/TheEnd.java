package de.hysky.skyblocker.skyblock.end;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.hysky.skyblocker.SkyblockerMod;
import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.events.HudRenderEvents;
import de.hysky.skyblocker.utils.ColorUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class TheEnd {
    protected static final Logger LOGGER = LoggerFactory.getLogger(TheEnd.class);

    public static Set<UUID> hitZealots = new HashSet<>();
    public static int zealotsSinceLastEye = 0;
    public static int zealotsKilled = 0;
    public static int eyes = 0;
    /**
     * needs to be saved?
     */
    private static boolean dirty = false;
    private static String currentProfile = "";
    private static JsonObject PROFILES_STATS;

    private static final Path FILE = SkyblockerMod.CONFIG_DIR.resolve("end.json");
    public static List<ProtectorLocation> protectorLocations = List.of(
            new ProtectorLocation(-649, -219, Text.translatable("skyblocker.end.hud.protectorLocations.left")),
            new ProtectorLocation(-644, -269, Text.translatable("skyblocker.end.hud.protectorLocations.front")),
            new ProtectorLocation(-689, -273, Text.translatable("skyblocker.end.hud.protectorLocations.center")),
            new ProtectorLocation(-727, -284, Text.translatable("skyblocker.end.hud.protectorLocations.back")),
            new ProtectorLocation(-639, -328, Text.translatable("skyblocker.end.hud.protectorLocations.rightFront")),
            new ProtectorLocation(-678, -332, Text.translatable("skyblocker.end.hud.protectorLocations.rightBack"))
    );

    public static ProtectorLocation currentProtectorLocation = null;
    public static int stage = 0;

    @Init
    public static void init() {
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (entity instanceof EndermanEntity enderman && isZealot(enderman)) {
                hitZealots.add(enderman.getUuid());
            }
            return ActionResult.PASS;
        });


        HudRenderEvents.AFTER_MAIN_HUD.register((drawContext, tickCounter) -> {
            if (!Utils.isInTheEnd()) return;
            if (!SkyblockerConfigManager.get().otherLocations.end.hudEnabled) return;

            EndHudWidget.INSTANCE.render(drawContext, SkyblockerConfigManager.get().uiAndVisuals.tabHud.enableHudBackground);
        });

        ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
            String lowerCase = Utils.getIslandArea().toLowerCase();
            if (Utils.isInTheEnd() || lowerCase.contains("the end") || lowerCase.contains("dragon's nest")) {
                ChunkPos pos = chunk.getPos();
                //
                Box box = new Box(pos.getStartX(), 0, pos.getStartZ(), pos.getEndX() + 1, 1, pos.getEndZ() + 1);
                for (ProtectorLocation protectorLocation : protectorLocations) {
                    if (box.contains(protectorLocation.x(), 0.5, protectorLocation.z())) {
                        // MinecraftClient.getInstance().player.sendMessage(Text.literal("Checking: ").append(protectorLocation.name));//MinecraftClient.getInstance().player.sendMessage(Text.literal(pos.getStartX() + " " + pos.getStartZ() + " " + pos.getEndX() + " " + pos.getEndZ()));
                        if (isProtectorHere(world, protectorLocation)) break;
                    }
                }
                if (currentProfile.isEmpty()) load(); // Wacky fix for when you join skyblock, and you are directly in the end (profile id isn't parsed yet most of the time)
            }


        });
        // Reset when changing island
        // TODO: Replace when a changed island event is added
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            resetLocation();
            save();
            load();
            EndHudWidget.INSTANCE.update();
        });
        // Save when leaving as well
        ClientLifecycleEvents.CLIENT_STOPPING.register((client) -> save());

        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!Utils.isInTheEnd() || overlay) return;
            String lowerCase = message.getString().toLowerCase();
            if (lowerCase.contains("tremor")) {
                if (stage == 0) checkAllProtectorLocations();
                else stage += 1;
            }
            else if (lowerCase.contains("rises from below")) stage = 5;
            else if (lowerCase.contains("protector down") || lowerCase.contains("has risen")) resetLocation();
            else return;
            EndHudWidget.INSTANCE.update();
        });

        WorldRenderEvents.AFTER_TRANSLUCENT.register(TheEnd::renderWaypoint);
        ClientLifecycleEvents.CLIENT_STARTED.register((client -> loadFile()));
    }

    private static void checkAllProtectorLocations() {
        ClientWorld world = MinecraftClient.getInstance().world;
        if (world == null) return;
        for (ProtectorLocation protectorLocation : protectorLocations) {
            if (!world.isChunkLoaded(protectorLocation.x() >> 4, protectorLocation.z() >> 4)) continue;
            if (isProtectorHere(world, protectorLocation)) break;
        }
    }

    /**
     * Checks a bunch of Ys to see if a player head is there, if it's there it returns true and updates the hud accordingly
     * @param world le world to check
     * @param protectorLocation protectorLocation to check
     * @return if found
     */
    private static boolean isProtectorHere(ClientWorld world, ProtectorLocation protectorLocation) {
        for (int i = 0; i < 5; i++) {
            if (world.getBlockState(new BlockPos(protectorLocation.x, i + 5, protectorLocation.z)).isOf(Blocks.PLAYER_HEAD)) {
                stage = i + 1;
                currentProtectorLocation = protectorLocation;
                EndHudWidget.INSTANCE.update();
                return true;
            }
        }
        return false;
    }

    private static void resetLocation() {
        stage = 0;
        currentProtectorLocation = null;
    }

    public static void onEntityDeath(Entity entity) {
        if (!(entity instanceof EndermanEntity enderman) || !isZealot(enderman)) return;
        if (hitZealots.contains(enderman.getUuid())) {
            //MinecraftClient.getInstance().player.sendMessage(Text.literal("You killed a zealot!!!"));
            if (isSpecialZealot(enderman)) {
                zealotsSinceLastEye = 0;
                eyes++;
            }
            else zealotsSinceLastEye++;
            zealotsKilled++;
            dirty = true;
            hitZealots.remove(enderman.getUuid());
            EndHudWidget.INSTANCE.update();
        }
    }

    public static boolean isZealot(EndermanEntity enderman) {
        if (enderman.getName().getString().toLowerCase().contains("zealot")) return true; // Future-proof. If they someday decide to actually rename the entities
        assert MinecraftClient.getInstance().world != null;
        List<ArmorStandEntity> entities = MinecraftClient.getInstance().world.getEntitiesByClass(
                ArmorStandEntity.class,
                enderman.getDimensions(null).getBoxAt(enderman.getPos()).expand(1),
                armorStandEntity -> armorStandEntity.getName().getString().toLowerCase().contains("zealot"));
        if (entities.isEmpty()) {
            return false;
        }
        return entities.getFirst().getName().getString().toLowerCase().contains("zealot");
    }

    public static boolean isSpecialZealot(EndermanEntity enderman) {
        return isZealot(enderman) && enderman.getCarriedBlock() != null && enderman.getCarriedBlock().isOf(Blocks.END_PORTAL_FRAME);
    }

    /**
     * Loads if needed
     */
    public static void load() {
        if (!Utils.isOnSkyblock() || Utils.getProfileId().isEmpty()) return;
        String id = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "");
        String profile = Utils.getProfileId();
        if (!profile.equals(currentProfile) && PROFILES_STATS != null) {
            currentProfile = profile;
            JsonElement jsonElement = PROFILES_STATS.get(id);
            if (jsonElement == null) return;
            JsonElement jsonElement1 = jsonElement.getAsJsonObject().get(profile);
            if (jsonElement1 == null) return;
            zealotsKilled = jsonElement1.getAsJsonObject().get("totalZealotKills").getAsInt();
            zealotsSinceLastEye = jsonElement1.getAsJsonObject().get("zealotsSinceLastEye").getAsInt();
            eyes = jsonElement1.getAsJsonObject().get("eyes").getAsInt();
            EndHudWidget.INSTANCE.update();
        }
    }

    private static void loadFile() {
        CompletableFuture.runAsync(() -> {
            try (BufferedReader reader = Files.newBufferedReader(FILE)) {
                PROFILES_STATS = SkyblockerMod.GSON.fromJson(reader, JsonObject.class);
                LOGGER.debug("[Skyblocker End] Loaded end stats");
            } catch (NoSuchFileException ignored) {
                PROFILES_STATS = new JsonObject();
            } catch (Exception e) {
                LOGGER.error("[Skyblocker End] Failed to load end stats", e);
            }
        });
    }

    /**
     * Saves if dirty
     */
    public static void save() {
        if (dirty && PROFILES_STATS != null) {
            String uuid = MinecraftClient.getInstance().getSession().getUuidOrNull().toString().replaceAll("-", "");
            JsonObject jsonObject = PROFILES_STATS.getAsJsonObject(uuid);
            if (jsonObject == null) {
                PROFILES_STATS.add(uuid, new JsonObject());
                jsonObject = PROFILES_STATS.getAsJsonObject(uuid);
            }

            jsonObject.add(currentProfile, new JsonObject());
            JsonElement jsonElement1 = jsonObject.get(currentProfile);

            jsonElement1.getAsJsonObject().addProperty("totalZealotKills", zealotsKilled);
            jsonElement1.getAsJsonObject().addProperty("zealotsSinceLastEye", zealotsSinceLastEye);
            jsonElement1.getAsJsonObject().addProperty("eyes", eyes);

            if (Utils.isOnSkyblock()) {
                CompletableFuture.runAsync(TheEnd::performSave);
            } else {
                performSave();
            }
        }
    }

    private static void performSave() {
        try (BufferedWriter writer = Files.newBufferedWriter(FILE)) {
            SkyblockerMod.GSON.toJson(PROFILES_STATS, writer);
            LOGGER.info("[Skyblocker End] Saved end stats");
            dirty = false;
        } catch (Exception e) {
            LOGGER.error("[Skyblocker End] Failed to save end stats", e);
        }
    }

    private static void renderWaypoint(WorldRenderContext context) {
        if (!SkyblockerConfigManager.get().otherLocations.end.waypoint) return;
        if (currentProtectorLocation == null || stage != 5) return;
        currentProtectorLocation.waypoint().render(context);
    }

    public record ProtectorLocation(int x, int z, Text name, Waypoint waypoint) {
        public ProtectorLocation(int x, int z, Text name) {
            this(x, z, name, new Waypoint(new BlockPos(x, 0, z), Waypoint.Type.WAYPOINT, ColorUtils.getFloatComponents(DyeColor.MAGENTA)));
        }
    }
}
