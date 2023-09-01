package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

public class SecretWaypoint {
    static final List<String> SECRET_ITEMS = List.of("Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
    final int secretIndex;
    final Category category;
    private final Text name;
    private final BlockPos pos;
    private final Vec3d centerPos;
    private boolean missing;

    SecretWaypoint(int secretIndex, JsonObject waypoint, String name, BlockPos pos) {
        this.secretIndex = secretIndex;
        this.category = Category.get(waypoint);
        this.name = Text.of(name);
        this.pos = pos;
        this.centerPos = pos.toCenterPos();
        this.missing = true;
    }

    static ToDoubleFunction<SecretWaypoint> getSquaredDistanceToFunction(Entity entity) {
        return secretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos);
    }

    static Predicate<SecretWaypoint> getRangePredicate(Entity entity) {
        return secretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos) <= 36D;
    }

    boolean shouldRender() {
        return category.isEnabled() && missing;
    }

    boolean needsInteraction() {
        return category.needsInteraction();
    }

    boolean isLever() {
        return category.isLever();
    }

    boolean needsItemPickup() {
        return category.needsItemPickup();
    }

    void setFound() {
        this.missing = false;
    }

    void setMissing() {
        this.missing = true;
    }

    /**
     * Renders the secret waypoint, including a filled cube, a beacon beam, the name, and the distance from the player.
     */
    void render(WorldRenderContext context) {
        RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, pos, category.colorComponents, 0.5F);
        Vec3d posUp = centerPos.add(0, 1, 0);
        RenderHelper.renderText(context, name, posUp, true);
        double distance = context.camera().getPos().distanceTo(centerPos);
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
    }

    enum Category {
        ENTRANCE(secretWaypoints -> secretWaypoints.enableEntranceWaypoints, 0, 255, 0),
        SUPERBOOM(secretWaypoints -> secretWaypoints.enableSuperboomWaypoints, 255, 0, 0),
        CHEST(secretWaypoints -> secretWaypoints.enableChestWaypoints, 2, 213, 250),
        ITEM(secretWaypoints -> secretWaypoints.enableItemWaypoints, 2, 64, 250),
        BAT(secretWaypoints -> secretWaypoints.enableBatWaypoints, 142, 66, 0),
        WITHER(secretWaypoints -> secretWaypoints.enableWitherWaypoints, 30, 30, 30),
        LEVER(secretWaypoints -> secretWaypoints.enableLeverWaypoints, 250, 217, 2),
        FAIRYSOUL(secretWaypoints -> secretWaypoints.enableFairySoulWaypoints, 255, 85, 255),
        STONK(secretWaypoints -> secretWaypoints.enableStonkWaypoints, 146, 52, 235),
        DEFAULT(secretWaypoints -> secretWaypoints.enableDefaultWaypoints, 190, 255, 252);
        private final Predicate<SkyblockerConfig.SecretWaypoints> enabledPredicate;
        private final float[] colorComponents;

        Category(Predicate<SkyblockerConfig.SecretWaypoints> enabledPredicate, int... intColorComponents) {
            this.enabledPredicate = enabledPredicate;
            colorComponents = new float[intColorComponents.length];
            for (int i = 0; i < intColorComponents.length; i++) {
                colorComponents[i] = intColorComponents[i] / 255F;
            }
        }

        private static Category get(JsonObject categoryJson) {
            return switch (categoryJson.get("category").getAsString()) {
                case "entrance" -> Category.ENTRANCE;
                case "superboom" -> Category.SUPERBOOM;
                case "chest" -> Category.CHEST;
                case "item" -> Category.ITEM;
                case "bat" -> Category.BAT;
                case "wither" -> Category.WITHER;
                case "lever" -> Category.LEVER;
                case "fairysoul" -> Category.FAIRYSOUL;
                case "stonk" -> Category.STONK;
                default -> Category.DEFAULT;
            };
        }

        boolean needsInteraction() {
            return this == CHEST || this == WITHER;
        }

        boolean isLever() {
            return this == LEVER;
        }

        boolean needsItemPickup() {
            return this == ITEM || this == BAT;
        }

        boolean isEnabled() {
            return enabledPredicate.test(SkyblockerConfig.get().locations.dungeons.secretWaypoints);
        }
    }
}
