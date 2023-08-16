package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class SecretWaypoint {
    static final List<String> SECRET_ITEMS = List.of("Decoy", "Defuse Kit", "Dungeon Chest Key", "Healing VIII", "Inflatable Jerry", "Spirit Leap", "Training Weights", "Trap", "Treasure Talisman");
    final int secretIndex;
    final Category category;
    private final Text name;
    final BlockPos pos;
    final Vec3d centerPos;
    private boolean missing;

    SecretWaypoint(int secretIndex, JsonObject waypoint, String name, BlockPos pos) {
        this.secretIndex = secretIndex;
        this.category = Category.get(waypoint);
        this.name = Text.of(name);
        this.pos = pos;
        this.centerPos = pos.toCenterPos();
        this.missing = true;
    }

    public boolean isMissing() {
        return missing;
    }

    public void setFound() {
        this.missing = false;
    }

    void render(WorldRenderContext context) {
        RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, pos, category.colorComponents, 0.5F);
        Vec3d posUp = centerPos.add(0, 1, 0);
        RenderHelper.renderText(context, name, posUp, true);
        double distance = context.camera().getPos().distanceTo(centerPos);
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), posUp, 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
    }

    enum Category {
        ENTRANCE(0, 255, 0),
        SUPERBOOM(255, 0, 0),
        CHEST(2, 213, 250),
        ITEM(2, 64, 250),
        BAT(142, 66, 0),
        WITHER(30, 30, 30),
        LEVER(250, 217, 2),
        FAIRYSOUL(255, 85, 255),
        STONK(146, 52, 235),
        DEFAULT(190, 255, 252);
        private final float[] colorComponents;

        Category(int... intColorComponents) {
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

        boolean needsItemPickup() {
            return this == ITEM || this == BAT;
        }
    }
}
