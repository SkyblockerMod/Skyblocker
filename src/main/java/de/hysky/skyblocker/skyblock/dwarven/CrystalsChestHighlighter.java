package de.hysky.skyblocker.skyblock.dwarven;

import de.hysky.skyblocker.annotations.Init;
import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.utils.Utils;
import de.hysky.skyblocker.utils.render.RenderHelper;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

public class CrystalsChestHighlighter {

    private static final MinecraftClient CLIENT = MinecraftClient.getInstance();
    private static final String CHEST_SPAWN_MESSAGE = "You uncovered a treasure chest!";
    private static final long MAX_PARTICLE_LIFE_TIME = 250;
    private static final Vec3d LOCK_HIGHLIGHT_SIZE = new Vec3d(0.1, 0.1, 0.1);

    private static int waitingForChest = 0;
    private static final List<BlockPos> activeChests = new ArrayList<>();
    private static final Object2LongOpenHashMap<Vec3d> activeParticles = new Object2LongOpenHashMap<>();
    private static int currentLockCount = 0;
    private static int neededLockCount = 0;

    @Init
    public static void init() {
        ClientReceiveMessageEvents.GAME.register(CrystalsChestHighlighter::extractLocationFromMessage);
        WorldRenderEvents.AFTER_TRANSLUCENT.register(CrystalsChestHighlighter::render);
        ClientPlayConnectionEvents.JOIN.register((_handler, _sender, _client) -> reset());
    }

    private static void reset() {
        waitingForChest = 0;
        activeChests.clear();
        activeParticles.clear();
        currentLockCount = 0;
    }

    private static void extractLocationFromMessage(Text text, boolean b) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        //if a chest is spawned add chest to look for
        if (text.getString().matches(CHEST_SPAWN_MESSAGE)) {
            waitingForChest += 1;
        }
    }

    /**
     * When a block is updated in the crystal hollows if looking for a chest see if it's a chest and if so add to active. or remove active chests from where air is placed
     *
     * @param pos   location of block update
     * @param state the new state of the block
     */
    public static void onBlockUpdate(BlockPos pos, BlockState state) {
        if (!SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter || CLIENT.player == null) {
            return;
        }
        if (waitingForChest > 0 && state.isOf(Blocks.CHEST)) {
            //make sure it is not too far from the player (more than 10 blocks away)
            if (pos.getSquaredDistance(CLIENT.player.getPos()) > 100) {
                return;
            }
            activeChests.add(pos);
            currentLockCount = 0;
            waitingForChest -= 1;
        } else if (state.isAir() && activeChests.contains(pos)) {
            currentLockCount = 0;
            activeChests.remove(pos);
        }
    }

    /**
     * When a particle is spawned add that particle to active particles if correct for lock picking
     *
     * @param packet particle spawn packet
     */
    public static void onParticle(ParticleS2CPacket packet) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        if (ParticleTypes.CRIT.equals(packet.getParameters().getType())) {
            activeParticles.put(new Vec3d(packet.getX(), packet.getY(), packet.getZ()), System.currentTimeMillis());
        }
    }

    /**
     * Updates {@link CrystalsChestHighlighter#currentLockCount} and clears {@link CrystalsChestHighlighter#activeParticles} based on lock pick related sound events.
     *
     * @param packet sound packet
     */
    public static void onSound(PlaySoundS2CPacket packet) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        String path = packet.getSound().value().getId().getPath();
        //lock picked sound
        if (path.equals("entity.experience_orb.pickup") && packet.getPitch() == 1) {
            currentLockCount += 1;
            activeParticles.clear();
            //lock pick fail sound
        } else if (path.equals("entity.villager.no")) {
            currentLockCount = 0;
            activeParticles.clear();
            //lock pick finish sound
        } else if (path.equals("block.chest.open")) {
            //set the needed lock count to the current, so we know how many locks a chest has
            neededLockCount = currentLockCount;
            activeParticles.clear();
        }
    }

    /**
     * If enabled, renders a box around active treasure chests, taking the color from the config.
     * Additionally, calculates and displaces the highlight to indicate lock-picking spots on chests.
     * Finally, renders text showing how many lock picks the player has done
     *
     * @param context context
     */
    private static void render(WorldRenderContext context) {
        if (!Utils.isInCrystalHollows() || !SkyblockerConfigManager.get().mining.crystalHollows.chestHighlighter) {
            return;
        }
        //render chest outline
        float[] color = SkyblockerConfigManager.get().mining.crystalHollows.chestHighlightColor.getComponents(new float[]{0, 0, 0, 0});
        for (BlockPos chest : activeChests) {
            RenderHelper.renderOutline(context, Box.of(chest.toCenterPos().subtract(0, 0.0625, 0), 0.885, 0.885, 0.885), color, color[3], 3, false);
        }

        //render lock picking if player is looking at chest that is in the active chests list
        if (CLIENT.player == null) {
            return;
        }
        HitResult target = CLIENT.crosshairTarget;
        if (target instanceof BlockHitResult blockHitResult && activeChests.contains(blockHitResult.getBlockPos())) {
            Vec3d chestPos = blockHitResult.getBlockPos().toCenterPos();

            if (!activeParticles.isEmpty()) {
                //the player is looking at a chest use active particle to highlight correct spot
                Vec3d highlightSpot = Vec3d.ZERO;

                //if to old remove particle
                activeParticles.object2LongEntrySet().removeIf(e -> System.currentTimeMillis() - e.getLongValue() > MAX_PARTICLE_LIFE_TIME);

                //add up all particle within range of active block
                for (Vec3d particlePos : activeParticles.keySet()) {
                    if (particlePos.squaredDistanceTo(chestPos) <= 0.75) {
                        highlightSpot = highlightSpot.add(particlePos);
                    }
                }

                //render the spot
                highlightSpot = highlightSpot.multiply((double) 1 / activeParticles.size()).subtract(LOCK_HIGHLIGHT_SIZE.multiply(0.5));
                RenderHelper.renderFilled(context, highlightSpot, LOCK_HIGHLIGHT_SIZE, color, color[3], true);
            }

            //render total text if needed is more than 0
            if (neededLockCount <= 0) {
                return;
            }
            RenderHelper.renderText(context, Text.literal(Math.min(currentLockCount, neededLockCount) + "/" + neededLockCount).withColor(SkyblockerConfigManager.get().mining.crystalHollows.chestHighlightColor.getRGB()), chestPos, true);
        }
    }
}