package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.title.Title;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FishingHelper {
    private static final Title title = new Title("skyblocker.fishing.reelNow", Formatting.GREEN);
    private static long startTime;
    private static long startTimeFish;
    private static Vec3d normalYawVector;

    @Init
    public static void init() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (!Utils.isOnSkyblock()) {
                return TypedActionResult.pass(stack);
            }
            if (stack.getItem() instanceof FishingRodItem) {
                if (player.fishHook == null) {
                    start(player);
                } else {
                    reset();
                }
            }
            return TypedActionResult.pass(stack);
        });
        WorldRenderEvents.AFTER_TRANSLUCENT.register(FishingHelper::render);
    }

    public static void start(PlayerEntity player) {
        startTime = System.currentTimeMillis();
        startTimeFish = System.currentTimeMillis();
        float yawRad = player.getYaw() * 0.017453292F;
        normalYawVector = new Vec3d(-MathHelper.sin(yawRad), 0, MathHelper.cos(yawRad));
    }

    public static void reset() {
        startTime = 0;
        startTimeFish = 0;
    }

    public static void resetFish() {
        startTimeFish = 0;
    }

    public static void onSound(PlaySoundS2CPacket packet) {
        String path = packet.getSound().value().getId().getPath();
        if (SkyblockerConfigManager.get().helpers.fishing.enableFishingHelper && startTimeFish != 0 && System.currentTimeMillis() >= startTimeFish + 2000 && ("entity.generic.splash".equals(path) || "entity.player.splash".equals(path))) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && player.fishHook != null) {
                Vec3d soundToFishHook = player.fishHook.getPos().subtract(packet.getX(), 0, packet.getZ());
                if (Math.abs(normalYawVector.x * soundToFishHook.z - normalYawVector.z * soundToFishHook.x) < 0.2D && Math.abs(normalYawVector.dotProduct(soundToFishHook)) < 4D && player.getPos().squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) > 1D) {
                    RenderHelper.displayInTitleContainerAndPlaySound(title, 10);
                    resetFish();
                }
            } else {
                reset();
            }
        }
    }

    public static void render(WorldRenderContext context) {
        if (SkyblockerConfigManager.get().helpers.fishing.enableFishingTimer && startTime != 0) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && player.fishHook != null) {
                float time = (int) ((System.currentTimeMillis() - startTime) / 100f) / 10f; //leave 1dp in seconds
                float scale = SkyblockerConfigManager.get().helpers.fishing.fishingTimerScale;
                Vec3d pos = player.fishHook.getPos().add(0, 0.4 + scale / 10, 0);

                Text text;
                if (time >= 20 && SkyblockerConfigManager.get().helpers.fishing.changeTimerColor) {
                    text = Text.literal(String.valueOf(time)).formatted(Formatting.GREEN);
                } else {
                    text = Text.literal(String.valueOf(time));
                }

                RenderHelper.renderText(context, text, pos, scale, true);
            }
        }
    }
}
