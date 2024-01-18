package de.hysky.skyblocker.skyblock.endermanslayer;

import de.hysky.skyblocker.config.SkyblockerConfigManager;
import de.hysky.skyblocker.skyblock.entity.MobGlow;
import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import de.hysky.skyblocker.utils.scheduler.Scheduler;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeaconHighlighter {
    static BlockPos pos = null;

    public static void init() {
        WorldRenderEvents.AFTER_TRANSLUCENT.register(BeaconHighlighter::render);
        Scheduler.INSTANCE.scheduleCyclic(BeaconHighlighter::update, 20);
    }

    public static void update() {
        Logger logger = LoggerFactory.getLogger(BeaconHighlighter.class);

        var player = MinecraftClient.getInstance().player;
        var world = MinecraftClient.getInstance().world;
        pos = null;
        if(player != null && world != null &&
                SkyblockerConfigManager.get().slayer.endermanSlayer.highlightBeacons) {
            for(int x = (player.getBlockPos().getX()-15); x<player.getBlockPos().getX()+15; x++) {
                for(int z = (player.getBlockPos().getZ()-15); z<player.getBlockPos().getZ()+15; z++) {
                    for(int y = (player.getBlockPos().getY()-3); y<player.getBlockPos().getY()+7; y++) {
                        var state = world.getBlockState(new BlockPos(x, y, z));
                        if(state.toString().contains("minecraft:beacon")) {
                            pos = new BlockPos(x, y, z);
                        }
                    }
                }
            }
        }
    }

    public static void render(WorldRenderContext context) {
        Logger logger = LoggerFactory.getLogger(BeaconHighlighter.class);
        if(pos != null) {
            RenderHelper.renderFilled(
                    context,
                    pos,
                    new float[]{1.0f, 0.0f, 0.0f},
                    0.5f,
                    true
            );
        }
    }
}
