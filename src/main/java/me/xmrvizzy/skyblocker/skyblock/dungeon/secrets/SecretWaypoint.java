package me.xmrvizzy.skyblocker.skyblock.dungeon.secrets;

import com.google.gson.JsonObject;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

public record SecretWaypoint(int secretIndex, Category category, Text name, BlockPos pos, PlayerEntity player, boolean missing) {
    SecretWaypoint(int secretIndex, JsonObject waypoint, String name, BlockPos pos) {
        this(secretIndex, Category.get(waypoint), Text.of(name), pos, MinecraftClient.getInstance().player, true);
    }

    void render(WorldRenderContext context) {
        RenderHelper.renderFilledThroughWallsWithBeaconBeam(context, pos(), category().colorComponents, 0.5F);
        RenderHelper.renderText(context, name(), pos().up().toCenterPos(), true);
        double distance = player().getPos().distanceTo(pos().toCenterPos());
        RenderHelper.renderText(context, Text.literal(Math.round(distance) + "m").formatted(Formatting.YELLOW), pos().up().toCenterPos(), 1, MinecraftClient.getInstance().textRenderer.fontHeight + 1, true);
    }

    private enum Category {
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
    }
}
