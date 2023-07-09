package me.xmrvizzy.skyblocker.skyblock;

import me.xmrvizzy.skyblocker.config.SkyblockerConfig;
import me.xmrvizzy.skyblocker.utils.RenderHelper;
import me.xmrvizzy.skyblocker.utils.title.Title;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class FishingHelper {
    private static final Title title = new Title("skyblocker.fishing.reelNow", Formatting.GREEN);
    private static long startTime;
    private static Vec3d normalYawVector;

    public static void init() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof FishingRodItem) {
                if (player.fishHook == null) {
                    start(player);
                } else {
                    reset();
                }
            }
            return TypedActionResult.pass(stack);
        });
    }

    public static void start(PlayerEntity player) {
        startTime = System.currentTimeMillis();
        float yawRad = player.getYaw() * 0.017453292F;
        normalYawVector = new Vec3d(-MathHelper.sin(yawRad), 0, MathHelper.cos(yawRad));
    }

    public static void reset() {
        startTime = 0;
    }

    public static void onSound(MinecraftClient client, PlaySoundS2CPacket packet) {
        String path = packet.getSound().value().getId().getPath();
        if (SkyblockerConfig.get().general.fishing.enableFishingHelper && startTime != 0 && System.currentTimeMillis() >= startTime + 2000 && ("entity.generic.splash".equals(path) || "entity.player.splash".equals(path))) {
            ClientPlayerEntity player = client.player;
            if (player != null && player.fishHook != null) {
                Vec3d soundToFishHook = player.fishHook.getPos().subtract(packet.getX(), 0, packet.getZ());
                if (Math.abs(normalYawVector.x * soundToFishHook.z - normalYawVector.z * soundToFishHook.x) < 0.2D && Math.abs(normalYawVector.dotProduct(soundToFishHook)) < 4D && player.getPos().squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) > 1D) {
                    RenderHelper.displayInTitleContainerAndPlaySound(title, 10);
                    reset();
                }
            } else {
                reset();
            }
        }
    }
}
