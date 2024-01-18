package de.hysky.skyblocker.skyblock.endermanslayer;

import de.hysky.skyblocker.utils.Tickable;
import de.hysky.skyblocker.utils.render.RenderHelper;
import de.hysky.skyblocker.utils.render.Renderable;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.util.math.BlockPos;

public class BeaconHighlighter implements Tickable, Renderable {
    BlockPos pos = null;
    @Override
    public void tick(MinecraftClient client) {
        var player = MinecraftClient.getInstance().player;
        var world = MinecraftClient.getInstance().world;
        pos = null;
        if(player != null && world != null) {
            for(int x = (int) (player.getX()-20); x<player.getX()+20; x++) {
                for(int z = (int) (player.getX()-20); z<player.getX()+20; z++) {
                    for(int y = (int) (player.getY()-10); y<player.getY()+10; y++) {
                        var state = world.getBlockState(new BlockPos(x, y, z));
                        var item = ItemStack.fromNbt((NbtCompound) new NbtCompound()
                                .put("id", NbtString.of("minecraft:beacon")));
                        if(state.isOf(Block.getBlockFromItem(item.getItem()))) {
                            pos = new BlockPos(x, y, z);

                        }
                    }
                }
            }
        }

    }

    @Override
    public void render(WorldRenderContext context) {
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
