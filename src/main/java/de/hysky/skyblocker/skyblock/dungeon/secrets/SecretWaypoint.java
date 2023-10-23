package de.hysky.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import de.hysky.skyblocker.config.SkyblockerConfig;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.waypoint.Waypoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;

public class SecretWaypoint extends Waypoint {
    static final List<String> SECRET_ITEMS = List.of("Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
    private static final SkyblockerConfig.SecretWaypoints config = SkyblockerConfigManager.get().locations.dungeons.secretWaypoints;
    private static final Supplier<Type> typeSupplier = () -> config.waypointType;
    final int secretIndex;
    final Category category;
    private final Text name;
    private final Vec3d centerPos;

    SecretWaypoint(int secretIndex, JsonObject waypoint, String name, BlockPos pos) {
        super(pos, typeSupplier, Category.get(waypoint).colorComponents);
        this.secretIndex = secretIndex;
        this.category = Category.get(waypoint);
        this.name = Text.of(name);
        this.centerPos = pos.toCenterPos();
    }

    static ToDoubleFunction<SecretWaypoint> getSquaredDistanceToFunction(Entity entity) {
        return secretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos);
    }

    static Predicate<SecretWaypoint> getRangePredicate(Entity entity) {
        return secretWaypoint -> entity.squaredDistanceTo(secretWaypoint.centerPos) <= 36D;
    }

    @Override
    protected boolean shouldRender() {
        return super.shouldRender() && category.isEnabled();
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

    boolean isBat() {
        return category.isBat();
    }

    /**
     * Renders the secret waypoint, including a filled cube, a beacon beam, the name, and the distance from the player.
     */
    @Override
    protected void render(WorldRenderContext context) {
        //TODO In the future, shrink the box for wither essence and items so its more realistic
        super.render(context);

        if (config.showSecretText) {
            Vec3d posUp = centerPos.add(0, 1, 0);
            RenderHelper.renderText(context, name, posUp, true);
            double distance = context.camera().getPos().distanceTo(centerPos);
            RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
        }
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
        AOTV(secretWaypoints -> secretWaypoints.enableAotvWaypoints, 252, 98, 3),
        PEARL(secretWaypoints -> secretWaypoints.enablePearlWaypoints, 57, 117, 125),
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
                case "aotv" -> Category.AOTV;
                case "pearl" -> Category.PEARL;
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
            return this == ITEM;
        }

        boolean isBat() {
            return this == BAT;
        }

        boolean isEnabled() {
            return enabledPredicate.test(SkyblockerConfigManager.get().locations.dungeons.secretWaypoints);
        }
    }
}
