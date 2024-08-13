package de.hysky.skyblocker.skyblock;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.ItemUtils;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class TeleportOverlay {
    private static final float[] COLOR_COMPONENTS = {118f / 255f, 21f / 255f, 148f / 255f};
    private static final MinecraftClient client = MinecraftClient.getInstance();

    @Init
    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(TeleportOverlay::render);
    }

    private static void render(WorldRenderContext wrc) {
        if (Utils.isOnSkyblock() && SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableTeleportOverlays && client.player != null && client.world != null) {
            ItemStack heldItem = client.player.getMainHandStack();
            String itemId = heldItem.getSkyblockId();
            NbtCompound customData = ItemUtils.getCustomData(heldItem);

            switch (itemId) {
                case "ASPECT_OF_THE_LEECH_1" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
                        render(wrc, 3);
                    }
                }
                case "ASPECT_OF_THE_LEECH_2" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWeirdTransmission) {
                        render(wrc, 4);
                    }
                }
                case "ASPECT_OF_THE_END", "ASPECT_OF_THE_VOID" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission && client.options.sneakKey.isPressed() && customData.getInt("ethermerge") == 1) {
                        render(wrc, customData, 57);
                    } else if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableInstantTransmission) {
                        render(wrc, customData, 8);
                    }
                }
                case "ETHERWARP_CONDUIT" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableEtherTransmission) {
                        render(wrc, customData, 57);
                    }
                }
                case "SINSEEKER_SCYTHE" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableSinrecallTransmission) {
                        render(wrc, customData, 4);
                    }
                }
                case "NECRON_BLADE", "ASTRAEA", "HYPERION", "SCYLLA", "VALKYRIE" -> {
                    if (SkyblockerConfigManager.get().uiAndVisuals.teleportOverlay.enableWitherImpact) {
                        render(wrc, 10);
                    }
                }
            }
        }
    }

    /**
     * Renders the teleport overlay with a given base range and the tuned transmission stat.
     */
    private static void render(WorldRenderContext wrc, NbtCompound customData, int baseRange) {
        render(wrc, customData != null && customData.contains("tuned_transmission") ? baseRange + customData.getInt("tuned_transmission") : baseRange);
    }

    /**
     * Renders the teleport overlay with a given range. Uses {@link MinecraftClient#crosshairTarget} if it is a block and within range. Otherwise, raycasts from the player with the given range.
     *
     * @implNote {@link MinecraftClient#player} and {@link MinecraftClient#world} must not be null when calling this method.
     */
    private static void render(WorldRenderContext wrc, int range) {
        if (client.crosshairTarget != null && client.crosshairTarget.getType() == HitResult.Type.BLOCK && client.crosshairTarget instanceof BlockHitResult blockHitResult && client.crosshairTarget.squaredDistanceTo(client.player) < range * range) {
            render(wrc, blockHitResult);
        } else if (client.interactionManager != null && range > client.player.getAttributeInstance(EntityAttributes.PLAYER_BLOCK_INTERACTION_RANGE).getValue()) {
            HitResult result = client.player.raycast(range, wrc.tickCounter().getTickDelta(true), false);
            if (result.getType() == HitResult.Type.BLOCK && result instanceof BlockHitResult blockHitResult) {
                render(wrc, blockHitResult);
            }
        }
    }

    /**
     * Renders the teleport overlay at the given {@link BlockHitResult}.
     *
     * @implNote {@link MinecraftClient#world} must not be null when calling this method.
     */
    private static void render(WorldRenderContext wrc, BlockHitResult blockHitResult) {
        BlockPos pos = blockHitResult.getBlockPos();
        @SuppressWarnings("DataFlowIssue")
        BlockState state = client.world.getBlockState(pos);
        if (!state.isAir() && client.world.getBlockState(pos.up()).isAir() && client.world.getBlockState(pos.up(2)).isAir()) {
            RenderHelper.renderFilled(wrc, pos, COLOR_COMPONENTS, 0.5f, false);
        }
    }
}
