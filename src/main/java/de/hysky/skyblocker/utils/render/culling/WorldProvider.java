package de.hysky.skyblocker.utils.render.culling;

import com.logisticscraft.occlusionculling.DataProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public class WorldProvider implements DataProvider {
	private final static MinecraftClient CLIENT = MinecraftClient.getInstance();
	private ClientWorld world = null;

	@Override
	public boolean prepareChunk(int chunkX, int chunkZ) {
		this.world = CLIENT.world;
		return this.world != null;
	}

	@Override
	public boolean isOpaqueFullCube(int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		return this.world.getBlockState(pos).isOpaqueFullCube(this.world, pos);
	}

	@Override
	public void cleanup() {
		this.world = null;
	}
}
